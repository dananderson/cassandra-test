/*
 * Copyright (C) 2016 Daniel Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.unittested.cassandra.test.data.cql;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public class BasicCqlSourceLoaderTest {

    @DataProvider
    public Object[][] loadCqlSourceData() {
        return new Object[][] {
                { "select * from table;", 1 },
                { "select * from table;select * from table;", 2 },
                { "select * from table;select * from table;select * from table;", 3 },
                { ";", 0 },
                { "string:;", 0 },
                { "string.cql:;", 0 },
        };
    }

    @Test(dataProvider = "loadCqlSourceData")
    public void loadCqlSource(String cqlSource, int expectedExecuteCallCount) throws Exception {
        // given
        BasicCqlSourceLoader basicCqlSourceLoader = new BasicCqlSourceLoader();
        ArgumentCaptor<Statement> statements = ArgumentCaptor.forClass(Statement.class);
        TestRuntime runtime = createRuntime(statements);

        // when
        basicCqlSourceLoader.loadCqlSource(runtime, cqlSource);

        // then
        verify(runtime.getKeyspace().getSession(), times(expectedExecuteCallCount)).execute(Matchers.any(Statement.class));
        for (Statement statement : statements.getAllValues()) {
            assertThat(statement.getConsistencyLevel(), nullValue());
            assertThat(statement.getSerialConsistencyLevel(), nullValue());
        }
    }

    @DataProvider
    public Object[][] consistencyData() {
        return new Object[][] {
                {
                        "consistency ALL;select * from table;",
                        asList(ConsistencyLevel.ALL),
                },
                {
                        "select * from table;consistency ONE;select * from table;",
                        asList(null, ConsistencyLevel.ONE),
                },
                {
                        "select * from table;consistency ONE;select * from table;select * from table;",
                        asList(null, ConsistencyLevel.ONE, ConsistencyLevel.ONE),
                },
                {
                        "consistency ONE;",
                        Collections.emptyList()
                }
        };
    }

    @Test(dataProvider = "consistencyData")
    public void consistency(String cqlSource, List<ConsistencyLevel> expectedConsistency) throws Exception {
        // given
        BasicCqlSourceLoader basicCqlSourceLoader = new BasicCqlSourceLoader();
        ArgumentCaptor<Statement> statements = ArgumentCaptor.forClass(Statement.class);
        TestRuntime runtime = createRuntime(statements);

        // when
        basicCqlSourceLoader.loadCqlSource(runtime, cqlSource);

        // then
        verify(runtime.getKeyspace().getSession(), times(expectedConsistency.size())).execute(Matchers.any(Statement.class));
        assertThat(statements.getAllValues().size(), is(expectedConsistency.size()));
        for (int i = 0; i < expectedConsistency.size(); i++) {
            assertThat(statements.getAllValues().get(i).getConsistencyLevel(), is(expectedConsistency.get(i)));
            assertThat(statements.getAllValues().get(i).getSerialConsistencyLevel(), nullValue());
        }
    }

    @DataProvider
    public Object[][] serialConsistencyData() {
        return new Object[][] {
                {
                        "consistency LOCAL_SERIAL;select * from table;",
                        asList(ConsistencyLevel.LOCAL_SERIAL),
                },
                {
                        "select * from table;consistency LOCAL_SERIAL;select * from table;",
                        asList(null, ConsistencyLevel.LOCAL_SERIAL),
                },
                {
                        "select * from table;consistency LOCAL_SERIAL;select * from table;select * from table;",
                        asList(null, ConsistencyLevel.LOCAL_SERIAL, ConsistencyLevel.LOCAL_SERIAL),
                },
                {
                        "consistency LOCAL_SERIAL;",
                        Collections.emptyList()
                },

        };
    }

    @Test(dataProvider = "serialConsistencyData")
    public void serialConsistency(String cqlSource, List<ConsistencyLevel> expectedSerialConsistency) throws Exception {
        // given
        BasicCqlSourceLoader basicCqlSourceLoader = new BasicCqlSourceLoader();
        ArgumentCaptor<Statement> statements = ArgumentCaptor.forClass(Statement.class);
        TestRuntime runtime = createRuntime(statements);

        // when
        basicCqlSourceLoader.loadCqlSource(runtime, cqlSource);

        // then
        verify(runtime.getKeyspace().getSession(), times(expectedSerialConsistency.size())).execute(Matchers.any(Statement.class));
        assertThat(statements.getAllValues().size(), is(expectedSerialConsistency.size()));
        for (int i = 0; i < expectedSerialConsistency.size(); i++) {
            assertThat(statements.getAllValues().get(i).getConsistencyLevel(), nullValue());
            assertThat(statements.getAllValues().get(i).getSerialConsistencyLevel(), is(expectedSerialConsistency.get(i)));
        }
    }

    @DataProvider
    public Object[][] illegalConsistencyStatementData() {
        return new Object[][] {
                { "consistency xxx;" },
                { "consistency default;" },
        };
    }

    @Test(dataProvider = "illegalConsistencyStatementData", expectedExceptions = IllegalArgumentException.class)
    public void illegalConsistencyStatement(String cqlSource) throws Exception {
        // given
        BasicCqlSourceLoader basicCqlSourceLoader = new BasicCqlSourceLoader();
        TestRuntime runtime = createRuntime();

        // when
        basicCqlSourceLoader.loadCqlSource(runtime, cqlSource);

        // then
        // CqlException
    }

    @DataProvider
    public Object[][] cqlSourceIOExceptionData() {
        return new Object[][] {
                { "file:&(&%*^%^%^%*&^%" },
                { "file.cql:&(&%*^%^%^%*&^%" },
                { "classpath:..." },
                { "classpath.cql:..." },
        };
    }

    @Test(dataProvider = "cqlSourceIOExceptionData", expectedExceptions = IOException.class)
    public void cqlSourceIOException(String cqlSource) throws Exception {
        // given
        BasicCqlSourceLoader basicCqlSourceLoader = new BasicCqlSourceLoader();
        TestRuntime runtime = createRuntime();

        // when
        basicCqlSourceLoader.loadCqlSource(runtime, cqlSource);

        // then
        // IOException
    }

    @DataProvider
    public Object[][] invalidCqlSourceFormatData() {
        return new Object[][] {
                { "" },
                { "string:" },
                { "cql:" },
                { "string.cql:" },
                { "file:" },
                { "file.cql:" },
                { "classpath:" },
                { "classpath.cql:" },
        };
    }

    @Test(dataProvider = "invalidCqlSourceFormatData", expectedExceptions = CassandraTestException.class)
    public void invalidCqlSourceFormat(String cqlSource) throws Exception {
        // given
        BasicCqlSourceLoader basicCqlSourceLoader = new BasicCqlSourceLoader();
        TestRuntime runtime = createRuntime();

        // when
        basicCqlSourceLoader.loadCqlSource(runtime, cqlSource);

        // then
        // CassandraTestException
    }

    private TestRuntime createRuntime() {
        ArgumentCaptor<Statement> statements = ArgumentCaptor.forClass(Statement.class);
        return createRuntime(statements);
    }


    private TestRuntime createRuntime(ArgumentCaptor<Statement> executeStatementCaptor) {
        TestRuntime runtime = mock(TestRuntime.class);

        when(runtime.getKeyspace()).thenReturn(mock(Keyspace.class));
        when(runtime.getKeyspace().getSession()).thenReturn(mock(Session.class));
        when(runtime.getKeyspace().getSession().execute(executeStatementCaptor.capture())).thenReturn(null);

        return runtime;
    }
}

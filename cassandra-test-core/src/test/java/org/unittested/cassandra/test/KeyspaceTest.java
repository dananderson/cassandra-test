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

package org.unittested.cassandra.test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.mockito.Matchers;
import org.mockito.verification.VerificationMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

@CassandraKeyspace(keyspace = "test", schema = "classpath:cql/sample-schema.cql")
@CassandraData(data = "classpath:cql/sample-data.cql")
@CassandraRollback(afterClass = RollbackStrategy.DROP)
public class KeyspaceTest extends AbstractCassandraTest {

    @Test
    public void getName() throws Exception {
        assertThat(getKeyspace().getName(), is("test"));
    }

    @Test
    public void getSessionTest() throws Exception {
        assertThat(getKeyspace().getSession(), notNullValue());
    }

    @Test
    public void getContainer() throws Exception {
        assertThat(getKeyspace().getContainer(), notNullValue());
    }

    @Test
    public void getTable() throws Exception {
        assertThat(getKeyspace().getTable("test_table"), notNullValue());
    }

    @DataProvider(name = "allTables")
    public static Object[][] allTables() {
        return new Object[][] {
                { "test",  containsInAnyOrder("test_table", "test_table_2") },
                { "", emptyIterable() }
        };
    }

    @Test(dataProvider = "allTables")
    public void allTableNames(String name, Matcher<Iterable<String>> expectedResult) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getSession(), name);

        // when, then
        assertThat(keyspace.allTableNames(), expectedResult);
    }

    @Test(dataProvider = "allTables")
    public void allTables(String name, Matcher<Iterable<String>> expectedResult) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getSession(), name);
        Collection<String> tableNames = new ArrayList<String>();

        // when
        for (Table table : keyspace.allTables()) {
            tableNames.add(table.getName());
        }

        // then
        assertThat(tableNames, expectedResult);
    }

    @DataProvider
    public static Object[][] isNull() {
        return new Object[][] {
                { null, true },
                { "", true },
                { "test", false },
        };
    }

    @Test(dataProvider = "isNull")
    public void isNull(String name, boolean expectedResult) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getSession(), name);

        // when, then
        assertThat(keyspace.isNull(), is(expectedResult));
    }

    @DataProvider
    public static Object[][] isCaseSensitiveName() {
        return new Object[][] {
                { null, false },
                { "", false },
                { "test", false },
                { "Test", true },
        };
    }

    @Test(dataProvider = "isCaseSensitiveName")
    public void isCaseSensitiveName(String name, boolean expectedResult) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getSession(), name);

        // when, then
        assertThat(keyspace.isCaseSensitiveName(), is(expectedResult));
    }

    @DataProvider
    public static Object[][] exists() {
        return new Object[][] {
                { null, false },
                { "", false },
                { "test", true },
        };
    }

    @Test(dataProvider = "exists")
    public void exists(String name, boolean expectedResult) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getSession(), name);

        // when, then
        assertThat(keyspace.exists(), is(expectedResult));
    }

    @DataProvider
    public static Object[][] tableExists() {
        return new Object[][] {
                { "test", "test_table", true },
                { "test", "", false },
                { "test", null, false },
                { "", "test_table", false },
                { null, "test_table", false },
                { "unknown", "test_table", false },
        };
    }

    @Test(dataProvider = "tableExists")
    public void tableExists(String name, String tableName, boolean expectedResult) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getSession(), name);

        // when, then
        assertThat(keyspace.tableExists(tableName), is(expectedResult));
    }

    @DataProvider(name = "use")
    public static Object[][] use() {
        Session testLoggedKeyspace = mock(Session.class);
        when(testLoggedKeyspace.getLoggedKeyspace()).thenReturn("test");

        return new Object[][] {
                { "test", testLoggedKeyspace, never() },
                { "test", mock(Session.class), times(1) },
                { "", mock(Session.class), never() },
                { null, mock(Session.class), never() },
        };
    }

    @Test(dataProvider = "use")
    public void use(String name, Session session, VerificationMode expectedExecuteCallCount) throws Exception {
        // given
        Keyspace keyspace = new Keyspace(session, name);

        // when
        keyspace.use();

        // then
        verify(session, expectedExecuteCallCount).execute(Matchers.anyString());
    }

    @Test
    public void getSchemaSignature() throws Exception {
        // given
        Integer before = getKeyspace().getSchemaSignature();

        getKeyspace().getTable("test_table").drop();

        // when
        Integer after = getKeyspace().getSchemaSignature();

        // then
        assertThat(after, is(not(before)));
    }

    @Test
    public void getSchemaSignatureForNullKeyspace() throws Exception {
        Session session = null;

        try {
            session = getCluster().connect();
            Keyspace keyspace = new Keyspace(session, "");

            assertThat(keyspace.getSchemaSignature(), is(629));
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}

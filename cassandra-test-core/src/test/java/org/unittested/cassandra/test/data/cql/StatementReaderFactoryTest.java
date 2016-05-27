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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.io.FileNotFoundException;

import org.unittested.cassandra.test.exception.CassandraTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StatementReaderFactoryTest {

    @DataProvider(name = "cqlSourceData")
    public static Object[][] cqlSourceData() {
        return new Object[][] {
                { "INSERT INTO test_table(id, name) VALUES (1000, 'insert_from_file');", 1 },
                { "string:INSERT INTO test_table(id, name) VALUES (1000, 'insert_from_file');", 1 },
                { "string.cql:INSERT INTO test_table(id, name) VALUES (1000, 'insert_from_file');", 1 },
                { "classpath:cql/sample-data.cql", 1 },
                { "classpath: cql/sample-data.cql", 1 },
                { "classpath.cql:cql/sample-data.cql", 1 },
                { "classpath.cql: cql/sample-data.cql", 1 },
                { "file:target/test-classes/cql/sample-data.cql", 1 },
                { "file: target/test-classes/cql/sample-data.cql", 1 },
                { "file.cql:target/test-classes/cql/sample-data.cql", 1 },
                { "file.cql: target/test-classes/cql/sample-data.cql", 1 },
                { ";", 0 },
                { "string:;", 0 },
                { "string.cql:;", 0 },
        };
    }

    @Test(dataProvider = "cqlSourceData")
    public void createStatementReader(String cqlSource, int expectedStatementCount) throws Exception {
        StatementReader statementReader = null;

        try {
            // given
            StatementReaderFactory factory = new StatementReaderFactory();

            // when
            statementReader = factory.createStatementReader(cqlSource);

            // then
            assertThat(statementReader.all(), hasSize(expectedStatementCount));
        } finally {
            if (statementReader != null) {
                statementReader.close();
            }
        }
    }

    @DataProvider(name = "invalidCqlSourceFormatData")
    public static Object[][] invalidCqlSourceFormatData() {
        return new Object[][] {
                { "invalid:..." },
                { "invalid.invalid:..." },
                { "classpath.invalid:..." },
                { "cql:not cql" },
                { "" },
                { "cql:" },
                { "string:" },
                { "string.cql:" },
                { "file:" },
                { "classpath:" },
                { " " },
                { "cql: " },
                { "string: " },
                { "string.cql: " },
                { "file: " },
                { "classpath: " },
        };
    }

    @Test(dataProvider = "invalidCqlSourceFormatData", expectedExceptions = CassandraTestException.class)
    public void createStatementReaderWithInvalidCqlSourceFormat(String cqlSource) throws Exception {
        // given
        StatementReaderFactory factory = new StatementReaderFactory();

        // when
        factory.createStatementReader(cqlSource);

        // then
        // CassandraTestException
    }

    @DataProvider(name = "fileDoesNotExistData")
    public static Object[][] fileDoesNotExistData() {
        return new Object[][] {
                { "file:target/dasdlajsdjasdjlasjdlajsdkl" },
                { "file.cql:target/dasdlajsdjasdjlasjdlajsdkl" },
                { "classpath:dasdasdasdasdasdasd" },
                { "classpath.cql:dasdasdasdasdasdasd" },
        };
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void createStatementReaderWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        StatementReaderFactory factory = new StatementReaderFactory();

        // when
        factory.createStatementReader(cqlSource);

        // then
        // FileNotFoundException
    }

    @DataProvider(name = "directoryData")
    public static Object[][] directoryData() {
        return new Object[][] {
                { "file:target" },
                { "file.cql:target" },
                { "classpath:cql" },
                { "classpath.cql:cql" },
        };
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void createStatementReaderWithDirectory(String cqlSource) throws Exception {
        // given
        StatementReaderFactory factory = new StatementReaderFactory();

        // when
        factory.createStatementReader(cqlSource);

        // then
        // UnsupportedOperationException
    }
}

/*
 * Copyright (C) 2016 Daniel Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.unittested.cassandra.test.data.cql;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.unittested.cassandra.test.exception.CassandraTestException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.util.DriverCompatibility;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/**
 * Test cases for {@link CqlStatementReader}.
 */
public class CqlStatementReaderTest {

    private CqlStatementReader testee;

    public CqlStatementReaderTest() {

    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (testee != null) {
            testee.close();
            testee = null;
        }
    }

    @Test
    public void allTwice() throws Exception {
        // given
        testee = statementParser("use x; use y;");
        assertThat(testee.all().size(), is(2));

        // when
        Collection<Statement> statements = testee.all();

        // then
        assertThat(statements.isEmpty(), is(true));
        assertThat(testee.hasMore(), is(false));
    }

    @Test
    public void one() throws Exception {
        // given
        testee = statementParser("use x;");

        // when
        Statement statement = testee.one();

        // then
        assertThat(getQueryString(statement), is("use x;"));
        assertThat(testee.hasMore(), is(false));
    }

    @Test
    public void oneTwice() throws Exception {
        // given
        testee = statementParser("use x;");
        assertThat(testee.one(), notNullValue());

        // when
        Statement statement = testee.one();

        // then
        assertThat(statement, nullValue());
        assertThat(testee.hasMore(), is(false));
    }

    @Test(dataProvider = "emptyStatementsData")
    public void hasMoreWithEmptyStatements(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        boolean hasMore = testee.hasMore();

        // then
        assertThat(hasMore, is(false));
    }

    @DataProvider
    public Object[][] emptyStatementsData() {
        return new Object[][] {
                { "" },
                { " " },
                { ";" },
                { ";;" },
                { " ;  " },
                { "/* comment */"},
                { "/* \ncomment with\nnewlines */"},
                { "// comment"},
                { "-- comment"},
                { "-- select * from x where id = 'id900909';"},
                { "// select * from x where id = 'id900909';"},
                { "/* select * from x where id = 'id900909';*/"},
        };
    }

    @Test(dataProvider = "emptyStatementsData")
    public void emptyStatements(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Collection<Statement> statements = testee.all();

        // then
        assertThat(statements.isEmpty(), is(true));
    }

    @DataProvider
    public Object[][] singleStatementsData() {
        return new Object[][] {
                { "select * from x where id = 'id900909';",
                        "select * from x where id = 'id900909';" },
                { "    select * from x where id = 'id900909'   ;    ",
                        "select * from x where id = 'id900909'   ;" },
                { " /* comment */ select * from x where id = 'id900909';    " +
                        "",  "select * from x where id = 'id900909';" },
                { "select * from x where id /* comment */ = 'id900909';",
                        "select * from x where id  = 'id900909';" },
                { "select * from x where id/* comment no space */= 'id900909';",
                        "select * from x where id= 'id900909';" },
                { "select * from x where id = 'id900909'; -- trailing comment ",
                        "select * from x where id = 'id900909';" },
                { "select * from x where id = 'id900909'; // trailing comment  ",
                        "select * from x where id = 'id900909';" },
                { "alter keyspace \"Excalibur\" with replication = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };",
                        "alter keyspace \"Excalibur\" with replication = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };" },
                { "create table timeseries (\n event_type text,\n insertion_time timestamp,\n event blob,\n primary key (event_type, insertion_time)\n )\n with clustering order by (insertion_time desc);",
                        "create table timeseries (\n event_type text,\n insertion_time timestamp,\n event blob,\n primary key (event_type, insertion_time)\n )\n with clustering order by (insertion_time desc);" },
                { "insert into users (todo) values ( { '2013-9-22 12:01'  : 'birthday wishes to Bilbo', '2013-10-1 18:00' : 'Check into Inn of Prancing Pony' });",
                        "insert into users (todo) values ( { '2013-9-22 12:01'  : 'birthday wishes to Bilbo', '2013-10-1 18:00' : 'Check into Inn of Prancing Pony' });" },
                { "update users set todo = { '2012-9-24' : 'enter mordor', '2012-10-2 12:00' : 'throw ring into mount doom' } where user_id = 'frodo';",
                        "update users set todo = { '2012-9-24' : 'enter mordor', '2012-10-2 12:00' : 'throw ring into mount doom' } where user_id = 'frodo';"},
                { "select\n *\n from\n x\n where\n id\n = 'id900909'\n;",
                        "select\n *\n from\n x\n where\n id\n = 'id900909'\n;" },
        };
    }

    @Test(dataProvider = "singleStatementsData")
    public void singleStatements(String input, String expectedCql) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Collection<Statement> statements = testee.all();

        // then
        assertThat(statements.size(), is(1));
        assertThat(getQueryString(statements.iterator().next()), is(expectedCql));
    }

    @DataProvider
    public Object[][] multipleStatementsData() {
        return new Object[][]{
                { "select * from x; select * from y;",
                        Arrays.asList("select * from x;", "select * from y;") },
                { "select * from x;\nselect * from y;",
                        Arrays.asList("select * from x;", "select * from y;") },
                { "/* comment */\nselect * from x;\n\n\n\n// comment\nselect * from y;\n--comment",
                        Arrays.asList("select * from x;", "select * from y;") },
                { "select * from x; select * from y",
                        Arrays.asList("select * from x;", "select * from y") },
        };
    }

    @Test(dataProvider = "multipleStatementsData")
    public void multipleStatements(String input, List<String> expectedCql) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Collection<Statement> statements = testee.all();

        // then
        assertThat(statements.size(), is(expectedCql.size()));

        Iterator<String> expectedCqlIterator = expectedCql.iterator();
        Iterator<Statement> statementIterator = statements.iterator();

        while(expectedCqlIterator.hasNext() && statementIterator.hasNext()) {
            assertThat(getQueryString(statementIterator.next()), is(expectedCqlIterator.next()));
        }
    }

    @DataProvider
    public Object[][] emptyBatchStatementsData() {
        return new Object[][]{
                { "begin batch ; apply batch;" },
                { "begin unlogged batch ; apply batch;" },
                { "begin counter batch ; apply batch;" },
                { "begin batch using timestamp 1234; apply batch;" },
                { "begin unlogged batch using timestamp 1234; apply batch;" },
                { "begin batch apply batch;" },
                { "begin unlogged batch apply batch;" },
                { "begin counter batch apply batch;" },
                { "begin batch using timestamp 1234 apply batch;" },
                { "begin unlogged batch using timestamp 1234 apply batch;" },
        };
    }

    @Test(dataProvider = "emptyBatchStatementsData", expectedExceptions = CassandraTestException.class)
    public void emptyBatchStatements(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        testee.one();

        // then
        // StatementReaderException
    }

    @DataProvider
    public Object[][] batchStatementsData() {
        return new Object[][]{
                { "begin batch delete a from b where c = 'd'; apply batch;",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.LOGGED },
                { "begin batch; delete a from b where c = 'd'; apply batch;",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.LOGGED  },
                { "begin batch  ;;delete a from b where c = 'd'; apply batch",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.LOGGED  },
                { "begin unlogged batch delete a from b where c = 'd'; apply batch;",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.UNLOGGED },
                { "begin unlogged batch; delete a from b where c = 'd'; apply batch;",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.UNLOGGED  },
                { "begin unlogged batch  ;;delete a from b where c = 'd'; apply batch",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.UNLOGGED  },
                { "begin counter batch using timestamp 1234 delete a from b where c = 'd'; apply batch;",
                        "delete a from b where c = 'd';", 1234L, BatchStatement.Type.COUNTER },
                { "begin counter batch using timestamp 1234; delete a from b where c = 'd'; apply batch;",
                        "delete a from b where c = 'd';", 1234L, BatchStatement.Type.COUNTER  },
                { "begin counter batch using timestamp 1234  ;;delete a from b where c = 'd'; apply batch",
                        "delete a from b where c = 'd';", 1234L, BatchStatement.Type.COUNTER  },
                { "begin batch; delete a from b where c = 'd'; ; apply batch",
                        "delete a from b where c = 'd';", Long.MIN_VALUE, BatchStatement.Type.LOGGED  },
        };
    }

    @Test(dataProvider = "batchStatementsData")
    public void batchStatements(String input, String expectedCql, Long expectedTimestamp, BatchStatement.Type expectedBatchType) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Statement statement = testee.one();

        // then
        assertThat(testee.one(), nullValue());
        assertThat(statement, instanceOf(BatchStatement.class));

        BatchStatement batchStatement = (BatchStatement)statement;
        Long timestamp = DriverCompatibility.getDefaultTimestamp(batchStatement);

        if (timestamp != null) {
            assertThat(timestamp, is(expectedTimestamp));
        }

        assertThat(getBatchType(batchStatement), is(expectedBatchType));
        assertThat(batchStatement.getStatements(), hasSize(1));
        assertThat(getQueryString(batchStatement.getStatements().iterator().next()), is(expectedCql));
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void invalidCql() throws Exception {
        // given
        testee = statementParser("unknown * from t;");

        // when
        testee.one();

        // then
        // StatementReaderException
    }

    @DataProvider
    public Object[][] invalidCommentData() {
        return new Object[][] {
                { "select * from /* comment t;" },
                { "select * from /* * comment t;" },
                { "select * from /* * / comment t;" },
                { "select * from /x comment t;" },
                { "select * from t; / comment" },
                { "select * from t; / / comment" },
                { "select * from t; - comment" },
                { "select * from t; - - comment" },
        };
    }

    @Test(dataProvider = "invalidCommentData", expectedExceptions = CassandraTestException.class)
    public void invalidComment(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        testee.all();

        // then
        // StatementReaderException
    }

    @DataProvider
    public Object[][] validCommentData() {
        return new Object[][] {
                { "select * from /**/ t;",          "select * from  t;" },
                { "select * from /* */ t;",         "select * from  t;" },
                { "select * from /*comment*/ t;",   "select * from  t;" },
                { "select * from /* comment */ t;", "select * from  t;" },
                { "/**/ select * from t;",          "select * from t;" },
                { "/* comment */ select * from t;", "select * from t;" },
                { "/*comment*/ select * from t;",   "select * from t;" },
                { "select * from t;/**/",           "select * from t;" },
                { "select * from t;/* comment */",  "select * from t;" },
                { "select * from t;/*comment*/",    "select * from t;" },
                { "select * from t;/*comment*/",    "select * from t;" },
                { "select * from t; // comment",    "select * from t;" },
                { "select * from t; // select * from x;",    "select * from t;" },
                { "select * from t; -- comment",    "select * from t;" },
                { "select * from t; -- select * from x;",    "select * from t;" },
                { "select\n *\n from\n // inline comment\n x\n where\n id\n = 'id900909'\n;", "select\n *\n from\n  x\n where\n id\n = 'id900909'\n;" },
                { "select\n *\n from\n -- inline comment\n x\n where\n id\n = 'id900909'\n;", "select\n *\n from\n  x\n where\n id\n = 'id900909'\n;" },
        };
    }

    @Test(dataProvider = "validCommentData")
    public void validComment(String input, String expectedCql) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Collection<Statement> statements = testee.all();

        // then
        assertThat(statements.size(), is(1));
        assertThat(getQueryString(statements.iterator().next()), is(expectedCql));
    }

    @DataProvider
    public Object[][] unterminatedQuoteData() {
        return new Object[][] {
                { "select * from x where id = 'id900909;" },
                { "select * from x where id = \"id900909;" },
        };
    }

    @Test(dataProvider = "unterminatedQuoteData", expectedExceptions = CassandraTestException.class)
    public void unterminatedQuote(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        testee.one();

        // then
        // StatementReaderException
    }

    @DataProvider
    public Object[][] hasMoreData() {
        return new Object[][] {
                { "select * from t;", true },
                { "select * from t; select * from x;", true },
                { ";", false },
        };
    }

    @Test(dataProvider = "hasMoreData")
    public void hasMore(String input, boolean expectedHasMoreResult) throws Exception {
        // given
        testee = statementParser(input);

        // when
        boolean hasMoreResult = testee.hasMore();

        // then
        assertThat(hasMoreResult, is(expectedHasMoreResult));
    }

    @DataProvider
    public Object[][] invalidBatchData() {
        return new Object[][] {
                { "apply batch;" },
                { "begin batch; apply batch;" },
                { "begin batch; ; apply batch;" },
                { "begin batch; ;; apply batch;" },
                { "begin batch; begin batch; apply batch;" },
                { "begin batch; begin batch; delete a from b where c = 'd'; apply batch;" },
                { "begin batch; delete a from b where c = 'd'; begin batch;  apply batch;" },
                { "begin batch; consistency serial; apply batch;" },
                { "begin batch; consistency one; apply batch;" },
        };
    }

    @Test(dataProvider = "invalidBatchData", expectedExceptions = CassandraTestException.class)
    public void invalidBatch(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        testee.one();

        // then
        // StatementReaderException
    }

    @DataProvider
    public Object[][] escapeInQuotesData() {
        return new Object[][] {
                { "delete a from b where c = 'd'", "delete a from b where c = 'd'" },
                { "delete a from b where c = '''d'", "delete a from b where c = ''d'" },
                { "delete a from b where c = ' ''d'", "delete a from b where c = ' 'd'" },
                { "delete a from b where c = 'd'''", "delete a from b where c = 'd''" },
                { "delete a from b where c = 'd'' '", "delete a from b where c = 'd' '" },
                { "delete a from b where c = ''", "delete a from b where c = ''" },
                { "select * from \"a\"", "select * from \"a\""},
                { "select * from \"\"\"a\"", "select * from \"\"a\""},
                { "select * from \"a\"\"\"", "select * from \"a\"\""},
                { "select * from \"a\"\"\" ", "select * from \"a\"\" "},
                { "select * from \" \"\"a\"", "select * from \" \"a\""},
        };
    }

    @Test(dataProvider = "escapeInQuotesData")
    public void escapeInQuotes(String input, String expectedCql) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Statement statement = testee.one();

        // then
        assertThat(getQueryString(statement), is(expectedCql));
    }

    @DataProvider
    public Object[][] consistencyData() {
        return new Object[][] {
                // consistency
                { "consistency one", ConsistencyLevel.ONE, null },
                { "consistency TWO;", ConsistencyLevel.TWO, null },
                { "consistency Three; ", ConsistencyLevel.THREE, null },
                { "consistency ALL   ", ConsistencyLevel.ALL, null },
                { "CONSISTENCY AnY;", ConsistencyLevel.ANY, null },
                { "Consistency each_quorum ;", ConsistencyLevel.EACH_QUORUM, null },
                { "  consistency    local_ONE  ", ConsistencyLevel.LOCAL_ONE, null },
                { "consistency LOCAL_QUORUM\n;", ConsistencyLevel.LOCAL_QUORUM, null },
                { "consistency \nQuorum", ConsistencyLevel.QUORUM, null },
                // serial consistency
                { "consistency serial", null, ConsistencyLevel.SERIAL },
                { "consistency local_serial", null, ConsistencyLevel.LOCAL_SERIAL },
        };
    }

    @Test(dataProvider = "consistencyData")
    public void consistency(String input, ConsistencyLevel expectedConsistency, ConsistencyLevel expectedSerialConsistency) throws Exception {
        // given
        testee = statementParser(input);

        // when
        Statement statement = testee.one();

        // then
        assertThat(statement, instanceOf(ConsistencyStatement.class));
        ConsistencyStatement consistencyStatement = (ConsistencyStatement)statement;
        Statement applied = consistencyStatement.applyConsistency(new SimpleStatement(""));

        assertThat(applied.getConsistencyLevel(), is(expectedConsistency));
        assertThat(applied.getSerialConsistencyLevel(), is(expectedSerialConsistency));
    }

    @DataProvider
    public Object[][] invalidConsistencyData() {
        return new Object[][] {
                { "consistency xxx" },
                { "consistency default;" },
        };
    }

    @Test(dataProvider = "invalidConsistencyData", expectedExceptions = IllegalArgumentException.class)
    public void invalidConsistency(String input) throws Exception {
        // given
        testee = statementParser(input);

        // when
        testee.one();

        // then
        // CassandraTestException
    }

    @Test
    public void uuid() throws Exception {
        // given
        testee = statementParser("select * from t where id = bd37d523-512e-4412-939a-a4eebe2745f9");

        // when
        Statement statement = testee.one();

        // then
        assertThat(getQueryString(statement), is("select * from t where id = bd37d523-512e-4412-939a-a4eebe2745f9"));
    }

    private BatchStatement.Type getBatchType(BatchStatement batchStatement) throws Exception {
        Field batchType = BatchStatement.class.getDeclaredField("batchType");

        batchType.setAccessible(true);

        return (BatchStatement.Type)batchType.get(batchStatement);
    }

    private String getQueryString(Statement statement) {
        assertThat(statement, instanceOf(RegularStatement.class));
        return ((RegularStatement)statement).getQueryString();
    }

    private CqlStatementReader statementParser(String input) {
        return new CqlStatementReader(new StringReader(input));
    }
}

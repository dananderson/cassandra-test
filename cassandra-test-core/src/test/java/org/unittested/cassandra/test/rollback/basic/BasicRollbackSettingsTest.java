package org.unittested.cassandra.test.rollback.basic;

import org.unittested.cassandra.test.AbstractCassandraTest;
import org.unittested.cassandra.test.TestSettings;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.rollback.RollbackSettings;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.apache.commons.lang3.ArrayUtils.*;
import static org.unittested.cassandra.test.rollback.RollbackStrategy.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@CassandraKeyspace(
        value = "rollback_settings_test",
        schema = { "create table a (x int primary key);", "create table b (x int primary key);", "create table p (x int primary key);" },
        protectedTables = "p")
@CassandraRollback(afterClass = DROP)
@CassandraData("insert into p(x) values (1000); insert into a(x) values (1000); insert into b(x) values (1000);")
public class BasicRollbackSettingsTest extends AbstractCassandraTest {

    public static final String [] EMPTY = EMPTY_STRING_ARRAY;

    @DataProvider
    public static Object[][] rollbackSettings() {
        return new Object[][] {
                // truncates
                { false, TRUNCATE, EMPTY, EMPTY, toArray("p"), true, true, 0, 0, 1 },
                { false, TRUNCATE, EMPTY, EMPTY, EMPTY, true, true, 0, 0, 0 },
                { false, TRUNCATE, toArray("a"), EMPTY, EMPTY, true, true, 0, 1, 1 },
                { false, TRUNCATE, toArray("a"), EMPTY, toArray("p"), true, true, 0, 1, 1 },
                { false, TRUNCATE, EMPTY, toArray("b"), EMPTY, true, true, 0, 1, 0 },
                { false, TRUNCATE, EMPTY, toArray("b"), toArray("p"), true, true, 0, 1, 1 },

                { false, KEYSPACE_TRUNCATE, EMPTY, EMPTY, toArray("p"), true, true, 0, 0, 1 },
                { false, KEYSPACE_TRUNCATE, EMPTY, EMPTY, EMPTY, true, true, 0, 0, 0 },
                { false, KEYSPACE_TRUNCATE, toArray("a"), EMPTY, EMPTY, true, true, 0, 0, 0 },
                { false, KEYSPACE_TRUNCATE, toArray("a"), EMPTY, toArray("p"), true, true, 0, 0, 1 },
                { false, KEYSPACE_TRUNCATE, EMPTY, toArray("b"), EMPTY, true, true, 0, 0, 0 },
                { false, KEYSPACE_TRUNCATE, EMPTY, toArray("b"), toArray("p"), true, true, 0, 0, 1 },

                // drop keyspace

                { false, DROP, EMPTY, EMPTY, toArray("p"), true, false, 0, 0, 0 },
                { false, DROP, EMPTY, toArray("a"), EMPTY, true, false, 0, 0, 0 },
                { false, DROP, toArray("a"), EMPTY, EMPTY, true, false, 0, 0, 0 },
                { false, DROP, EMPTY, EMPTY, EMPTY, true, false, 0, 0, 0 },

                // rollback disabled

                { false, NONE, EMPTY, EMPTY, toArray("p"), true, true, 1, 1, 1 },
                { false, NONE, EMPTY, toArray("a"), EMPTY, true, true, 1, 1, 1 },
                { false, NONE, toArray("a"), EMPTY, EMPTY, true, true, 1, 1, 1 },
                { false, NONE, EMPTY, EMPTY, EMPTY, true, true, 1, 1, 1 },

                // null keyspace
                { true, NONE, EMPTY, EMPTY, EMPTY, true, false, 0, 0, 0 },
                { true, TRUNCATE, EMPTY, EMPTY, EMPTY, true, false, 0, 0, 0 },
                { true, KEYSPACE_TRUNCATE, EMPTY, EMPTY, EMPTY, true, false, 0, 0, 0 },
                { true, DROP, EMPTY, EMPTY, EMPTY, true, false, 0, 0, 0 },
        };
    }

    @Test(dataProvider = "rollbackSettings")
    public void rollbackAfterMethod(
                boolean useNullKeyspace,
                RollbackStrategy strategy,
                String [] tableInclusions,
                String [] tableExclusions,
                String [] protectedTables,
                boolean canDropKeyspace,
                boolean expectKeyspaceExists,
                long expectedTableASize,
                long expectedTableBSize,
                long expectedTablePSize) throws Exception {
        // given
        RollbackSettings rollbackSettings = new BasicRollbackSettings(tableInclusions, tableExclusions, strategy, NONE);
        TestRuntime runtime = createRuntime(useNullKeyspace, protectedTables, canDropKeyspace);

        // when
        rollbackSettings.rollbackAfterMethod(runtime);

        // then
        if (!useNullKeyspace) {
            assertThat(getKeyspace().exists(), is(expectKeyspaceExists));
        }

        if (expectKeyspaceExists) {
            assertThat(getKeyspace().getTable("p").getCount(), is(expectedTablePSize));
            assertThat(getKeyspace().getTable("a").getCount(), is(expectedTableASize));
            assertThat(getKeyspace().getTable("b").getCount(), is(expectedTableBSize));
        }
    }

    @Test(dataProvider = "rollbackSettings")
    public void rollbackAfterClass(
                boolean useNullKeyspace,
                RollbackStrategy strategy,
                String [] tableInclusions,
                String [] tableExclusions,
                String [] protectedTables,
                boolean canDropKeyspace,
                boolean expectKeyspaceExists,
                long expectedTableASize,
                long expectedTableBSize,
                long expectedTablePSize) throws Exception {
        // given
        RollbackSettings rollbackSettings = new BasicRollbackSettings(tableInclusions, tableExclusions, NONE, strategy);
        TestRuntime runtime = createRuntime(useNullKeyspace, protectedTables, canDropKeyspace);

        // when
        rollbackSettings.rollbackAfterClass(runtime);

        // then
        if (!useNullKeyspace) {
            assertThat(getKeyspace().exists(), is(expectKeyspaceExists));
        }

        if (expectKeyspaceExists) {
            assertThat(getKeyspace().getTable("p").getCount(), is(expectedTablePSize));
            assertThat(getKeyspace().getTable("a").getCount(), is(expectedTableASize));
            assertThat(getKeyspace().getTable("b").getCount(), is(expectedTableBSize));
        }
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void invalidTableSpecification() throws Exception {
        // given
        String [] tableInclusions = toArray("a");
        String [] tableExclusions = toArray("b");

        // when
        new BasicRollbackSettings(tableInclusions, tableExclusions, NONE, NONE);

        // then
        // CassandraTestException
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void afterMethodIllegalDrop() throws Exception {
        // given
        RollbackSettings rollbackSettings = new BasicRollbackSettings(EMPTY, EMPTY, DROP, NONE);
        TestRuntime runtime = createRuntime(false, EMPTY, false);

        // when
        rollbackSettings.rollbackAfterMethod(runtime);

        // then
        // CassandraTestException
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void afterClassIllegalDrop() throws Exception {
        // given
        RollbackSettings rollbackSettings = new BasicRollbackSettings(EMPTY, EMPTY, NONE, DROP);
        TestRuntime runtime = createRuntime(false, EMPTY, false);

        // when
        rollbackSettings.rollbackAfterClass(runtime);

        // then
        // CassandraTestException
    }

    private TestRuntime createRuntime(boolean useNullKeyspace, String[] protectedTables, boolean canDropKeyspace) {
        Keyspace keyspace;

        if (useNullKeyspace) {
            keyspace = new Keyspace(getCluster().connect(), Keyspace.NULL);
        } else {
            keyspace = getKeyspace();
        }

        TestRuntime runtime = mock(TestRuntime.class);

        when(runtime.getKeyspace()).thenReturn(keyspace);
        when(runtime.getTestSettings()).thenReturn(mock(TestSettings.class));
        when(runtime.getTestSettings().getKeyspaceSettings()).thenReturn(mock(KeyspaceSettings.class));
        when(runtime.getTestSettings().getKeyspaceSettings().canDropKeyspace()).thenReturn(canDropKeyspace);
        when(runtime.getTestSettings().getKeyspaceSettings().getProtectedTables()).thenReturn(protectedTables);

        return runtime;
    }
}

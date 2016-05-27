package org.unittested.cassandra.test.keyspace.foreign;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum.*;

import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.unittested.cassandra.test.AbstractCassandraTest;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.TestSettings;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;
import org.mockito.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@CassandraKeyspace(keyspace = "immutable_schema_settings_test")
@CassandraRollback(afterMethod = RollbackStrategy.NONE, afterClass = RollbackStrategy.DROP)
public class ForeignKeyspaceSettingsTest extends AbstractCassandraTest {

    @DataProvider
    public static Object[][] syncConfiguration() {
        return new Object[][]{
                // tracked by schema state manager
                { KEYSPACE, true, false, false, 0, 1, 0 },
                { CLUSTER, true, false, false, 0, 0, 1 },
                { NONE, true, false, false, 0, 0, 0 },
                // not tracked by schema state manager
                { KEYSPACE, false, false, false, 1, 0, 0 },
                { CLUSTER, false, false, false, 1, 0, 0 },
                { NONE, false, false, false, 1, 0, 0 },
        };
    }

    @Test(dataProvider = "syncConfiguration")
    public void sync(SchemaChangeDetectionEnum schemaChangeDetection,
                     boolean isTracked,
                     boolean keyspaceSignatureResult,
                     boolean clusterSignatureResult,
                     int expectedTrackCalls,
                     int expectedKeyspaceSignatureCalls,
                     int expectedClusterSignatureCalls) throws Exception {
        // given
        TestRuntime runtime = createRuntime(getKeyspace(), schemaChangeDetection);
        KeyspaceStateManager keyspaceStateManager = mock(KeyspaceStateManager.class);

        when(keyspaceStateManager.hasKeyspaceCqlSignatureChanged(Matchers.anyInt(), Matchers.anyInt()))
                .thenReturn(keyspaceSignatureResult);
        when(keyspaceStateManager.hasClusterSchemaVersionChanged(Matchers.anyInt(), Matchers.any(UUID.class)))
                .thenReturn(clusterSignatureResult);
        when(keyspaceStateManager.isTracked(Matchers.anyInt()))
                .thenReturn(isTracked);

        // when
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);

        // then
        assertThat(getKeyspace().exists(), is(true));
        verify(keyspaceStateManager, times(expectedTrackCalls)).track(
                Matchers.anyInt(), Matchers.any(UUID.class), Matchers.anyInt());
        verify(keyspaceStateManager, times(expectedClusterSignatureCalls)).hasClusterSchemaVersionChanged(
                Matchers.anyInt(), Matchers.any(UUID.class));
        verify(keyspaceStateManager, times(expectedKeyspaceSignatureCalls)).hasKeyspaceCqlSignatureChanged(
                Matchers.anyInt(), Matchers.anyInt());
        verify(keyspaceStateManager, times(1)).isTracked(Matchers.anyInt());
    }

    @DataProvider
    public static Object[][] syncFailConfiguration() {
        return new Object[][]{
                { KEYSPACE, true, false, 1, 0 },
                { CLUSTER, false, true, 0, 1 },
        };
    }

    @Test(dataProvider = "syncFailConfiguration", expectedExceptions = CassandraTestException.class)
    public void syncFail(SchemaChangeDetectionEnum schemaChangeDetection,
                     boolean keyspaceSignatureResult,
                     boolean clusterSignatureResult,
                     int expectedKeyspaceSignatureCalls,
                     int expectedClusterSignatureCalls) throws Exception {
        // given
        TestRuntime runtime = createRuntime(getKeyspace(), schemaChangeDetection);
        KeyspaceStateManager keyspaceStateManager = mock(KeyspaceStateManager.class);

        when(keyspaceStateManager.hasKeyspaceCqlSignatureChanged(
                Matchers.anyInt(), Matchers.anyInt()))
                .thenReturn(keyspaceSignatureResult);
        when(keyspaceStateManager.hasClusterSchemaVersionChanged(
                Matchers.anyInt(), Matchers.any(UUID.class)))
                .thenReturn(clusterSignatureResult);
        when(keyspaceStateManager.isTracked(Matchers.anyInt()))
                .thenReturn(true);

        // when
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);

        // then
        assertThat(getKeyspace().exists(), is(true));
        verify(keyspaceStateManager, times(0)).track(
                Matchers.anyInt(), Matchers.any(UUID.class), Matchers.anyInt());
        verify(keyspaceStateManager, times(expectedClusterSignatureCalls)).hasClusterSchemaVersionChanged(
                Matchers.anyInt(), Matchers.any(UUID.class));
        verify(keyspaceStateManager, times(expectedKeyspaceSignatureCalls)).hasKeyspaceCqlSignatureChanged(
                Matchers.anyInt(), Matchers.anyInt());
        verify(keyspaceStateManager, times(1)).isTracked(Matchers.anyInt());
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void nullKeyspace() throws Exception {
        new ForeignKeyspaceSettings(Keyspace.NULL, false, KEYSPACE, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void droppedKeyspace() throws Exception {
        // given
        TestRuntime runtime = createRuntime(getKeyspace(), KEYSPACE);
        KeyspaceStateManager keyspaceStateManager = mock(KeyspaceStateManager.class);

        getKeyspace().drop();

        // when
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);

        // then
        // CassandraTestException
    }

    private TestRuntime createRuntime(Keyspace keyspace,
                                      SchemaChangeDetectionEnum schemaChangeDetection) throws Exception {
        KeyspaceSettings keyspaceSettings = new ForeignKeyspaceSettings(
                keyspace.getName(),
                keyspace.isCaseSensitiveName(),
                schemaChangeDetection,
                ArrayUtils.EMPTY_STRING_ARRAY);

        TestRuntime runtime = mock(TestRuntime.class);

        when(runtime.getKeyspace()).thenReturn(keyspace);
        when(runtime.getTestSettings()).thenReturn(mock(TestSettings.class));
        when(runtime.getTestSettings().getKeyspaceSettings()).thenReturn(keyspaceSettings);
        when(runtime.getTestSettings().getConnectSettings()).thenReturn(new BasicConnectSettings());

        return runtime;
    }
}

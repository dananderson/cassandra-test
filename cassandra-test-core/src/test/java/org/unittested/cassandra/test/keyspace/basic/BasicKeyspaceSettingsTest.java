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

package org.unittested.cassandra.test.keyspace.basic;

import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.unittested.cassandra.test.AbstractCassandraTest;
import org.unittested.cassandra.test.TestSettings;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.data.cql.BasicCqlResourceLoader;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;
import org.mockito.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum.*;

@CassandraKeyspace(keyspace = "basic_schema_settings")
@CassandraRollback(afterMethod = RollbackStrategy.NONE, afterClass = RollbackStrategy.DROP)
public class BasicKeyspaceSettingsTest extends AbstractCassandraTest {

    private static final String [] SCHEMA = {
            "create keyspace if not exists  basic_schema_settings with replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} and durable_writes = true",
            "create table basic_schema_settings.a (x int primary key);"
    };

    @DataProvider
    public static Object[][] syncConfiguration() {
        return new Object[][] {
                // keyspace cql changed, expect re-install
                { true, KEYSPACE, false, true, true, false, true, 1, 1, 0 },
                { false, KEYSPACE, false, true, true, false, true, 1, 1, 0 },
                // keyspace cql not changed, expect no schema ops
                { true, KEYSPACE, false, true, false, false, true, 0, 1, 0 },
                { false, KEYSPACE, false, true, false, false, true, 0, 1, 0 },
                // cluster schema changed, expect re-install
                { true, CLUSTER, false, true, false, true, true, 1, 0, 1 },
                { false, CLUSTER, false, true, false, true, true, 1, 0, 1 },
                // cluster schema not changed, expect no schema ops
                { true, CLUSTER, false, true, false, false, true, 0, 0, 1 },
                { false, CLUSTER, false, true, false, false, true, 0, 0, 1 },
                // ignore schema changes, expect no schema ops
                { true, NONE, false, true, true, false, true, 0, 0, 0 },
                { false, NONE, false, true, true, false, true, 0, 0, 0 },
                // registered and keyspace does not exist, expect re-install
                { true, KEYSPACE, true, false, false, false, true, 1, 0, 0 },
                { true, CLUSTER, true, false, false, false, true, 1, 0, 0 },
                { true, NONE, true, false, false, false, true, 1, 0, 0 },
                // registered, auto create disabled and keyspace does not exist, expect re-install
                { false, KEYSPACE, true, false, false, false, true, 1, 0, 0 },
                { false, CLUSTER, true, false, false, false, true, 1, 0, 0 },
                { false, NONE, true, false, false, false, true, 1, 0, 0 },
                // not registered, expect re-install
                { true, KEYSPACE, false, false, true, false, true, 1, 0, 0 },
                { true, CLUSTER, false, false, true, false, true, 1, 0, 0 },
                { true, NONE, false, false, true, false, true, 1, 0, 0 },
                // not registered and auto create disabled
                { false, KEYSPACE, false, false, true, false, true, 1, 0, 0 },
                { false, CLUSTER, false, false, true, false, true, 1, 0, 0 },
                { false, NONE, false, false, true, false, true, 1, 0, 0 },
                // not registered and keyspace does not exist, expect re-install
                { true, KEYSPACE, true, false, false, false, true, 1, 0, 0 },
                { true, CLUSTER, true, false, false, false, true, 1, 0, 0 },
                { true, NONE, true, false, false, false, true, 1, 0, 0 },
                // not registered, auto create disabled and keyspace does not exist, expect re-install
                { false, KEYSPACE, true, false, false, false, true, 1, 0, 0 },
                { false, CLUSTER, true, false, false, false, true, 1, 0, 0 },
                { false, NONE, true, false, false, false, true, 1, 0, 0 },
        };
    }

    @Test(dataProvider = "syncConfiguration")
    public void sync(boolean autoCreateKeyspace,
                     SchemaChangeDetectionEnum schemaChangeDetection,
                     boolean dropKeyspaceBeforeSync,
                     boolean isRegistered,
                     boolean keyspaceSignatureResult,
                     boolean clusterSignatureResult,
                     boolean expectedKeyspaceExists,
                     int expectedTrackCalls,
                     int expectedKeyspaceSignatureCalls,
                     int expectedClusterSignatureCalls) throws Exception {
        // given
        TestRuntime runtime = createRuntime(getKeyspace(), autoCreateKeyspace, schemaChangeDetection);
        KeyspaceStateManager keyspaceStateManager = mock(KeyspaceStateManager.class);

        when(keyspaceStateManager.hasKeyspaceCqlSignatureChanged(
                Matchers.anyInt(), Matchers.anyInt()))
            .thenReturn(keyspaceSignatureResult);
        when(keyspaceStateManager.hasClusterSchemaVersionChanged(
                Matchers.anyInt(), Matchers.any(UUID.class)))
            .thenReturn(clusterSignatureResult);
        when(keyspaceStateManager.isTracked(Matchers.anyInt()))
            .thenReturn(isRegistered);

        if (dropKeyspaceBeforeSync) {
            getKeyspace().drop();
        }

        // when
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);

        // then
        assertThat(getKeyspace().exists(), is(expectedKeyspaceExists));
        verify(keyspaceStateManager, times(expectedTrackCalls)).track(
                Matchers.anyInt(), Matchers.any(UUID.class), Matchers.anyInt());
        verify(keyspaceStateManager, times(expectedClusterSignatureCalls)).hasClusterSchemaVersionChanged(
                Matchers.anyInt(), Matchers.any(UUID.class));
        verify(keyspaceStateManager, times(expectedKeyspaceSignatureCalls)).hasKeyspaceCqlSignatureChanged(
                Matchers.anyInt(), Matchers.anyInt());
        verify(keyspaceStateManager, times(1)).isTracked(Matchers.anyInt());
    }

    @Test
    public void syncNullKeyspace() throws Exception {
        // given
        Keyspace keyspace = new Keyspace(getCluster().connect(), Keyspace.NULL);
        TestRuntime runtime = createRuntime(keyspace, true, KEYSPACE);
        KeyspaceStateManager keyspaceStateManager = mock(KeyspaceStateManager.class);
        when(keyspaceStateManager.hasKeyspaceCqlSignatureChanged(
                Matchers.anyInt(), Matchers.anyInt()))
                .thenReturn(true);

        // when
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);

        // then
        verifyZeroInteractions(keyspaceStateManager);
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void syncWithResourceException() throws Exception {
        // given
        Keyspace keyspace = getKeyspace();
        TestRuntime runtime = createRuntime(keyspace, true, KEYSPACE, ArrayUtils.toArray("file://does_not_exist"));
        KeyspaceStateManager keyspaceStateManager = mock(KeyspaceStateManager.class);

        // when
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);

        // then
        // CassandraTestException
    }

    private TestRuntime createRuntime(Keyspace keyspace,
                                      boolean autoCreateKeyspace,
                                      SchemaChangeDetectionEnum schemaChangeDetection) throws Exception {
        return createRuntime(keyspace, autoCreateKeyspace, schemaChangeDetection, SCHEMA);
    }

    private TestRuntime createRuntime(Keyspace keyspace,
                                      boolean autoCreateKeyspace,
                                      SchemaChangeDetectionEnum schemaChangeDetection,
                                      String [] schema) throws Exception {
        KeyspaceSettings keyspaceSettings = new BasicKeyspaceSettings(
                keyspace.getName(),
                keyspace.isCaseSensitiveName(),
                autoCreateKeyspace,
                schema,
                schemaChangeDetection,
                ArrayUtils.EMPTY_STRING_ARRAY,
                new BasicCqlResourceLoader());

        TestRuntime runtime = mock(TestRuntime.class);

        when(runtime.getKeyspace()).thenReturn(keyspace);
        when(runtime.getTestSettings()).thenReturn(mock(TestSettings.class));
        when(runtime.getTestSettings().getKeyspaceSettings()).thenReturn(keyspaceSettings);
        when(runtime.getTestSettings().getConnectSettings()).thenReturn(new BasicConnectSettings());

        return runtime;
    }
}

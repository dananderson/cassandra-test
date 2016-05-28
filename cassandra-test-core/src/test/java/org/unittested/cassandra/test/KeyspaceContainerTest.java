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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.hamcrest.Matcher;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;

@CassandraKeyspace(keyspace = "keyspace_container_test")
@CassandraRollback(afterMethod = RollbackStrategy.NONE, afterClass = RollbackStrategy.DROP)
public class KeyspaceContainerTest extends AbstractCassandraTest {

    @DataProvider
    public static Object[][] badClusters() {
        Cluster cluster = mock(Cluster.class);
        when(cluster.isClosed()).thenReturn(true);

        return new Object[][] {
                { cluster },
                { null },
        };
    }

    @DataProvider
    public static Object[][] keyspaceMetadata() {
        return new Object[][] {
                { "test", notNullValue() },
                { Keyspace.NULL, nullValue() },
                { null, nullValue() },
        };
    }

    @Test(dataProvider = "keyspaceMetadata")
    public void getKeyspaceMetadata(String keyspace, Matcher<KeyspaceMetadata> expectedValue) throws Exception {
        // given
        KeyspaceContainer keyspaceContainer = new KeyspaceContainer(getCluster());

        // when
        KeyspaceMetadata keyspaceMetadata = keyspaceContainer.getKeyspaceMetadata(keyspace);

        // then
        assertThat(keyspaceMetadata, expectedValue);
    }

    @Test(dataProvider = "badClusters", expectedExceptions = CassandraTestException.class)
    public void getKeyspaceMetaDataWithBadCluster(Cluster cluster) throws Exception {
        // given
        KeyspaceContainer keyspaceContainer = new KeyspaceContainer(cluster);

        // when
        keyspaceContainer.getKeyspaceMetadata("test");

        // then
        // CassandraTestException
    }

    @DataProvider
    public static Object[][] hasKeyspaceData() {
        return new Object[][] {
                { "test", true },
                { Keyspace.NULL, false },
                { null, false },
        };
    }

    @Test(dataProvider = "hasKeyspaceData")
    public void hasKeyspace(String keyspace, boolean expectedResult) throws Exception {
        // given
        KeyspaceContainer keyspaceContainer = new KeyspaceContainer(getCluster());

        // when
        boolean result = keyspaceContainer.hasKeyspace(keyspace);

        // then
        assertThat(result, is(expectedResult));
    }

    @Test(dataProvider = "badClusters", expectedExceptions = CassandraTestException.class)
    public void hasKeyspaceWithBadCluster(Cluster cluster) throws Exception {
        // given
        KeyspaceContainer keyspaceContainer = new KeyspaceContainer(cluster);

        // when
        keyspaceContainer.hasKeyspace("test");

        // then
        // CassandraTestException
    }

    @Test
    public void getClusterTest() throws Exception {
        // given
        KeyspaceContainer keyspaceContainer = new KeyspaceContainer(getCluster());

        // when
        Cluster cluster = keyspaceContainer.getCluster();

        // then
        assertThat(cluster, is(getCluster()));
    }

    @DataProvider
    public static Object[][] clusters() {
        Cluster cluster = mock(Cluster.class);
        Cluster clusterExceptionOnClose = mock(Cluster.class);

        doThrow(new RuntimeException()).when(clusterExceptionOnClose).close();

        return new Object[][] {
                { cluster },
                { clusterExceptionOnClose },
        };
    }

    @Test(dataProvider = "clusters")
    public void close(Cluster cluster) throws Exception {
        // given
        KeyspaceContainer keyspaceContainer = new KeyspaceContainer(cluster);

        // when
        keyspaceContainer.close();

        // then
        verify(cluster, times(1)).close();
        assertThat(keyspaceContainer.getCluster(), nullValue());
    }
}

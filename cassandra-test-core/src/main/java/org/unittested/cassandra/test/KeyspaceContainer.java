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

import org.unittested.cassandra.test.exception.CassandraTestException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;

/**
 * Cluster operations.
 */
public class KeyspaceContainer {

    private Cluster cluster;

    public KeyspaceContainer(final Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Get this {@link Cluster}.
     *
     * @return {@link Cluster}
     */
    public Cluster getCluster() {
        return this.cluster;
    }

    /**
     * Does a keyspace exist in this cluster?
     *
     * @param name Keyspace name.
     * @return {@link Boolean}
     */
    public boolean keyspaceExists(String name) {
        return (getKeyspaceMetadata(name) != null);
    }

    /**
     * Close this cluster.
     */
    public void close() {
        try {
            this.cluster.close();
        } catch (Exception e) {
            // LOG.warn("Failed to close cluster.");
        }

        this.cluster = null;
    }

    KeyspaceMetadata getKeyspaceMetadata(String name) {
        if (this.cluster == null || this.cluster.isClosed()) {
            throw new CassandraTestException("Connection is closed. Cannot get keyspace info.");
        }

        if (name == null || name.isEmpty()) {
            return null;
        }

        return this.cluster.getMetadata().getKeyspace(name);
    }
}

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

package org.unittested.cassandra.test.keyspace.state;

import java.util.UUID;

/**
 * Tracks keyspace schema state across tests.
 *
 * Keyspace schema state is tracked at the cluster and the keyspace. Cluster schema state uses the system.local.schema_version
 * UUID to track changes. If the UUID changes, Cassandra Test assumes that the schema for the test keyspace has changed.
 * Even though this change detection produces false positives, it is sufficient for most test cases. Keyspace schema state
 * uses a hash code of the keyspace schema (exported as a list of CQL strings). If the UUID changes, Cassandra Test assumes
 * that the schema for the test keyspace has changed.
 * <p>
 * Tests can configure which keyspace schema change method to use in keyspace setting annotations.
 */
public interface KeyspaceStateManager {

    /**
     * Records schema information associated with a keyspace.
     *
     * @param key Hash code of keyspace settings.
     * @param clusterSchemaVersion The current schema_version UUID of the Cassandra cluster.
     * @param keyspaceCqlSignature The current hash code of keyspace schema.
     */
    void track(Integer key, UUID clusterSchemaVersion, Integer keyspaceCqlSignature);

    /**
     * Checks if {@link #track(Integer, UUID, Integer)} has been called for the keyspace.
     *
     * @param key Hash code of keyspace settings.
     * @return {@link Boolean}
     */
    boolean isTracked(Integer key);

    /**
     * Checks if the cluster schema_version has changed.
     *
     * @param key Hash code of keyspace settings.
     * @param clusterSchemaVersion The current schema_version UUID of the Cassandra cluster.
     * @return {@link Boolean}
     */
    boolean hasClusterSchemaVersionChanged(Integer key, UUID clusterSchemaVersion);

    /**
     * Checks if the keyspace schema has changed.
     *
     * @param key Hash code of keyspace settings.
     * @param keyspaceSchemaSignature The current hash code of keyspace schema.
     * @return {@link Boolean}
     */
    boolean hasKeyspaceCqlSignatureChanged(Integer key, Integer keyspaceSchemaSignature);
}

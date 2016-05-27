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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasicKeyspaceStateManager implements KeyspaceStateManager {

    private Map<Integer, SchemaState> schemaStateMap;

    public BasicKeyspaceStateManager() {
        this.schemaStateMap = new HashMap<Integer, SchemaState>();
    }

    @Override
    public void track(Integer key, UUID clusterSchemaVersion, Integer keyspaceCqlSignature) {
        this.schemaStateMap.put(key, new SchemaState(keyspaceCqlSignature, clusterSchemaVersion));
    }

    @Override
    public boolean isTracked(final Integer key) {
        return this.schemaStateMap.containsKey(key);
    }

    @Override
    public boolean hasClusterSchemaVersionChanged(Integer key, UUID latestClusterSchemaVersion) {
        SchemaState schemaState = this.schemaStateMap.get(key);
        return schemaState == null || !schemaState.getClusterSchemaVersion().equals(latestClusterSchemaVersion);
    }

    @Override
    public boolean hasKeyspaceCqlSignatureChanged(Integer key, Integer latestKeyspaceSchemaSignature) {
        SchemaState schemaState = this.schemaStateMap.get(key);
        return schemaState == null || !schemaState.getKeyspaceSchemaSignature().equals(latestKeyspaceSchemaSignature);
    }

    private static class SchemaState {
        private final Integer keyspaceSchemaSignature;
        private final UUID clusterSchemaVersion;

        public SchemaState(Integer keyspaceSchemaSignature, UUID clusterSchemaVersion) {
            this.keyspaceSchemaSignature = keyspaceSchemaSignature;
            this.clusterSchemaVersion = clusterSchemaVersion;
        }

        public Integer getKeyspaceSchemaSignature() {
            return this.keyspaceSchemaSignature;
        }

        public UUID getClusterSchemaVersion() {
            return this.clusterSchemaVersion;
        }
    }
}

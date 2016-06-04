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

package org.unittested.cassandra.test.keyspace.importer;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.util.Utils;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.keyspace.AbstractKeyspaceSettings;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;

public class ImportKeyspaceSettings extends AbstractKeyspaceSettings {

    private int hashCode;
    private SchemaChangeDetectionEnum schemaChangeDetection;

    public ImportKeyspaceSettings(String keyspace,
                                  boolean isCaseSensitiveKeyspace,
                                  SchemaChangeDetectionEnum schemaChangeDetection,
                                  String[] protectedTables) {
        super(keyspace, isCaseSensitiveKeyspace, false, protectedTables);

        if (keyspace.isEmpty()) {
            throw new CassandraTestException("CassandraImportKeyspace requires a keyspace.");
        }

        this.schemaChangeDetection = schemaChangeDetection;
        this.hashCode = new HashCodeBuilder(17, 37)
                .append(keyspace)
                .append(isCaseSensitiveKeyspace)
                .append(schemaChangeDetection.name())
                .append(protectedTables)
                .hashCode();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public void sync(final TestRuntime runtime, KeyspaceStateManager keyspaceStateManager) {
        if (!runtime.getKeyspace().exists()) {
            throw new CassandraTestException("Import keyspace does not exists!");
        }

        Keyspace keyspace = runtime.getKeyspace();
        Integer key = runtime.getTestSettings().getKeyspaceSettings().hashCode();

        if (!keyspaceStateManager.isTracked(key)) {
            keyspaceStateManager.track(
                    key,
                    Utils.getSchemaVersion(keyspace.getSession()),
                    keyspace.getSchemaSignature());
        } else {
            boolean changed;

            switch(this.schemaChangeDetection) {
                case CLUSTER:
                    changed = keyspaceStateManager.hasClusterSchemaVersionChanged(key, Utils.getSchemaVersion(keyspace.getSession()));
                    break;
                case KEYSPACE:
                    changed = keyspaceStateManager.hasKeyspaceCqlSignatureChanged(key, keyspace.getSchemaSignature());
                    break;
                case NONE:
                    changed = false;
                    break;
                default:
                    throw new CassandraTestException("Unsupported SchemaChangeDetection = " + this.schemaChangeDetection);
            }

            if (changed) {
                throw new CassandraTestException("Immutable schema has been modified.");
            }
        }
    }
}

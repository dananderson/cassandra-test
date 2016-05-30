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

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.util.Utils;
import org.unittested.cassandra.test.data.cql.BasicCqlSourceLoader;
import org.unittested.cassandra.test.data.cql.CqlSourceLoader;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.keyspace.AbstractKeyspaceSettings;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicKeyspaceSettings extends AbstractKeyspaceSettings {

    private static final Logger LOG = LoggerFactory.getLogger(BasicKeyspaceSettings.class);

    private String [] schema;
    private boolean autoCreateKeyspace;
    private SchemaChangeDetectionEnum schemaChangeDetection;
    private CqlSourceLoader cqlSourceLoader;
    private int hashCode;

    public BasicKeyspaceSettings() {
        this(Keyspace.NULL, false, false, ArrayUtils.EMPTY_STRING_ARRAY, SchemaChangeDetectionEnum.NONE,
                ArrayUtils.EMPTY_STRING_ARRAY, new BasicCqlSourceLoader());
    }

    public BasicKeyspaceSettings(String keyspace,
                                 boolean isCaseSensitiveKeyspace,
                                 boolean autoCreateKeyspace,
                                 String[] schema,
                                 SchemaChangeDetectionEnum schemaChangeDetection,
                                 String[] protectedTables,
                                 CqlSourceLoader cqlSourceLoader) {
        super(keyspace, isCaseSensitiveKeyspace, true, protectedTables);
        this.schema = schema;
        this.autoCreateKeyspace = autoCreateKeyspace;
        this.schemaChangeDetection = schemaChangeDetection;
        this.cqlSourceLoader = cqlSourceLoader;
        this.hashCode = new HashCodeBuilder(17, 37)
                .append(keyspace)
                .append(isCaseSensitiveKeyspace)
                .append(autoCreateKeyspace)
                .append(schema)
                .append(schemaChangeDetection.name())
                .append(protectedTables)
                .toHashCode();
    }

    @Override
    public void sync(TestRuntime runtime, KeyspaceStateManager keyspaceStateManager) {
        Keyspace keyspace = runtime.getKeyspace();

        if (keyspace.isNull()) {
            return;
        }

        Integer key = runtime.getTestSettings().getKeyspaceSettings().hashCode();
        boolean installSchema = false;

        if (!keyspaceStateManager.isTracked(key)) {
            keyspace.dropIfExists();
            installSchema = true;
        } else if (!keyspace.exists()) {
            installSchema = true;
        } else if (this.schemaChangeDetection.equals(SchemaChangeDetectionEnum.CLUSTER)) {
            UUID schemaVesion = Utils.getSchemaVersion(keyspace.getSession());
            if (keyspaceStateManager.hasClusterSchemaVersionChanged(key, schemaVesion)) {
                keyspace.drop();
                installSchema = true;
            }
        } else if (this.schemaChangeDetection.equals(SchemaChangeDetectionEnum.KEYSPACE)) {
            Integer keyspaceSchemaSignature = keyspace.getSchemaSignature();
            if (keyspaceStateManager.hasKeyspaceCqlSignatureChanged(key, keyspaceSchemaSignature)) {
                keyspace.drop();
                installSchema = true;
            }
        } else if (this.schemaChangeDetection.equals(SchemaChangeDetectionEnum.NONE)) {
            installSchema = false;
        } else {
            throw new CassandraTestException("Unsupported SchemaChangeDetection = %s", this.schemaChangeDetection);
        }

        if (!keyspace.exists() && this.autoCreateKeyspace) {
            keyspace.create();
        }

        keyspace.use();

        if (installSchema) {
            for (String cqlSource : this.schema) {
                LOG.trace("Loading cql source.");
                try {
                    this.cqlSourceLoader.loadCqlSource(runtime, cqlSource);
                } catch (IOException e) {
                    throw new CassandraTestException("Bad schema: %s", cqlSource, e);
                }
            }

            keyspaceStateManager.track(
                    key,
                    Utils.getSchemaVersion(keyspace.getSession()),
                    keyspace.getSchemaSignature());

            keyspace.use();
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}

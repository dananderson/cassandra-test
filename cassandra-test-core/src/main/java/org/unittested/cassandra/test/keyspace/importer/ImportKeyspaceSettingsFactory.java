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

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.annotation.CassandraImportKeyspace;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertyResolver;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;

public class ImportKeyspaceSettingsFactory implements KeyspaceSettingsFactory {

    @Override
    public KeyspaceSettings create(final Annotation annotation, final PropertyResolver propertyResolver) {
        if (!(annotation instanceof CassandraImportKeyspace)) {
            throw new CassandraTestException("Expected annotation of type @CassandraImportKeyspace, but got %s", annotation);
        }

        CassandraImportKeyspace schema = (CassandraImportKeyspace)annotation;

        return new ImportKeyspaceSettings(
                propertyResolver.resolve(schema.value()),
                Boolean.parseBoolean(propertyResolver.resolve(schema.isCaseSensitiveKeyspace())),
                SchemaChangeDetectionEnum.valueOf(propertyResolver.resolve(schema.schemaChangeDetection().toUpperCase())),
                propertyResolver.resolve(schema.protectedTables()));
    }
}

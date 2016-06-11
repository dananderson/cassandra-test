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

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.data.cql.BasicCqlResourceLoader;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertyResolver;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;

public class BasicKeyspaceSettingsFactory implements KeyspaceSettingsFactory {

    public BasicKeyspaceSettingsFactory() {

    }

    @Override
    public KeyspaceSettings create(Annotation annotation, PropertyResolver propertyResolver) {
        if (!(annotation instanceof CassandraKeyspace)) {
            throw new CassandraTestException("Expected annotation of type @CassandraKeyspace, but got %s", annotation);
        }

        CassandraKeyspace cassandraKeyspace = (CassandraKeyspace)annotation;

        return new BasicKeyspaceSettings(
                propertyResolver.resolveReferences(cassandraKeyspace.value()),
                Boolean.parseBoolean(propertyResolver.resolveReferences(cassandraKeyspace.isCaseSensitiveKeyspace())),
                Boolean.parseBoolean(propertyResolver.resolveReferences(cassandraKeyspace.autoCreateKeyspace())),
                propertyResolver.resolveReferences(cassandraKeyspace.schema()),
                SchemaChangeDetectionEnum.valueOf(propertyResolver.resolveReferences(cassandraKeyspace.schemaChangeDetection())),
                propertyResolver.resolveReferences(cassandraKeyspace.protectedTables()),
                new BasicCqlResourceLoader());
    }
}

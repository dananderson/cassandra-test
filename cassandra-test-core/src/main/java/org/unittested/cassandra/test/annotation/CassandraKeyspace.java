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

package org.unittested.cassandra.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.SchemaChangeDetection;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;
import org.unittested.cassandra.test.keyspace.basic.BasicKeyspaceSettingsFactory;

/**
 * Test keyspace that is under the control of Cassandra Test.
 *
 * Cassandra Test may drop, create and install schema for the test keyspace as necessary. This keyspace annotation is
 * a good choice if the keyspace schema will be defined in the test environment.
 * <p>
 * Before a test method runs, the test keyspace schema is checked for changes. If changes are detected, the test keyspace
 * is dropped, re-created and schema is installed. Schema can change if the test method alters the schema or
 * {@link CassandraRollback} drops the keyspace. In conjunction with CassandraRollback settings, the keyspace schema
 * integrity ensures each test method runs against consistent Cassandra state.
 * <p>
 * All settings in this annotation can be defined inline or by property references in the form of ${property.name}. Property
 * names are resolved with the {@link org.unittested.cassandra.test.property.PropertyResolver} configured in
 * {@link org.unittested.cassandra.test.TestEnvironmentAdapter}. Property references are the way to avoid hard coding
 * keyspace and connection settings in annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraKeyspace {

    /**
     * The test keyspace.
     *
     * @return The test keyspace.
     */
    String keyspace();

    /**
     * Is the keyspace ID case sensitive?
     * <p>
     * Supported Values: "true", "false"
     *
     * @return {@link Boolean}
     */
    String isCaseSensitiveKeyspace() default "false";

    /**
     * Creates the test keyspace.
     * <p>
     * If true, Cassandra Test will create the test keyspace with a replication_factor = 1, durable_writes = true and
     * default keyspace properties.
     * <p>
     * If false, a create keyspace CQL statement for the test keyspace must appear in the schema property.
     *
     * @return {@link Boolean}
     */
    String autoCreateKeyspace() default "true";

    /**
     * Schema data source list.
     * <p>
     * Each schema data source can be a CQL statement, semi-colon (;) delimited set of CQL statements or a schema data
     * source locator. The schema data source locator takes the form of protocol.contentType: {value}.
     * <p>
     * Supported Protocols
     * <ul>
     *     <li>string - String literal.</li>
     *     <li>file - Path to file on the filesystem.</li>
     *     <li>classpath - Path to file in the classpath.</li>
     * </ul>
     * <p>
     * Supported Content Types
     * <ul>
     *     <li>cql - Semi-colon (;) delimited set of CQL statements.</li>
     * </ul>
     * <p>
     * CQL statements should be limited to schema creation or altering. It is recommended that data related CQL statements
     * go into {@link CassandraData} data sources.
     *
     * @return Schema data source list.
     */
    String [] schema() default {};

    /**
     * Schema change detection method.
     * <p>
     * Supported Values: String values in {@link SchemaChangeDetection}
     *
     * @return {@link org.unittested.cassandra.test.keyspace.SchemaChangeDetectionEnum}
     */
    String schemaChangeDetection() default SchemaChangeDetection.KEYSPACE;

    /**
     * Tables that should never be truncated on a rollback.
     *
     * @return List of protected tables.
     */
    String [] protectedTables() default {};

    /**
     * PRIVATE - DO NOT SET.
     * <p>
     * Binds this annotation to a {@link KeyspaceSettings}, allowing for custom
     * keyspace settings annotations.
     *
     * @return {@link KeyspaceSettingsFactory} class.
     */
    Class<? extends KeyspaceSettingsFactory> __keyspaceSettingsFactory() default BasicKeyspaceSettingsFactory.class;
}

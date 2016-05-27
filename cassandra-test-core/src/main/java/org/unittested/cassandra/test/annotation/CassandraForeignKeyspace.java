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
import org.unittested.cassandra.test.keyspace.foreign.ForeignKeyspaceSettingsFactory;

/**
 * Test keyspace that has been setup outside of the Cassandra Test environment.
 *
 * This keyspace is not under the control of Cassandra Test. Cassandra Test is not allowed to drop this keyspace and the
 * tests are not allowed to alter the keyspace schema. If a test alters the keyspace schema, the tests will fail with a
 * {@link org.unittested.cassandra.test.exception.CassandraTestException}. This annotation is a good choice for test
 * environments that setup a keyspace and schema before running tests.
 * <p>
 * All settings in this annotation can be defined inline or by property references in the form of ${property.name}. Property
 * names are resolved with the {@link org.unittested.cassandra.test.property.PropertyResolver} configured in
 * {@link org.unittested.cassandra.test.TestEnvironmentAdapter}. Property references are the way to avoid hard coding
 * keyspace and connection settings in annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraForeignKeyspace {

    /**
     * The test keyspace.
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
    Class<? extends KeyspaceSettingsFactory> __keyspaceSettingsFactory() default ForeignKeyspaceSettingsFactory.class;
}

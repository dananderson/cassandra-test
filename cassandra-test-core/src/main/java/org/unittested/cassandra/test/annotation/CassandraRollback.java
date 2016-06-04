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

import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.unittested.cassandra.test.rollback.RollbackSettingsFactory;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettingsFactory;

/**
 * Cleans up the test keyspace after a test runs.
 *
 * Cassandra Test approximates rollback behavior, as Cassandra does not have the concept of transactions or the native
 * ability to undo mutating operations. The rollback behavior is to truncate tables or drop the test keyspace after
 * a test method runs, effectively undoing any mutations performed by the test. Before the next test runs, Cassandra
 * Test ensures the schema is in a known state and imports any data, allowing the next test to run against a known
 * data set.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraRollback {

    /**
     * List of tables in the test keyspace that should be included when a {@link RollbackStrategy#TRUNCATE} is specified.
     * <p>
     * If tableInclusions is set, tableExclusions must be empty.
     * <p>
     * The test keyspace is specified by the schema annotation, including {@link CassandraKeyspace},
     * {@link CassandraImportKeyspace}, etc.
     *
     * @return List of tables to truncate.
     */
    String [] tableInclusions() default {};

    /**
     * List of tables in the test keyspace that should not be included when a {@link RollbackStrategy#TRUNCATE} is specified.
     * <p>
     * If tableExclusions is set, tableInclusions must be empty.
     * <p>
     * The test keyspace is specified by the schema annotation, including {@link CassandraKeyspace},
     * {@link CassandraImportKeyspace}, etc.
     *
     * @return List of tables to exclude from truncation.
     */
    String [] tableExclusions() default {};

    /**
     * The {@link RollbackStrategy} to execute after each test method in the test class.
     *
     * @return {@link RollbackStrategy}
     */
    RollbackStrategy afterMethod() default RollbackStrategy.TRUNCATE;

    /**
     * The {@link RollbackStrategy} to execute after all test methods in the test class have run.
     *
     * @return {@link RollbackStrategy}
     */
    RollbackStrategy afterClass() default RollbackStrategy.NONE;

    /**
     * PRIVATE - DO NOT SET.
     * <p>
     * Binds this annotation to a {@link org.unittested.cassandra.test.rollback.RollbackSettings}, allowing for custom
     * rollback settings annotations.
     *
     * @return {@link RollbackSettingsFactory} class.
     */
    Class<? extends RollbackSettingsFactory> __rollbackSettingsFactory() default BasicRollbackSettingsFactory.class;
}

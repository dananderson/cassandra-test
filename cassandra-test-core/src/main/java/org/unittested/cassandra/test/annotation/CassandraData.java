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

import org.unittested.cassandra.test.data.DataSettingsFactory;
import org.unittested.cassandra.test.data.basic.BasicDataSettingsFactory;

/**
 * Populates Cassandra tables from a list of data sources.
 *
 * Data sources will be executed before each test method in the test class to ensure that each test runs against a
 * consistent data set. When a test method is finished, {@link CassandraRollback} clears any data populated by
 * CassandraData or the test run itself.
 * <p>
 * All settings in this annotation can be defined inline or by property references in the form of ${property.name}. Property
 * names are resolved with the {@link org.unittested.cassandra.test.properties.PropertyResolver} configured in
 * {@link org.unittested.cassandra.test.TestEnvironmentAdapter}. Property references are the way to avoid hard coding
 * keyspace and connection settings in annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraData {

    /**
     * Data source list.
     * <p>
     * Each data source string entry can be either CQL statements or a URL pointing to a text file with CQL statements.
     * The format for the CQL statements is similar to cql files accepted by cqlsh. {@link org.unittested.cassandra.test.resource.Resource.ContentType#CQL}
     * contains more info on the format.
     * <p>
     * CQL statements in CassandraData should be limited to data insertion (INSERT, BATCH, USE, etc are OK). Schema
     * altering statements should appear in CassandraKeyspace schema.
     * <p>
     * Example Data Source Strings
     * <ul>
     *     <li>INSERT INTO x(id) VALUES (1);</li>
     *     <li>INSERT INTO x(id) VALUES (1);INSERT INTO x(id) VALUES (2);</li>
     *     <li>file:data.cql</li>
     *     <li>file://data.cql</li>
     *     <li>classpath:data.cql</li>
     *     <li>classpath://data.cql</li>
     * </ul>
     *
     * @return Data source list.
     */
    String [] data();

    /**
     * PRIVATE - DO NOT SET.
     * <p>
     * Binds this annotation to a {@link org.unittested.cassandra.test.data.DataSettings}, allowing for custom
     * data settings annotations.
     *
     * @return {@link DataSettingsFactory} class.
     */
    Class<? extends DataSettingsFactory> __dataSettingsFactory() default BasicDataSettingsFactory.class;
}

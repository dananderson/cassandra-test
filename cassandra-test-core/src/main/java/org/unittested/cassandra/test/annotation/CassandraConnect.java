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

import org.unittested.cassandra.test.connect.ConnectSettingsFactory;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettingsFactory;

/**
 * Connection settings.
 *
 * Specifies how the {@link com.datastax.driver.core.Cluster} and {@link com.datastax.driver.core.Session} driver
 * connection objects will be configured for this test.
 * <p>
 * All settings in this annotation can be defined inline or by property references in the form of ${property.name}. Property
 * names are resolved with the {@link org.unittested.cassandra.test.property.PropertyResolver} configured in
 * {@link org.unittested.cassandra.test.TestEnvironmentAdapter}. Property references are the way to avoid hard coding
 * keyspace and connection settings in annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraConnect {

    /**
     * Cassandra node address(es).
     * <p>
     * Comma separated lists of addresses are accepeted.
     *
     * @return Cassandra node address(es).
     */
    String [] host() default {};

    /**
     * Cassandra binary port.
     *
     * @return {@link Integer}
     */
    String port() default "9042";

    /**
     * Cassandra username.
     * <p>
     * If the username is not specified, no authentication will be used to connect to Cassandra.
     *
     * @return Username.
     */
    String username() default "";

    /**
     * Cassandra password.
     *
     * @return Password.
     */
    String password() default "";

    /**
     * PRIVATE - DO NOT SET.
     * <p>
     * Binds this annotation to a {@link org.unittested.cassandra.test.connect.ConnectSettings}, allowing for custom
     * connect settings annotations.
     *
     * @return {@link ConnectSettingsFactory} class.
     */
    Class<? extends ConnectSettingsFactory> __connectSettingsFactory() default BasicConnectSettingsFactory.class;
}

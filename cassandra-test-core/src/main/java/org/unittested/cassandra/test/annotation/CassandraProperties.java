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

import org.unittested.cassandra.test.resource.Resource;

/**
 * Properties file to load.
 *
 * Properties are primarily used to resolve property references in Cassandra Test annotation values. Property references
 * take the form of ${property.name} embedded in a string. Property references are supported in annotation values to
 * avoid hard coding connection and keyspace information in annotations.
 * <p>
 * If CassandraProperties is not specified by a test, properties will be loaded from /cassandra-test.properties if it
 * exists in the classpath.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraProperties {

    /**
     * Properties file URL.
     *
     * @return Properties file URL.
     * @see Resource
     */
    String value();
}

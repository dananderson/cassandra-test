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
 * Specify a properties file to resolve property references in Cassandra Test annotations.
 *
 * If CassandraProperties is not set, default properties will be selected by the test environment. For most environments,
 * System properties are used as the default property set.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CassandraProperties {

    /**
     * URL pointing to a Java properties file resource.
     * <p>
     * Accepted URL formats defined in {@link Resource}
     * <p>
     * Example URLs
     * <ul>
     *     <li>classpath:cassandra.properties</li>
     *     <li>classpath//:cassandra.properties</li>
     *     <li>file:cassandra.properties</li>
     *     <li>file//:cassandra.properties</li>
     *     <li>classpath:my%20test%20data.properties</li>
     *     <li>classpath:my test data.properties (spaces OK)</li>
     * </ul>
     *
     * @return URL pointing to a Java properties file resource.
     */
    String value();
}

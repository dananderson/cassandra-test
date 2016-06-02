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
     * Property file locator.
     * <p>
     * Locator is in the {@link org.unittested.cassandra.test.io.Locator} format used by Cassandra Test to reference
     * files.
     * <p>
     * Example locator for a classpath properties file, classpath:props.properties, and a filesystem properties
     * file, file:path/props.properties.
     *
     * @return Property file locator.
     */
    String value();
}

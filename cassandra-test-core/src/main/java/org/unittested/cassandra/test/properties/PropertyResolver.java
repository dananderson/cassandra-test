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

package org.unittested.cassandra.test.properties;

/**
 * Resolves property references in Cassandra Test annotations.
 *
 * Cassandra Test annotation values can contain property references in the form of ${property.name}. A PropertyResolver
 * identifies property references and the implementation resolves the property to a string value.
 * <p>
 * Custom PropertyResolver implementations can be registered with {@link org.unittested.cassandra.test.TestEnvironmentAdapter}.
 */
public interface PropertyResolver {

    /**
     * Resolves any property references in the given annotationValue.
     * <p>
     * If a property references does not exist, annotationValue is returned unaltered.
     *
     * @param annotationValue Raw annotation string value.
     * @return Property value or input.
     */
    String resolve(String annotationValue);

    /**
     * Resolve a list of annotation values.
     *
     * @param annotationValue Raw annotation string array value.
     * @return Property values or input for each array item.
     */
    String [] resolve(String [] annotationValue);
}

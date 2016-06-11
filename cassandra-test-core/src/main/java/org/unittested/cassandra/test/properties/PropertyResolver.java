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
 * Interface for resolving properties.
 *
 * This interface is primarily used for resolving property references in Cassandra Test annotation values. Property references
 * are in the form of ${property.name}.
 * <p>
 * Custom PropertyResolver implementations can be registered with {@link org.unittested.cassandra.test.TestSettingsBuilder}.
 */
public interface PropertyResolver {

    /**
     * Resolves any property references in the given string.
     * <p>
     * If a property references does not exist, text is returned unaltered.
     *
     * @param text String to resolve.
     * @return Property value or input.
     */
    String resolveReferences(String text);

    /**
     * Resolve a list of string values.
     *
     * @param text String to resolve.
     * @return Property values or input for each array item.
     */
    String [] resolveReferences(String[] text);

    /**
     * Get a property by name.
     *
     * @param propertyName Property name.
     * @param defaultValue If the property does not exist, this value is returned.
     * @return Value for propertyName.
     */
    String getProperty(String propertyName, String defaultValue);
}

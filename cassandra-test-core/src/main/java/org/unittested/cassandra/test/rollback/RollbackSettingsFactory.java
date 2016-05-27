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

package org.unittested.cassandra.test.rollback;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.property.PropertyResolver;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettings;

/**
 * Creates a {@link RollbackSettings} instance from a rollback annotation.
 */
public interface RollbackSettingsFactory {

    /**
     * Create a new {@link RollbackSettings} instance.
     *
     * @param annotation An annotation supported by this class. If the annotation type is not supported, a
     *                   {@link org.unittested.cassandra.test.exception.CassandraTestException} is thrown.
     * @param propertyResolver Resolves property references in annotation strings.
     * @return {@link RollbackSettings}
     */
    BasicRollbackSettings create(Annotation annotation, PropertyResolver propertyResolver);
}

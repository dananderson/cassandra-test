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

package org.unittested.cassandra.test.rollback.basic;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertyResolver;
import org.unittested.cassandra.test.rollback.RollbackSettingsFactory;

public class BasicRollbackSettingsFactory implements RollbackSettingsFactory {

    @Override
    public BasicRollbackSettings create(Annotation annotation, PropertyResolver propertyResolver) {
        if (!(annotation instanceof CassandraRollback)) {
            throw new CassandraTestException("Expected annotation of type @CassandraRollback, but got " + annotation);
        }

        CassandraRollback cassandraRollback = (CassandraRollback)annotation;

        return new BasicRollbackSettings(
                cassandraRollback.tableInclusions(),
                cassandraRollback.tableExclusions(),
                cassandraRollback.afterMethod(),
                cassandraRollback.afterClass());
    }
}

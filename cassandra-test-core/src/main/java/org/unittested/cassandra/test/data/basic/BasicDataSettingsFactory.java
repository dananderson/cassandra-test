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

package org.unittested.cassandra.test.data.basic;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.DataSettingsFactory;
import org.unittested.cassandra.test.data.cql.BasicCqlSourceLoader;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertyResolver;

/**
 * Create a {@link DataSettings} from a {@link CassandraData} annotation.
 */
public class BasicDataSettingsFactory implements DataSettingsFactory {

    @Override
    public DataSettings create(final Annotation annotation, final PropertyResolver propertyResolver) {
        if (!(annotation instanceof CassandraData)) {
            throw new CassandraTestException("Expected annotation of type @CassandraData, but got %s", annotation);
        }

        CassandraData cassandraData = (CassandraData)annotation;

        return new BasicDataSettings(cassandraData.data(), new BasicCqlSourceLoader());
    }
}

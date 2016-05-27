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

package org.unittested.cassandra.test.connect.basic;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.util.Utils;
import org.unittested.cassandra.test.annotation.CassandraConnect;
import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.connect.ConnectSettingsFactory;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.property.PropertyResolver;

public class BasicConnectSettingsFactory implements ConnectSettingsFactory {

    public BasicConnectSettingsFactory() {

    }

    @Override
    public ConnectSettings create(Annotation annotation, PropertyResolver propertyResolver) {
        if (!(annotation instanceof CassandraConnect)) {
            throw new CassandraTestException("Expected annotation of type @CassandraConnect, but got %s", annotation);
        }

        CassandraConnect cassandraConnect = (CassandraConnect)annotation;

        return new BasicConnectSettings(
                Utils.expandCommaDelimitedEntries(propertyResolver.resolve(cassandraConnect.host())),
                Integer.parseInt(propertyResolver.resolve(cassandraConnect.port())),
                propertyResolver.resolve(cassandraConnect.username()),
                propertyResolver.resolve(cassandraConnect.password()));
    }
}

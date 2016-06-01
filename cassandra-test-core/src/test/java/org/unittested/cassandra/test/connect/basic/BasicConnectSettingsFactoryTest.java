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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;

import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;

import org.unittested.cassandra.test.FactoryTestAnnotations;
import org.unittested.cassandra.test.annotation.CassandraConnect;
import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.connect.ConnectSettingsFactory;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.property.system.PropertiesPropertyResolver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.ProtocolOptions;

public class BasicConnectSettingsFactoryTest {

    @Test
    public void create() throws Exception {
        // given
        ConnectSettingsFactory connectSettingsFactory = new BasicConnectSettingsFactory();
        CassandraConnect cassandraConnect = FactoryTestAnnotations.class.getAnnotation(CassandraConnect.class);

        // when
        ConnectSettings connectSettings = connectSettingsFactory.create(cassandraConnect, PropertiesPropertyResolver.SYSTEM);

        // then
        assertThat(connectSettings, instanceOf(BasicConnectSettings.class));
        BasicConnectSettings basicConnectSettings = (BasicConnectSettings)connectSettings;
        assertThat(basicConnectSettings.getClusterBuilder().getContactPoints(),
                contains(new InetSocketAddress("127.0.0.1", ProtocolOptions.DEFAULT_PORT)));
        assertThat(basicConnectSettings.getClusterBuilder().getConfiguration().getProtocolOptions().getAuthProvider(),
                instanceOf(PlainTextAuthProvider.class));
    }

    @DataProvider
    public Object[][] invalidAnnotations() {
        return new Object[][] {
                { null },
                { FactoryTestAnnotations.createStubAnnotation() },
        };
    }

    @Test(dataProvider = "invalidAnnotations", expectedExceptions = CassandraTestException.class)
    public void createWithInvalidAnnotations(Annotation annotation) throws Exception {
        // given
        ConnectSettingsFactory connectSettingsFactory = new BasicConnectSettingsFactory();

        // when
        connectSettingsFactory.create(annotation, PropertiesPropertyResolver.SYSTEM);

        // then
        // expect IllegalArgumentException
    }
}

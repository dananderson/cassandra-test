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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;

public class BasicConnectSettingsTest {

    @DataProvider
    public Object[][] connectSettingsData() {
        return new Object[][] {
                { "0.0.0.0", 9042, "", "",  null },
                { "1.1.1.1", 9042, "", "", null },
                { "0.0.0.0", 9042, "admin", "password", PlainTextAuthProvider.class },
        };
    }

    @Test(dataProvider = "connectSettingsData")
    public void newInstance(String host,
                                int port,
                                String user,
                                String pass,
                                Class<? extends AuthProvider> authProviderClass) throws Exception {
        // given
        // data provider

        // when
        ConnectSettings connectSettings = new BasicConnectSettings(host, port, user, pass);

        // then
        Cluster.Builder builder = connectSettings.getClusterBuilder();
        assertThat(builder.getContactPoints(), hasSize(1));
        assertThat(builder.getContactPoints().get(0).getAddress().getHostAddress(), is(host));
        assertThat(builder.getContactPoints().get(0).getPort(), is(port));
        if (authProviderClass != null) {
            assertThat(builder.getConfiguration().getProtocolOptions().getAuthProvider(), instanceOf(authProviderClass));
        } else {
            assertThat(builder.getConfiguration().getProtocolOptions().getAuthProvider(), is(AuthProvider.NONE));
        }
    }

    @Test
    public void connect() throws Exception {
        // given
        ConnectSettings connectSettings = new BasicConnectSettings();

        // when
        Session session = connectSettings.connect();

        // then
        try {
            assertThat(session, notNullValue());
        } finally {
            session.getCluster().close();
        }
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void connectFailure() throws Exception {
        // given
        ConnectSettings connectSettings = new BasicConnectSettings("1.1.1.1", 1111, "", "");

        // when
        connectSettings.connect();

        // then
        // CassandraTestException
    }
}

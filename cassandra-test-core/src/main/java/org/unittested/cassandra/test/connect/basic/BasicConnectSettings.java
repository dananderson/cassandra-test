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

import java.net.InetAddress;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.unittested.cassandra.test.connect.AbstractConnectSettings;
import org.unittested.cassandra.test.util.Utils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.QueryOptions;

public class BasicConnectSettings extends AbstractConnectSettings {

    private String[] host;
    private int port;
    private String username;
    private String password;

    public BasicConnectSettings() {
        this(ArrayUtils.EMPTY_STRING_ARRAY, ProtocolOptions.DEFAULT_PORT, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public BasicConnectSettings(String host, int port, String username, String password) {
        this(new String [] { host }, port, username, password);
    }

    public BasicConnectSettings(String [] host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public Cluster.Builder getClusterBuilder() {
        Cluster.Builder builder = Cluster.builder();

        if (this.host.length == 0 || (this.host.length == 1 && this.host[0].isEmpty())) {
            builder.addContactPoints(InetAddress.getLoopbackAddress());
        } else if (this.host.length > 0) {
            builder.addContactPoints(this.host);
        }

        if (this.port > 0) {
            builder.withPort(this.port);
        }

        if (!this.username.isEmpty()) {
            builder.withCredentials(this.username, this.password);
        }

        QueryOptions queryOptions = Utils.setRefreshSchemaIntervalMillis(new QueryOptions(), 0);

        return builder.withQueryOptions(queryOptions);
    }
}

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

package org.unittested.cassandra.test;

import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.basic.BasicDataSettings;
import org.unittested.cassandra.test.rollback.RollbackSettings;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.basic.BasicKeyspaceSettings;

/**
 * Cassandra Test configuration.
 */
public class TestSettings {

    private final ConnectSettings connectSettings;
    private final DataSettings dataSettings;
    private final RollbackSettings rollbackSettings;
    private final KeyspaceSettings keyspaceSettings;

    public TestSettings() {
        this(new BasicConnectSettings(), new BasicKeyspaceSettings(), new BasicDataSettings(), new BasicRollbackSettings());
    }

    public TestSettings(ConnectSettings connectSettings,
                        KeyspaceSettings keyspaceSettings,
                        DataSettings dataSettings,
                        RollbackSettings rollbackSettings) {
        this.connectSettings = connectSettings;
        this.dataSettings = dataSettings;
        this.rollbackSettings = rollbackSettings;
        this.keyspaceSettings = keyspaceSettings;
    }

    public ConnectSettings getConnectSettings() {
        return this.connectSettings;
    }

    public DataSettings getDataSettings() {
        return this.dataSettings;
    }

    public RollbackSettings getRollbackSettings() {
        return this.rollbackSettings;
    }

    public KeyspaceSettings getKeyspaceSettings() {
        return this.keyspaceSettings;
    }
}

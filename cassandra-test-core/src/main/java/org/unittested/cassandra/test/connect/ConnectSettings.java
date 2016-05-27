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

package org.unittested.cassandra.test.connect;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Cassandra connection settings.
 */
public interface ConnectSettings {

    /**
     * Get connection settings as a {@link com.datastax.driver.core.Cluster.Builder}.
     *
     * @return {@link com.datastax.driver.core.Cluster.Builder}
     */
    Cluster.Builder getClusterBuilder();

    /**
     * Open connection to Cassandra cluster.
     *
     * @return {@link Session}
     */
    Session connect();
}

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

package org.unittested.cassandra.test.keyspace;

import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;

/**
 * Keyspace configuration and management.
 */
public interface KeyspaceSettings {

    /**
     * Keyspace name.
     *
     * @return Keyspace name.
     */
    String getKeyspace();

    /**
     * Tables in this schema that should never be truncated.
     *
     * @return List of tables.
     */
    String [] getProtectedTables();

    /**
     * Is this schema's keyspace droppable?
     *
     * @return Is this schema's keyspace droppable?
     */
    boolean canDropKeyspace();

    /**
     * Hash code of this schema's configuration.
     *
     * @return Hash code.
     */
    int hashCode();

    /**
     * Ensures that this schema configuration is in sync with the currently installed Cassandra schema. If a difference
     * is detected, this method will update the schema or fail the test, depending on this SchemaSetting's behavior.
     *
     * @param runtime Cassandra Test runtime.
     * @param keyspaceStateManager Keyspace schema state manager.
     */
    void sync(TestRuntime runtime, KeyspaceStateManager keyspaceStateManager);
}

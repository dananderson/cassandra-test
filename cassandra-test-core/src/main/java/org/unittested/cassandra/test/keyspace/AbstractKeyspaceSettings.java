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

/**
 * Common code for {@link KeyspaceSettings} implementations.
 */
public abstract class AbstractKeyspaceSettings implements KeyspaceSettings {

    private String keyspace;
    private boolean canDropKeyspace;
    private String [] protectedTables;

    public AbstractKeyspaceSettings(
            String keyspace, boolean
            isCaseSensitiveKeyspace,
            boolean canDropKeyspace,
            String[] protectedTables) {
        this.keyspace = isCaseSensitiveKeyspace ? keyspace : keyspace.toLowerCase();
        this.protectedTables = protectedTables;
        this.canDropKeyspace = canDropKeyspace;
    }

    @Override
    public String getKeyspace() {
        return this.keyspace;
    }

    @Override
    public boolean canDropKeyspace() {
        return this.canDropKeyspace;
    }

    @Override
    public String[] getProtectedTables() {
        return this.protectedTables;
    }
}

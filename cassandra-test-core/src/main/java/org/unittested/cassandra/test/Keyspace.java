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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;

/**
 * Test keyspace operations.
 *
 * Represents the current test keyspace of a Cassandra Test. This class wraps {@link Session} and provides keyspace
 * operations useful to tests.
 */
public class Keyspace {

    /**
     * Name of the null keyspace.
     */
    public static final String NULL = "";

    private KeyspaceContainer container;
    private Session session;
    private String name;

    public Keyspace(Session session, String name) {
        this.session = session;
        this.name = name;
        this.container = new KeyspaceContainer(this.session.getCluster());
    }

    /**
     * Get the {@link Session} associated with this keyspace.
     *
     * @return {@link Session}
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Get the cluster (or containing) this keyspace belongs to.
     *
     * @return {@link KeyspaceContainer}
     */
    public KeyspaceContainer getContainer() {
        return this.container;
    }

    /**
     * Is this the null keyspace?
     *
     * @return {@link Boolean}
     */
    public boolean isNull() {
        return (this.name == null || this.name.isEmpty());
    }

    /**
     * Name of this keyspace (case sensitive).
     *
     * @return Keyspace name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Is this keyspace name case sensitive?
     *
     * @return {@link Boolean}
     */
    public boolean isCaseSensitiveName() {
        return !isNull() && !StringUtils.isAllLowerCase(this.name);
    }

    /**
     * Ensure that this keyspace is used or logged into it's {@link Session}.
     */
    public void use() {
        if (isNull() || this.name.equals(this.session.getLoggedKeyspace())) {
            return;
        }

        this.session.execute(String.format("use \"%s\"", this.name));
    }

    /**
     * Drop this keyspace.
     */
    public void drop() {
        this.session.execute(String.format("drop keyspace \"%s\"", this.name));
    }

    /**
     * Drop this keyspace if it exists. If the keyspace does not exist, do nothing.
     */
    public void dropIfExists() {
        if (exists()) {
            drop();
        }
    }

    /**
     * Create this keyspace with a replication factor of 1.
     */
    public void create() {
        this.session.execute(String.format("create keyspace \"%s\" with replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} and durable_writes = true", this.name));
    }

    /**
     * Does this keyspace exist?
     *
     * @return {@link Boolean}
     */
    public boolean exists() {
        return this.container.keyspaceExists(this.name);
    }

    /**
     * List all tables in this keyspace by name.
     *
     * @return All table names.
     */
    public Collection<String> allTableNames() {
        Collection<TableMetadata> tables = getTables();
        Collection<String> result = new ArrayList<String>(tables.size());

        for (TableMetadata t : tables) {
            result.add(t.getName());
        }

        return Collections.unmodifiableCollection(result);
    }

    /**
     * List all tables in this keyspace.
     *
     * @return All tables.
     */
    public Collection<Table> allTables() {
        Collection<TableMetadata> tables = getTables();
        Collection<Table> result = new ArrayList<Table>(tables.size());

        for (TableMetadata t : tables) {
            result.add(new Table(t.getName(), this));
        }

        return Collections.unmodifiableCollection(result);
    }

    /**
     * Does a table exist in this keyspace?
     *
     * @param table Table name.
     * @return {@link Boolean}
     */
    public boolean tableExists(String table) {
        if (table == null || table.isEmpty() || isNull()) {
            return false;
        }

        KeyspaceMetadata keyspaceMetadata = this.container.getKeyspaceMetadata(this.name);

        return (keyspaceMetadata != null && keyspaceMetadata.getTable(table) != null);
    }

    /**
     * Get a table by name.
     *
     * @param table Table name
     * @return {@link Table}
     */
    public Table getTable(String table) {
        return new Table(table, this);
    }

    /**
     * Truncate tables in this keyspace.
     *
     * @param tables List of tables in this keyspace.
     */
    public void truncateTables(Set<String> tables) {
        for (Table table : allTables()) {
            if (tables.contains(table.getName())) {
                table.truncate();
            }
        }
    }

    /**
     * Get this keyspace's schema signature, the hash code of this keyspace's export CQL schema.
     *
     * @return Hash code of keyspace schema.
     */
    public Integer getSchemaSignature() {
        return new HashCodeBuilder(17, 37)
            .append(exists() ? this.container.getKeyspaceMetadata(this.name).exportAsString() : null)
            .build();
    }

    private Collection<TableMetadata> getTables() {
        KeyspaceMetadata keyspaceMetadata = this.container.getKeyspaceMetadata(this.name);

        if (keyspaceMetadata == null) {
            return Collections.emptyList();
        }

        return keyspaceMetadata.getTables();
    }
}

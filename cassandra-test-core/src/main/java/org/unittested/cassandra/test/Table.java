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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.unittested.cassandra.test.util.DriverCompatibility;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/**
 * Common Cassandra table operations for tests.
 */
public class Table {

    private static final int TRUNCATE_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(65);

    private String name;
    private Keyspace keyspace;

    public Table(String name, Keyspace keyspace) {
        this(name, false, keyspace);
    }

    public Table(String name, boolean isCaseSensitiveName, Keyspace keyspace) {
        this.name = isCaseSensitiveName ? name : name.toLowerCase();
        this.keyspace = keyspace;
    }

    /**
     * Get the keyspace this table belongs to.
     *
     * @return {@link Keyspace}
     */
    public Keyspace getKeyspace() {
        return this.keyspace;
    }

    /**
     * Get the table name.
     *
     * @return table name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Is the table name case sensitive?
     *
     * @return {@link Boolean}
     */
    public boolean isCaseSensitiveName() {
        return !StringUtils.isAllLowerCase(this.name);
    }

    /**
     * Get the number of rows in this table by issuing a count query for this table.
     *
     * @return Number of rows in this table.
     */
    public long getCount() {
        return getCount(0);
    }

    /**
     * Get the number of rows in this table by issuing a count query for this table.
     *
     * @param limit Count can never exceed this limit.
     * @return Number of rows in this table.
     */
    public long getCount(long limit) {
        return this.keyspace
                .getSession()
                .execute(select("COUNT(*)", this.name, limit))
                .one()
                .getLong(0);
    }

    /**
     * Select all rows for this table.
     *
     * @return Table rows. If the table is empty, an empty list is returned.
     */
    public List<Row> getRows() {
        return getRows(0);
    }

    /**
     * Select all rows for this table.
     *
     * @param limit Number of rows returned can never exceed this limit.
     * @return Table rows. If the table is empty, an empty list is returned.
     */
    public List<Row> getRows(long limit) {
        return this.keyspace
                .getSession()
                .execute(select("*", this.name, limit))
                .all();
    }

    /**
     * Drop this table.
     */
    public void drop() {
        Statement statement = new SimpleStatement(String.format("DROP TABLE \"%s\"", this.name)).setKeyspace(this.keyspace.getName());
        DriverCompatibility.setReadTimeoutMillis(statement, TRUNCATE_TIMEOUT);
        this.keyspace.getSession().execute(statement);
    }

    /**
     * Drop this table only if it exists.
     */
    public void dropIfExists() {
        if (exists()) {
            drop();
        }
    }

    /**
     * Truncate this table.
     */
    public void truncate() {
        Statement statement = new SimpleStatement(String.format("TRUNCATE \"%s\"", this.name)).setKeyspace(this.keyspace.getName());
        DriverCompatibility.setReadTimeoutMillis(statement, TRUNCATE_TIMEOUT);
        this.keyspace.getSession().execute(statement);
    }

    /**
     * Truncate this table only if it exists.
     */
    public void truncateIfExists() {
        if (exists()) {
            truncate();
        }
    }

    /**
     * Does this table exist in the schema?
     *
     * @return {@link Boolean}
     */
    public boolean exists() {
        return this.keyspace.tableExists(this.name);
    }

    private Statement select(String what, String from, long limit) {
        String limitStr = (limit > 0 ? ("LIMIT " + limit) : "");
        String cql = String.format("SELECT %s FROM \"%s\" %s", what, from, limitStr);
        return new SimpleStatement(cql).setKeyspace(this.keyspace.getName());
    }
}

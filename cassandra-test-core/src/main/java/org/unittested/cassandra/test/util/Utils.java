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

package org.unittested.cassandra.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

/**
 * Internal utilities.
 */
public final class Utils {

    private static final Pattern CQL = Pattern.compile("^\\s*(?:ALTER|BEGIN|CONSISTENCY|CREATE|DELETE|DROP|GRANT|INSERT|LIST|REVOKE|SELECT|SERIAL|TRUNCATE|UPDATE|USE)\\s",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern EMPTY_STATEMENT = Pattern.compile("^\\s*;\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private Utils() {

    }

    /**
     * Get system.local.schema_version from Cassandra.
     *
     * @param session {@link Session}
     * @return system.local.schema_version value
     */
    public static UUID getSchemaVersion(Session session) {
        return session
                .execute("select schema_version from system.local")
                .one()
                .getUUID("schema_version");
    }

    /**
     * Checks if a string kinda looks like a CQL statement.
     *
     * @param str String to check
     * @return {@link Boolean}
     */
    public static boolean isCqlLike(String str) {
        return (EMPTY_STATEMENT.matcher(str).matches() || CQL.matcher(str).find());
    }

    /**
     * Expand comma delimited entries in input array.
     * <p>
     * Example: [ "a,b", "c", "d,e"] to [ "a", "b", "c", "d", "e" ]
     *
     * @param input String array
     * @return Expanded string array
     */
    public static String [] expandCommaDelimitedEntries(String [] input) {
        ArrayList<String> output = new ArrayList<String>();

        for (String i : input) {
            for (String e : i.split(",")) {
                output.add(e.trim());
            }
        }

        return output.toArray(new String[output.size()]);
    }

    /**
     * Set refresh schema interval on {@link QueryOptions}.
     * <p>
     * Uses reflection to ensure backwards compatibility with older driver versions.
     *
     * @param queryOptions {@link QueryOptions}
     * @param refreshSchemaIntervalMillis Refresh schema interval.
     * @return Passed in QueryOptions for chaining
     */
    public static QueryOptions setRefreshSchemaIntervalMillis(QueryOptions queryOptions, int refreshSchemaIntervalMillis) {
        // Turn off driver de-bouncing for schema queries, as it can cause schema modifying tests to take really long.
        // Might want to make this configurable. Use reflection to maintain driver backwards compatibility.
        try {
            Method setRefreshSchemaIntervalMillis = QueryOptions.class.getDeclaredMethod("setRefreshSchemaIntervalMillis", int.class);
            setRefreshSchemaIntervalMillis.invoke(queryOptions, refreshSchemaIntervalMillis);
        } catch (NoSuchMethodException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }

        return queryOptions;
    }

    /**
     * Set the read timeout for a {@link Statement}.
     * <p>
     * Uses reflection to ensure backwards compatibility with older driver versions.
     *
     * @param statement {@link Statement}
     * @param readTimeoutMillis Read timeout.
     * @return Passed in statement for chaining
     */
    public static Statement setReadTimeoutMillis(Statement statement, int readTimeoutMillis) {
        try {
            Method setReadTimeoutMillis = Statement.class.getDeclaredMethod("setReadTimeoutMillis", int.class);
            setReadTimeoutMillis.invoke(statement, readTimeoutMillis);
        } catch (Exception e) {
            // Ignore.
        }

        return statement;
    }

    /**
     * Set default timestamp of a {@link BatchStatement}.
     * <p>
     * Uses reflection to ensure backwards compatibility with older driver versions.
     *
     * @param batchStatement {@link BatchStatement}
     * @param timestamp The timestamp value to set.
     * @return Passed in batchStatement for chaining
     */
    public static BatchStatement setDefaultTimestamp(BatchStatement batchStatement, long timestamp) {
        try {
            Method setDefaultTimestamp = BatchStatement.class.getDeclaredMethod("setDefaultTimestamp", long.class);
            setDefaultTimestamp.invoke(batchStatement, timestamp);
        } catch (Exception e) {
            // Ignore.
        }

        return batchStatement;
    }

    /**
     * Get default timestamp from a {@link BatchStatement}.
     * <p>
     * Uses reflection to ensure backwards compatibility with older driver versions.
     *
     * @param batchStatement {@link BatchStatement}
     * @return Default timestamp value or null if default timestamp is not available.
     */
    public static Long getDefaultTimestamp(BatchStatement batchStatement) {
        try {
            Method getDefaultTimestamp = BatchStatement.class.getDeclaredMethod("getDefaultTimestamp");
            return (Long)getDefaultTimestamp.invoke(batchStatement);
        } catch (Exception e) {
            // Ignore.
        }

        return null;
    }
}

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

import java.lang.reflect.Method;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Statement;

/**
 * Driver backwards compatibility utility methods.
 *
 * Use reflection to use new driver features and still be able to compile and run with older driver versions.
 */
public final class DriverCompatibility {

    private DriverCompatibility() {

    }

    /**
     * Set refresh schema interval on {@link QueryOptions}.
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
        } catch (Exception e) {
            // ignore
        }

        return queryOptions;
    }

    /**
     * Set the read timeout for a {@link Statement}.
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
            try {
                Method setDefaultTimestamp = Statement.class.getDeclaredMethod("setDefaultTimestamp", long.class);
                setDefaultTimestamp.invoke(batchStatement, timestamp);
            } catch (Exception ex) {
                // Ignore.
            }
        }

        return batchStatement;
    }

    /**
     * Get default timestamp from a {@link BatchStatement}.
     *
     * @param batchStatement {@link BatchStatement}
     * @return Default timestamp value or null if default timestamp is not available.
     */
    public static Long getDefaultTimestamp(BatchStatement batchStatement) {
        try {
            Method getDefaultTimestamp = BatchStatement.class.getDeclaredMethod("getDefaultTimestamp");
            return (Long)getDefaultTimestamp.invoke(batchStatement);
        } catch (Exception e) {
            try {
                Method getDefaultTimestamp = Statement.class.getDeclaredMethod("getDefaultTimestamp");
                return (Long)getDefaultTimestamp.invoke(batchStatement);
            } catch (Exception ex) {
                // Ignore.
            }
        }

        return null;
    }
}

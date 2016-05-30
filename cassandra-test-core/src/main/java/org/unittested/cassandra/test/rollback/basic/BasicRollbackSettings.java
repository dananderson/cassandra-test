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

package org.unittested.cassandra.test.rollback.basic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.rollback.RollbackSettings;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

public class BasicRollbackSettings implements RollbackSettings {

    private String [] tableInclusions;
    private String [] tableExclusions;
    private RollbackStrategy afterMethod;
    private RollbackStrategy afterClass;

    public BasicRollbackSettings() {
        this(new String[] {}, new String[] {}, RollbackStrategy.TRUNCATE, RollbackStrategy.NONE);
    }

    public BasicRollbackSettings(String [] tableInclusions,
                                 String [] tableExclusions,
                                 RollbackStrategy afterMethod,
                                 RollbackStrategy afterClass) {
        if (tableExclusions.length > 0 && tableInclusions.length > 0) {
            throw new CassandraTestException("Use tableExclusions OR tableInclusions not both.");
        }

        this.tableInclusions = tableInclusions;
        this.tableExclusions = tableExclusions;
        this.afterMethod = afterMethod;
        this.afterClass = afterClass;
    }

    @Override
    public String[] getTableInclusions() {
        return this.tableInclusions;
    }

    @Override
    public String[] getTableExclusions() {
        return this.tableExclusions;
    }

    @Override
    public RollbackStrategy getAfterMethod() {
        return this.afterMethod;
    }

    @Override
    public RollbackStrategy getAfterClass() {
        return this.afterClass;
    }

    @Override
    public void rollbackAfterMethod(TestRuntime runtime) {
        rollback(runtime, this.afterMethod);
    }

    @Override
    public void rollbackAfterClass(TestRuntime runtime) {
        rollback(runtime, this.afterClass);
    }

    private void rollback(TestRuntime runtime, RollbackStrategy rollbackStrategy) {
        switch(rollbackStrategy) {
            case DROP:
                if (!runtime.getTestSettings().getKeyspaceSettings().canDropKeyspace()) {
                    throw new CassandraTestException("Not allowed to drop the current keyspace.");
                }
                runtime.getKeyspace().dropIfExists();
                break;
            case KEYSPACE_TRUNCATE:
                truncate(runtime, ArrayUtils.EMPTY_STRING_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY);
                break;
            case TRUNCATE:
                truncate(runtime, this.tableInclusions, this.tableExclusions);
                break;
            case NONE:
                break;
            default:
                throw new CassandraTestException("Unsupported rollback strategy = %s", rollbackStrategy);
        }
    }

    private void truncate(TestRuntime runtime, String [] inclusions, String [] exclusions) {
        Set<String> tables = new HashSet<String>();

        if (inclusions.length > 0) {
            Collections.addAll(tables, inclusions);
        } else {
            tables.addAll(runtime.getKeyspace().allTableNames());
            for (String t : exclusions) {
                tables.remove(t);
            }
        }

        for(String t : runtime.getTestSettings().getKeyspaceSettings().getProtectedTables()) {
            tables.remove(t);
        }

        runtime.getKeyspace().truncateTables(tables);
    }
}

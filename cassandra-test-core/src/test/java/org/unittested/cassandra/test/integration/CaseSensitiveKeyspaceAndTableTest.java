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

package org.unittested.cassandra.test.integration;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.testng.annotations.Test;
import org.unittested.cassandra.test.AbstractCassandraTest;
import org.unittested.cassandra.test.Table;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

@CassandraKeyspace(value = "CaseSensitive", isCaseSensitiveKeyspace = "true", schema = "CREATE TABLE \"Group\" (id INT PRIMARY KEY);")
@CassandraData(value = "INSERT INTO \"Group\"(id) VALUES (1); INSERT INTO \"Group\"(id) VALUES (2);")
@CassandraRollback(afterMethod = RollbackStrategy.NONE, afterClass = RollbackStrategy.DROP)
public class CaseSensitiveKeyspaceAndTableTest extends AbstractCassandraTest {

    @Test
    public void verifyKeyspace() throws Exception {
        assertThat(getKeyspaceContainer().keyspaceExists("CaseSensitive"), is(true));
        assertThat(getKeyspace().getName(), is("CaseSensitive"));
        assertThat(getKeyspace().isCaseSensitiveName(), is(true));

        assertThat(getKeyspaceContainer().keyspaceExists("casesensitive"), is(false));
    }

    @Test
    public void verifyTable() throws Exception {
        Table group = getKeyspace().getTable("Group");

        assertThat(getKeyspace().tableExists("Group"), is(true));
        assertThat(group.getName(), is("Group"));
        assertThat(group.isCaseSensitiveName(), is(true));
        assertThat(group.getCount(), is(2L));
        assertThat(getKeyspace().allTableNames(), contains("Group"));

        assertThat(getKeyspace().tableExists("group"), is(false));
    }
}

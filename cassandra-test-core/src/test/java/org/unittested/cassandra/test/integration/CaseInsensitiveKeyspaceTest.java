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

import com.datastax.driver.core.Metadata;

@CassandraKeyspace(value = "CaseInsensitive", isCaseSensitiveKeyspace = "false", schema = "CREATE TABLE Group (id INT PRIMARY KEY);")
@CassandraData(value = "INSERT INTO Group (id) VALUES (1); INSERT INTO Group (id) VALUES (2);")
@CassandraRollback(afterMethod = RollbackStrategy.NONE, afterClass = RollbackStrategy.DROP)
public class CaseInsensitiveKeyspaceTest extends AbstractCassandraTest {

    @Test
    public void verifyKeyspace() throws Exception {
        assertThat(getCluster().getMetadata().getKeyspace("caseinsensitive"), notNullValue());
        assertThat(getKeyspace().getName(), is("caseinsensitive"));
        assertThat(getKeyspace().isCaseSensitiveName(), is(false));

        assertThat(getCluster().getMetadata().getKeyspace(Metadata.quote("CaseInsensitive")), nullValue());
    }

    @Test
    public void verifyTable() throws Exception {
        Table group = getKeyspace().getTable("group");

        assertThat(getKeyspace().tableExists("group"), is(true));
        assertThat(group.getName(), is("group"));
        assertThat(group.isCaseSensitiveName(), is(false));
        assertThat(group.getCount(), is(2L));
        assertThat(getKeyspace().allTableNames(), contains("group"));

        assertThat(getKeyspace().tableExists("Group"), is(false));
    }

}

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

package org.unittested.cassandra.test.spring.sample;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.*;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.annotation.CassandraBean;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.unittested.cassandra.test.spring.SpringCassandraTestExecutionListener;

/**
 * Sample usage of {@link SpringCassandraTestExecutionListener} to write tests.
 */
@CassandraKeyspace(keyspace = "spring_cassandra_test", schema = "classpath:sample-schema.cql")
@CassandraData(data = "classpath:sample-data.cql")
@CassandraRollback(afterClass = RollbackStrategy.DROP)
@ContextConfiguration(locations = { "classpath:spring-test-context.xml" })
@TestExecutionListeners(value = SpringCassandraTestExecutionListener.class, mergeMode = MERGE_WITH_DEFAULTS)
public class SpringCassandraSampleTest extends AbstractTestNGSpringContextTests {

    @CassandraBean
    private Keyspace keyspace;

    @Test
    public void timeseriesTableExists() throws Exception {
        assertThat(this.keyspace.tableExists("timeseries"), is(true));
    }

    @Test
    public void timeseriesRowCount() throws Exception {
        assertThat(this.keyspace.getTable("timeseries").getCount(), is(3L));
    }
}

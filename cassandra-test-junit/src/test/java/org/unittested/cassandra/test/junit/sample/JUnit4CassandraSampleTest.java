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

package org.unittested.cassandra.test.junit.sample;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.junit.AbstractJUnit4CassandraTest;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

import com.datastax.driver.core.ResultSet;

/**
 * Sample usage of {@link AbstractJUnit4CassandraTest} to write tests.
 */
@CassandraKeyspace(keyspace = "junit_cassandra_test", schema = "classpath:sample-schema.cql")
@CassandraData(data = "classpath:sample-data.cql")
@CassandraRollback(afterClass = RollbackStrategy.DROP)
public class JUnit4CassandraSampleTest extends AbstractJUnit4CassandraTest {

    @Test
    public void timeseriesTableExists() throws Exception {
        assertThat(getKeyspace().hasTable("timeseries"), is(true));
    }

    @Test
    public void timeseriesRowCount() throws Exception {
        ResultSet result = getSession().execute("SELECT COUNT(*) FROM timeseries");
        assertThat(result.one().getLong(0), is(3L));
    }
}


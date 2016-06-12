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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.junit.rule.CassandraClassRule;
import org.unittested.cassandra.test.junit.rule.CassandraRule;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

/**
 * Sample usage of Cassandra Test JUnit rules to write tests.
 */
@CassandraKeyspace(value = "junit_cassandra_test", schema = "classpath:sample-schema.cql")
@CassandraData("classpath:sample-data.cql")
@CassandraRollback(afterClass = RollbackStrategy.DROP)
public class JUnit4CassandraRuleSampleTest {

    @ClassRule
    public static CassandraClassRule classRule = new CassandraClassRule();

    @Rule
    public CassandraRule cassandraRule = new CassandraRule(classRule);

    @Test
    public void timeseriesTableExists() throws Exception {
        assertThat(this.cassandraRule.getKeyspace().tableExists("timeseries"), is(true));
    }

    @Test
    public void timeseriesRowCount() throws Exception {
        assertThat(this.cassandraRule.getKeyspace().getTable("timeseries").getCount(), is(3L));
    }
}

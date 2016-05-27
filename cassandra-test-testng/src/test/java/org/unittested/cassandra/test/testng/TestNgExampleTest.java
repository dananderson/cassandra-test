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

package org.unittested.cassandra.test.testng;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.testng.annotations.Test;

import com.datastax.driver.core.Row;

/**
 * Trivial example of {@link AbstractTestNgCassandraTest} usage.
 */
@CassandraKeyspace(keyspace = "test", schema = "CREATE TABLE timeseries (sensor INT, at TIMEUUID, PRIMARY KEY (sensor, at));")
@CassandraData(data = "INSERT INTO timeseries(sensor, at) VALUES (1, a3d787ba-e625-11e4-8a00-1681e6b88ec1);")
public class TestNgExampleTest extends AbstractTestNgCassandraTest {

    @Test
    public void test() throws Exception {
        Row row = getSession().execute("SELECT * FROM timeseries").one();

        assertThat(row, notNullValue());
        assertThat(row.getInt("sensor"), is(1));
        assertThat(row.getUUID("at"), is(UUID.fromString("a3d787ba-e625-11e4-8a00-1681e6b88ec1")));
    }
}

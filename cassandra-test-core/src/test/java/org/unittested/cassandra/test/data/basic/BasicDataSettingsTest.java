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

package org.unittested.cassandra.test.data.basic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.unittested.cassandra.test.AbstractCassandraTest;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.cql.BasicCqlResourceLoader;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

@CassandraKeyspace(value = "data_settings_test", schema = "CREATE TABLE a (x int PRIMARY KEY);")
@CassandraRollback(afterClass = RollbackStrategy.DROP)
public class BasicDataSettingsTest extends AbstractCassandraTest {

    private static final String [] DATA = new String [] {
            "INSERT INTO a(x) VALUES (1000);",
            "INSERT INTO a(x) VALUES (2000);",
            "INSERT INTO a(x) VALUES (3000);",
    };

    @Test
    public void load() throws Exception {
        // given
        DataSettings dataSettings = new BasicDataSettings(DATA, new BasicCqlResourceLoader());
        TestRuntime runtime = createRuntime();

        assertThat(tableCount("a"), is(0L));

        // when
        dataSettings.load(runtime);

        // then
        assertThat(tableCount("a"), is(3L));
    }

    private long tableCount(String table) {
        return getSession()
                .execute("select count(*) from \"" + table + "\";")
                .one()
                .getLong(0);
    }

    private TestRuntime createRuntime() {
        TestRuntime runtime = mock(TestRuntime.class);

        when(runtime.getKeyspace()).thenReturn(getKeyspace());

        return runtime;
    }
}

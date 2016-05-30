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

package org.unittested.cassandra.test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

import com.datastax.driver.core.exceptions.DriverException;

@CassandraKeyspace(keyspace = "test", schema = "classpath:cql/sample-schema.cql")
@CassandraData(data = "classpath:cql/sample-data.cql")
@CassandraRollback(afterClass = RollbackStrategy.DROP)
public class TableTest extends AbstractCassandraTest {

    @Test
    public void getName() throws Exception {
        assertThat(getTestTable().getName(), is("test_table"));
    }

    @Test
    public void exists() throws Exception {
        Table table = new Table("test_table", getKeyspace());

        assertThat(table.exists(), is(true));
    }

    @Test
    public void tableGetKeyspace() throws Exception {
        assertThat(getTestTable().getKeyspace(), sameInstance(getKeyspace()));
    }

    @Test
    public void count() throws Exception {
        assertThat(getTestTable().getCount(), is(1L));
    }

    @Test(expectedExceptions = DriverException.class)
    public void countWithTableThatDoesNotExist() throws Exception {
        // given
        Table table = new Table("t", getKeyspace());

        // when
        table.getCount();

        // then
        // DriverException
    }

    @Test
    public void countLimit() throws Exception {
        assertThat(getTestTable().getCount(1L), is(1L));
    }

    @Test(expectedExceptions = DriverException.class)
    public void countLimitWithTableThatDoesNotExist() throws Exception {
        // given
        Table table = new Table("t", getKeyspace());

        // when
        table.getCount(1L);

        // then
        // DriverException
    }

    @Test
    public void truncate() throws Exception {
        // given
        assertThat(getTestTable().getCount(), is(1L));

        // when
        getTestTable().truncate();

        // then
        assertThat(getTestTable().getCount(), is(0L));
    }

    @Test(expectedExceptions = DriverException.class)
    public void truncateWithTableThatDoesNotExist() throws Exception {
        // given
        Table table = new Table("t", getKeyspace());

        // when
        table.truncate();

        // then
        // DriverException
    }

    @DataProvider(name = "tables")
    public static Object[][] tables() {
        return new Object[][] {
                { "test_table" },
                { "t" }
        };
    }

    @Test(dataProvider = "tables")
    public void truncateIfExists(String name) throws Exception {
        // given
        Table table = new Table(name, getKeyspace());
        if (table.exists()) {
            assertThat(table.getCount(), is(1L));
        }

        // when
        table.truncateIfExists();

        // then
        if (table.exists()) {
            assertThat(table.getCount(), is(0L));
        }
    }

    @Test
    public void drop() throws Exception {
        // given
        assertThat(getTestTable().exists(), is(true));

        // when
        getTestTable().drop();

        // then
        assertThat(getTestTable().exists(), is(false));
    }

    @Test(expectedExceptions = DriverException.class)
    public void dropWithTableThatDoesNotExist() throws Exception {
        // given
        Table table = new Table("t", getKeyspace());

        // when
        table.drop();

        // then
        // DriverException
    }

    @Test(dataProvider = "tables")
    public void dropIfExists(String name) throws Exception {
        // given
        Table table = new Table(name, getKeyspace());

        // when
        table.dropIfExists();

        // then
        assertThat(table.exists(), is(false));
    }

    @Test
    public void getRows() throws Exception {
        assertThat(getTestTable().getRows(), hasSize(1));
    }

    @Test
    public void getRowsLimit() throws Exception {
        assertThat(getTestTable().getRows(1L), hasSize(1));
    }

    private Table getTestTable() {
        return getKeyspace().getTable("test_table");
    }
}

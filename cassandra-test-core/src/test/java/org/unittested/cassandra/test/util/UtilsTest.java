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


import org.unittested.cassandra.test.AbstractCassandraTest;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@CassandraKeyspace(keyspace = Keyspace.NULL)
@CassandraRollback(afterMethod = RollbackStrategy.NONE)
public class UtilsTest extends AbstractCassandraTest {

    @Test
    public void getSchemaVersion() throws Exception {
        assertThat(Utils.getSchemaVersion(getSession()), notNullValue());
    }

    @DataProvider
    public static Object[][] expandCommaDelimitedEntries() {
        return new Object[][] {
                { toArray("x,y", "z", "a"), toArray("x", "y", "z", "a") },
                { toArray("x,y", "z,a", "b,c,d"), toArray("x", "y", "z", "a", "b", "c", "d") },
                { toArray("x", "y", "z"), toArray("x", "y", "z") },
                { toArray("x", "", "y"), toArray("x", "", "y") }
        };
    }

    @Test(dataProvider = "expandCommaDelimitedEntries")
    public void expandCommaDelimitedEntries(String [] input, String [] output) throws Exception {
        assertThat(Utils.expandCommaDelimitedEntries(input), arrayContaining(output));
    }
}

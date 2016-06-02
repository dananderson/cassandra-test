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

package org.unittested.cassandra.test.junit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.KeyspaceContainer;
import org.unittested.cassandra.test.annotation.CassandraBean;
import org.unittested.cassandra.test.junit.rule.CassandraTest;
import org.unittested.cassandra.test.junit.rule.CassandraTestInit;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Base class for JUnit-based Cassandra Test tests.
 *
 * This base class is for convenience, avoiding a minimal amount of boiler plate code. Alternatively, test writers can
 * simply include {@link CassandraTestInit}, {@link CassandraTest} and {@link CassandraBean}.
 */
public abstract class AbstractJUnit4CassandraTest {

    @ClassRule
    public static CassandraTestInit init = new CassandraTestInit();

    @Rule
    public CassandraTest cassandraTest = new CassandraTest(init, this);

    @CassandraBean
    private Session session;

    @CassandraBean
    private Cluster cluster;

    @CassandraBean
    private Keyspace keyspace;

    @CassandraBean
    private KeyspaceContainer keyspaceContainer;

    public AbstractJUnit4CassandraTest() {

    }

    protected Cluster getCluster() {
        return this.cluster;
    }

    protected Session getSession() {
        return this.session;
    }

    protected Keyspace getKeyspace() {
        return this.keyspace;
    }

    protected KeyspaceContainer getKeyspaceContainer() {
        return this.keyspaceContainer;
    }
}

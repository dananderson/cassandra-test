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
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.KeyspaceContainer;
import org.unittested.cassandra.test.junit.rule.CassandraClassRule;
import org.unittested.cassandra.test.junit.rule.CassandraRule;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Base class for JUnit-based Cassandra Test tests.
 *
 * This base class is for convenience, avoiding a minimal amount of boiler plate code. Alternatively, test writers can
 * simply include JUnit rules {@link CassandraClassRule} and {@link CassandraRule}.
 */
public abstract class AbstractJUnit4CassandraTest {

    @ClassRule
    public static CassandraClassRule classRule = new CassandraClassRule();

    @Rule
    public CassandraRule cassandraRule = new CassandraRule(classRule);

    public AbstractJUnit4CassandraTest() {

    }

    protected Cluster getCluster() {
        return this.cassandraRule.getCluster();
    }

    protected Session getSession() {
        return this.cassandraRule.getSession();
    }

    protected Keyspace getKeyspace() {
        return this.cassandraRule.getKeyspace();
    }

    protected KeyspaceContainer getKeyspaceContainer() {
        return this.cassandraRule.getKeyspaceContainer();
    }
}

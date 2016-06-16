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

package org.unittested.cassandra.test.junit.rule;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * JUnit method rule that performs before method setup and after method tear down for Cassandra Test.
 *
 * A Cassandra field must be present and marked with a {@link org.junit.Rule} annotation in a
 * Cassandra Test. This rule works in conjunction with {@link CassandraClassRule} to integrate Cassandra Test
 * with JUnit.
 */
public class CassandraRule implements MethodRule {
    
    private CassandraClassRule classRule;
    private Object preparedInstance;

    public CassandraRule(CassandraClassRule classRule) {
        this.classRule = classRule;
        this.preparedInstance = null;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                TestEnvironmentAdapter adapter = CassandraRule.this.classRule.getAdapter();

                if (CassandraRule.this.preparedInstance != target) {
                    try {
                        adapter.onPrepareTestInstance(target);
                        CassandraRule.this.preparedInstance = target;
                    } catch (Exception e) {
                        throw new CassandraTestException("Failed to prepare the test instance!", e);
                    }
                }

                try {
                    adapter.onBeforeMethod(target, method.getMethod());
                    base.evaluate();
                } finally {
                    adapter.onAfterMethod(target, method.getMethod());
                }
            }
        };
    }

    public Session getSession() {
        return this.classRule.getAdapter().getRuntime().getKeyspace().getSession();
    }

    public Cluster getCluster() {
        return this.classRule.getAdapter().getRuntime().getKeyspace().getSession().getCluster();
    }

    public Keyspace getKeyspace() {
        return this.classRule.getAdapter().getRuntime().getKeyspace();
    }
}

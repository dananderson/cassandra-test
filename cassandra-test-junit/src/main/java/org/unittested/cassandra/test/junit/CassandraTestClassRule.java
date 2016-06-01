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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.TestSettingsBuilder;
import org.unittested.cassandra.test.property.system.PropertiesPropertyResolver;

/**
 * JUnit rule that runs before class setup and after class clean up for Cassandra Test.
 *
 * A CassandraTestClassRule field must be present and marked with a {@link org.junit.ClassRule} annotation in a
 * Cassandra Test. This rule works in conjunction with {@link CassandraTestMethodRule} to integrated Cassandra Test
 * with JUnit.
 */
public class CassandraTestClassRule implements TestRule, TestEnvironmentAdapterProvider {

    private TestEnvironmentAdapter adapter;

    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Class<?> testClass = description.getTestClass();

                CassandraTestClassRule.this.adapter = CassandraTestClassRule.this.createTestEnvironmentAdapter(testClass);
                CassandraTestClassRule.this.adapter.onBeforeClass(testClass, null);

                try {
                    base.evaluate();
                } finally {
                    CassandraTestClassRule.this.adapter.onAfterClass(description.getTestClass(), null);
                    CassandraTestClassRule.this.adapter = null;
                }
            }
        };
    }

    public TestEnvironmentAdapter getAdapter() {
        return this.adapter;
    }

    protected TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass) {
        return new TestEnvironmentAdapter(
                new TestSettingsBuilder()
                        .withPropertyResolver(PropertiesPropertyResolver.SYSTEM)
                        .withTestClass(testClass)
                        .build());
    }
}

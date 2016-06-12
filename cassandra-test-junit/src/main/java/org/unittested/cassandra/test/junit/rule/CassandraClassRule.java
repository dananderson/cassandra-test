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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.TestSettings;
import org.unittested.cassandra.test.TestSettingsBuilder;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertiesPropertyResolver;

/**
 * JUnit rule that runs before class setup and after class clean up for Cassandra Test.
 *
 * A CassandraClassRule field must be present and marked with a {@link org.junit.ClassRule} annotation in a
 * Cassandra Test. This rule works in conjunction with {@link CassandraRule} to integrate Cassandra Test
 * with JUnit.
 */
public class CassandraClassRule implements TestRule {

    private TestEnvironmentAdapter adapter;

    public CassandraClassRule() {

    }

    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Class<?> testClass = description.getTestClass();

                CassandraClassRule.this.adapter = CassandraClassRule.this.createTestEnvironmentAdapter(testClass);

                if (CassandraClassRule.this.adapter == null) {
                    throw new CassandraTestException("Failed to create a TestEnvironmentAdapter.");
                }

                try {
                    CassandraClassRule.this.adapter.onBeforeClass(testClass);

                    try {
                        base.evaluate();
                    } finally {
                        CassandraClassRule.this.adapter.onAfterClass(testClass);
                    }
                } finally {
                    CassandraClassRule.this.adapter = null;
                }
            }
        };
    }

    TestEnvironmentAdapter getAdapter() {
        return this.adapter;
    }

    protected TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass) {
        TestSettingsBuilder defaults = new TestSettingsBuilder()
                .withDefaultPropertyResolver(PropertiesPropertyResolver.DEFAULT)
                .withTestClass(testClass);

        return new TestEnvironmentAdapter(createTestSettings(defaults, testClass));
    }

    protected TestSettings createTestSettings(TestSettingsBuilder defaults, Class<?> testClass) {
        return defaults.build();
    }
}

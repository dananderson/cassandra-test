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

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

/**
 * JUnit rule that performs before method setup and after method tear down for Cassandra Test.
 *
 * A CassandraTestMethod field must be present and marked with a {@link org.junit.Rule} annotation in a Cassandra Test.
 * CassandraTestMethod depends on {@link CassandraTestClassRule} to be available to provide the test's
 * {@link org.unittested.cassandra.test.TestEnvironmentAdapter}. In addition, this rule does not have access to the
 * test instance in the {@link #apply(Statement, Description)} call, so the test instance must be passed in for later
 * access.
 */
public class CassandraTestMethodRule implements TestRule {
    
    private TestEnvironmentAdapterProvider adapterProvider;
    private Object testInstance;
    
    public CassandraTestMethodRule(TestEnvironmentAdapterProvider adapterProvider, Object testInstance) {
        this.adapterProvider = adapterProvider;
        this.testInstance = testInstance;

        try {
            this.adapterProvider.getAdapter().onPrepareTestInstance(this.testInstance, null);
        } catch (Exception e) {
            throw new CassandraTestException("Failed to prepare the test instance!", e);
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Object testInstance = CassandraTestMethodRule.this.testInstance;

                if (!testInstance.getClass().equals(description.getTestClass())) {
                    throw new CassandraTestException("Registered test instance class (%s) does not match class of currently executing test (%s).",
                            getCanonicalName(testInstance.getClass()),
                            getCanonicalName(description.getTestClass()));
                }

                Method testMethod = findTestMethod(description);
                TestEnvironmentAdapter adapter = CassandraTestMethodRule.this.adapterProvider.getAdapter();

                adapter.onBeforeMethod(testInstance, testMethod, null);

                try {
                    base.evaluate();
                } finally {
                    adapter.onAfterMethod(testInstance, testMethod, null);
                }
            }
        };
    }

    /*
     * The Description object exposes the test method name, but not the Method object or the full signature. With limited
     * info, search through the Test Methods and pick the first one with the matching name.
     */
    private Method findTestMethod(Description description) {
        for (Class<?> c = description.getTestClass(); c != null && !c.equals(Object.class); c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (!method.getName().equals(description.getMethodName())) {
                    continue;
                }

                if (method.isAnnotationPresent(Test.class)) {
                    return method;
                }
            }
        }

        throw new CassandraTestException("Cannot find test method '%s' in '%s'",
                description.getMethodName(), getCanonicalName(description.getTestClass()));
    }

    private String getCanonicalName(Class<?> testClass) {
        return testClass != null ? testClass.getCanonicalName() : "null";
    }
}

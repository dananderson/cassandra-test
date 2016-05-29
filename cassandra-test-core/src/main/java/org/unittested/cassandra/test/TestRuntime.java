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

import java.lang.reflect.Method;

import com.datastax.driver.core.Session;

/**
 * Cassandra Test runtime state.
 */
public class TestRuntime {

    private Object test;
    private Class<?> testClass;
    private Object testEnvironmentContext;
    private TestSettings testSettings;
    private Method testMethod;
    private Keyspace keyspace;

    public TestRuntime(Class<?> testClass,
                       Object testEnvironmentContext,
                       Session session,
                       TestSettings settings) {
        this.testClass = testClass;
        this.testSettings = settings;
        this.keyspace = new Keyspace(session, settings.getKeyspaceSettings().getKeyspace());
        this.testEnvironmentContext = testEnvironmentContext;
    }

    /**
     * Currently running test class instance.
     *
     * @return Test instance.
     */
    public Object getTest() {
        return this.test;
    }

    /**
     * Currently running test class.
     *
     * @return Test class.
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Currently running test method.
     * <p>
     * If null, Cassandra Test is not currently running a test method.
     *
     * @return Test method.
     */
    public Method getTestMethod() {
        return this.testMethod;
    }

    /**
     * Test environment specific context.
     * <p>
     * For the test environments that do not use a test context, this will be null.
     *
     * @return Test environment context.
     */
    public Object getTestEnvironmentContext() {
        return this.testEnvironmentContext;
    }

    /**
     * Cassandra connection.
     *
     * @return {@link Keyspace}
     */
    public Keyspace getKeyspace() {
        return this.keyspace;
    }

    /**
     * Cassandra Test configuration.
     *
     * @return {@link TestSettings}
     */
    public TestSettings getTestSettings() {
        return this.testSettings;
    }

    void updateTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    void updateTest(Object test) {
        this.test = test;
    }
}

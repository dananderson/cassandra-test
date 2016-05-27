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

import org.unittested.cassandra.test.keyspace.state.BasicKeyspaceStateManager;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects a test environment to Cassandra Test.
 */
public class TestEnvironmentAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TestEnvironmentAdapter.class);
    private static final KeyspaceStateManager KEYSPACE_STATE_MANAGER_INSTANCE = new BasicKeyspaceStateManager();

    private TestSettings testSettings;
    private KeyspaceStateManager keyspaceStateManager;
    private TestRuntime runtime;

    public TestEnvironmentAdapter(TestSettings settings) {
        this(settings, KEYSPACE_STATE_MANAGER_INSTANCE);
    }

    public TestEnvironmentAdapter(TestSettings settings,
                                  KeyspaceStateManager keyspaceStateManager) {
        this.testSettings = settings;
        this.keyspaceStateManager = keyspaceStateManager;
    }

    /**
     * Setup before test methods start running.
     *
     * @param test Test instance.
     * @param testEnvironmentContext Test environment context. Can be null if test environment does not have the concept of a context.
     * @throws Exception on test setup failure
     */
    public void onBeforeClass(Object test, Object testEnvironmentContext) throws Exception {
        LOG.trace("onBeforeClass()");

        this.runtime = openConnection(test, testEnvironmentContext, this.testSettings);
    }

    /**
     * Tear down after all test methods have finished running.
     *
     * @param test Test instance.
     * @param testEnvironmentContext Test environment context. Can be null if test environment does not have the concept of a context.
     * @throws Exception on test clean up failure
     */
    public void onAfterClass(Object test, Object testEnvironmentContext) throws Exception {
        LOG.trace("onAfterClass()");

        try {
            rollbackAfterClass(this.runtime);
        } finally {
            closeConnection(this.runtime);
            this.runtime = null;
        }
    }

    /**
     * Setup before a test method runs.
     *
     * @param test Test instance.
     * @param testMethod Test method.
     * @param testEnvironmentContext Test environment context. Can be null if test environment does not have the concept of a context.
     * @throws Exception on test setup failure
     */
    public void onBeforeMethod(Object test, Method testMethod, Object testEnvironmentContext) throws Exception {
        LOG.trace("onBeforeMethod() : {}", testMethod.getName());

        this.runtime.update(testMethod);
        syncSchema(this.runtime, this.keyspaceStateManager);
        loadData(this.runtime);
        this.runtime.getKeyspace().use();
    }

    /**
     * Tear dwon after a test method runs.
     *
     * @param test Test instance.
     * @param testMethod Test method.
     * @param testEnvironmentContext Test environment context. Can be null if test environment does not have the concept of a context.
     * @throws Exception on test clean up failure
     */
    public void onAfterMethod(Object test, Method testMethod, Object testEnvironmentContext) throws Exception {
        LOG.trace("onAfterMethod() : {}", testMethod.getName());

        try {
            rollbackAfterMethod(this.runtime);
        } finally {
            this.runtime.update(null);
        }
    }

    /**
     * Get Cassandra Test runtime state.
     *
     * @return {@link TestRuntime}
     */
    public TestRuntime getRuntime() {
        return this.runtime;
    }

    protected TestRuntime openConnection(Object test, Object testEnvironmentContext, TestSettings config) {
        return new TestRuntime(
                test,
                testEnvironmentContext,
                config.getConnectSettings().connect(),
                config);
    }

    protected void closeConnection(TestRuntime runtime) {
        Keyspace keyspace = runtime.getKeyspace();

        if (keyspace == null) {
            return;
        }

        keyspace.getContainer().close();
    }

    protected void rollbackAfterMethod(final TestRuntime runtime) {
        runtime.getTestSettings().getRollbackSettings().rollbackAfterMethod(runtime);
    }

    protected void rollbackAfterClass(final TestRuntime runtime) {
        runtime.getTestSettings().getRollbackSettings().rollbackAfterClass(runtime);
    }

    protected void syncSchema(TestRuntime runtime, KeyspaceStateManager keyspaceStateManager) {
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);
    }

    protected void loadData(TestRuntime runtime) {
        runtime.getTestSettings().getDataSettings().load(runtime);
    }
}

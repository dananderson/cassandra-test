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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.unittested.cassandra.test.annotation.CassandraBean;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.keyspace.state.BasicKeyspaceStateManager;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Handles events from the test environment to manage a Cassandra Test.
 *
 * Adapter events are called in the following order:
 * <ol>
 *     <li>{@link #onBeforeClass(Class, Object)}</li>
 *     <li>{@link #onPrepareTestInstance(Object, Object)}</li>
 *     <li>{@link #onBeforeMethod(Object, Method, Object)}</li>
 *     <li>Run test method.</li>
 *     <li>{@link #onAfterMethod(Object, Method, Object)}</li>
 *     <li>Repeat #3 until all test methods have been run.</li>
 *     <li>{@link #onAfterClass(Class, Object)}</li>
 * </ol>
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
     * Handle setup of the test class.
     * <p>
     * The test environment calls this once prior to test instance creation. Setup is limited to access to
     * the test class only. All test instance setup should be in {@link #onPrepareTestInstance(Object, Object)}. The
     * setup is structured like this to support different behaviors of test environments.
     *
     * @param testClass Test class.
     * @param testEnvironmentContext Test environment specific context. Can be null.
     * @throws Exception on test setup failure
     */
    public void onBeforeClass(Class<?> testClass, Object testEnvironmentContext) throws Exception {
        LOG.trace("onBeforeClass()");

        this.runtime = openConnection(testClass, testEnvironmentContext, this.testSettings);
    }

    /**
     * Handle setup of the test instance.
     * <p>
     * The test environment calls this once when the test instance is created and before the test methods start
     * running. Instance setup is performed here, including assigning fields with the {@link CassandraBean} annotation.
     *
     * @param test Test instance.
     * @param testEnvironmentContext Test environment specific context. Can be null.
     * @throws Exception on test setup failure
     */
    public void onPrepareTestInstance(Object test, Object testEnvironmentContext) throws Exception {
        LOG.trace("onPrepareTestInstance()");

        if (this.runtime == null) {
            return;
        }

        populateCassandraBeanFields(test, this.runtime);
        this.runtime.updateTest(test);
    }

    /**
     * Handle clean up of the test instance and class.
     * <p>
     * The test environment calls this once after all test methods have run. The Cassandra keyspace is cleaned up and
     * the connection is closed.
     *
     * @param testClass Test class.
     * @param testEnvironmentContext Test environment specific context. Can be null.
     * @throws Exception on test clean up failure
     */
    public void onAfterClass(Class<?> testClass, Object testEnvironmentContext) throws Exception {
        LOG.trace("onAfterClass()");

        if (this.runtime == null) {
            return;
        }

        try {
            rollbackAfterClass(this.runtime);
        } finally {
            closeConnection(this.runtime);
            this.runtime = null;
        }
    }

    /**
     * Handle setup before a test method runs.
     * <p>
     * The test environment calls this before each test method that is run. The keyspace, schema and data are examined
     * to check that the Cassandra state is consistent with the current configuration. This method will ensure that
     * the Cassandra state is synced with the configuration.
     *
     * @param test Test instance.
     * @param testMethod Test method.
     * @param testEnvironmentContext Test environment specific context. Can be null.
     * @throws Exception on test setup failure
     */
    public void onBeforeMethod(Object test, Method testMethod, Object testEnvironmentContext) throws Exception {
        LOG.trace("onBeforeMethod() : {}", testMethod.getName());

        if (this.runtime == null) {
            return;
        }

        this.runtime.updateTestMethod(testMethod);
        syncSchema(this.runtime, this.keyspaceStateManager);
        loadData(this.runtime);
        this.runtime.getKeyspace().use();
    }

    /**
     * Handle tear down after a test method runs.
     * <p>
     * The test environment calls this after each test method that is run. Rollback of Cassandra data is performed here.
     *
     * @param test Test instance.
     * @param testMethod Test method.
     * @param testEnvironmentContext Test environment context. Can be null if test environment does not have the concept of a context.
     * @throws Exception on test clean up failure
     */
    public void onAfterMethod(Object test, Method testMethod, Object testEnvironmentContext) throws Exception {
        LOG.trace("onAfterMethod() : {}", testMethod.getName());

        if (this.runtime == null) {
            return;
        }

        try {
            rollbackAfterMethod(this.runtime);
        } finally {
            this.runtime.updateTestMethod(null);
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

    protected TestRuntime openConnection(Class<?> testClass, Object testEnvironmentContext, TestSettings config) {
        return new TestRuntime(
                testClass,
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

    protected void rollbackAfterMethod(TestRuntime runtime) {
        runtime.getTestSettings().getRollbackSettings().rollbackAfterMethod(runtime);
    }

    protected void rollbackAfterClass(TestRuntime runtime) {
        runtime.getTestSettings().getRollbackSettings().rollbackAfterClass(runtime);
    }

    protected void syncSchema(TestRuntime runtime, KeyspaceStateManager keyspaceStateManager) {
        runtime.getTestSettings().getKeyspaceSettings().sync(runtime, keyspaceStateManager);
    }

    protected void loadData(TestRuntime runtime) {
        runtime.getTestSettings().getDataSettings().load(runtime);
    }

    protected void populateCassandraBeanFields(Object test, TestRuntime runtime) {
        Map<Class<?>, Object> beanMap = null;

        for (Class<?> c = test.getClass(); c != null && !c.equals(Object.class); c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if(!field.isAnnotationPresent(CassandraBean.class)) {
                    continue;
                }

                if((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                    throw new CassandraTestException("CassandraBean cannot be used with final fields = %s.%s",
                            c.getCanonicalName(), field.getName());
                }

                if((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                    throw new CassandraTestException("CassandraBean cannot be used with static fields = %s.%s",
                            c.getCanonicalName(), field.getName());
                }

                if (beanMap == null) {
                    beanMap = new HashMap<Class<?>, Object>();
                    beanMap.put(Session.class, runtime.getKeyspace().getSession());
                    beanMap.put(Cluster.class, runtime.getKeyspace().getSession().getCluster());
                    beanMap.put(Keyspace.class, runtime.getKeyspace());
                    beanMap.put(KeyspaceContainer.class, runtime.getKeyspace().getContainer());
                    beanMap.put(TestSettings.class, runtime.getTestSettings());
                }

                if (!beanMap.containsKey(field.getType())) {
                    throw new CassandraTestException("Field type %s not supported by CassandraBean",
                            field.getType().getCanonicalName());
                }

                field.setAccessible(true);

                try {
                    field.set(test, beanMap.get(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new CassandraTestException("Cannot set CassandraBean field '%s'", field.getName(), e);
                }
            }
        }
    }
}

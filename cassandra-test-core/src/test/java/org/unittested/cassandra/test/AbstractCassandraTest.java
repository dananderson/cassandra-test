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

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.unittested.cassandra.test.annotation.CassandraBean;
import org.unittested.cassandra.test.properties.PropertiesPropertyResolver;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public abstract class AbstractCassandraTest {

    @CassandraBean
    private Session session;

    @CassandraBean
    private Cluster cluster;

    @CassandraBean
    private Keyspace keyspace;

    private TestEnvironmentAdapter adapter;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception {
        this.adapter = createTestEnvironmentAdapter(getClass());
        this.adapter.onBeforeClass(getClass(), null);
    }

    @BeforeClass(alwaysRun = true, dependsOnMethods = "beforeClass")
    public void prepareTestInstance() throws Exception {
        this.adapter.onPrepareTestInstance(this);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception {
        if (this.adapter != null) {
            this.adapter.onAfterClass(getClass());
            this.adapter = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method testMethod) throws Exception {
        if (this.adapter != null) {
            this.adapter.onBeforeMethod(this, testMethod);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(Method testMethod) throws Exception {
        if (this.adapter != null) {
            this.adapter.onAfterMethod(this, testMethod);
        }
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

    private TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass) {
        return new TestEnvironmentAdapter(
            new TestSettingsBuilder()
                    .withDefaultPropertyResolver(PropertiesPropertyResolver.DEFAULT)
                    .withTestClass(testClass)
                    .build());
    }
}

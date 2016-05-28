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

package org.unittested.cassandra.test.testng;

import java.lang.reflect.Method;

import org.unittested.cassandra.test.Keyspace;
import org.unittested.cassandra.test.KeyspaceContainer;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.TestSettingsBuilder;
import org.unittested.cassandra.test.TestSettings;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.unittested.cassandra.test.annotation.CassandraBean;
import org.unittested.cassandra.test.property.PropertyResolver;
import org.unittested.cassandra.test.property.system.JavaPropertyResolver;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public abstract class AbstractTestNgCassandraTest {

    private TestEnvironmentAdapter adapter;

    private PropertyResolver propertyResolver;

    @CassandraBean
    private Session session;

    @CassandraBean
    private Cluster cluster;

    @CassandraBean
    private Keyspace keyspace;

    @CassandraBean
    private KeyspaceContainer keyspaceContainer;

    public AbstractTestNgCassandraTest() {
        this.propertyResolver = new JavaPropertyResolver();
    }

    public AbstractTestNgCassandraTest(final PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception {
        TestSettings settings = TestSettingsBuilder.fromAnnotatedElement(getClass(), this.propertyResolver);
        this.adapter = new TestEnvironmentAdapter(settings);
        this.adapter.onBeforeClass(this, null);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception {
        if (this.adapter != null) {
            this.adapter.onAfterClass(this, null);
            this.adapter = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method testMethod) throws Exception {
        if (this.adapter != null) {
            this.adapter.onBeforeMethod(this, testMethod, null);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(Method testMethod) throws Exception {
        if (this.adapter != null) {
            this.adapter.onAfterMethod(this, testMethod, null);
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

    protected KeyspaceContainer getKeyspaceContainer() {
        return this.keyspaceContainer;
    }
}

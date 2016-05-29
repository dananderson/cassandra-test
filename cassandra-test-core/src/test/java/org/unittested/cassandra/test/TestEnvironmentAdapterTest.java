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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.mockito.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.annotation.CassandraBean;
import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.state.KeyspaceStateManager;
import org.unittested.cassandra.test.rollback.RollbackSettings;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class TestEnvironmentAdapterTest {

    @Test
    public void onBeforeClass() throws Exception {
        // given
        TestSettings testSettings = createSettings();
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(testSettings);
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        when(session.getCluster()).thenReturn(cluster);
        when(testSettings.getConnectSettings().connect()).thenReturn(session);

        // when
        adapter.onBeforeClass(MockTest.class, null);

        // then
        verify(testSettings.getConnectSettings(), times(1)).connect();
        verifyNoMoreInteractions(testSettings.getConnectSettings());

        assertThat(adapter.getRuntime(), notNullValue());
        assertThat(adapter.getRuntime().getTestSettings(), is(testSettings));
        assertThat(adapter.getRuntime().getTest(), nullValue());
        assertThat(adapter.getRuntime().getKeyspace(), notNullValue());
        assertThat(adapter.getRuntime().getKeyspace().getSession(), is(session));
        assertThat(adapter.getRuntime().getTestEnvironmentContext(), nullValue());
        assertThat(adapter.getRuntime().getTestMethod(), nullValue());
        assertThat(MockTest.class.equals(adapter.getRuntime().getTestClass()), is(true));
    }

    @Test
    public void onPrepareTestInstance() throws Exception {
        // given
        TestSettings testSettings = createSettings();
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(testSettings);
        MockTest test = new MockTest();
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        when(session.getCluster()).thenReturn(cluster);
        when(testSettings.getConnectSettings().connect()).thenReturn(session);

        adapter.onBeforeClass(MockTest.class, null);

        // when
        adapter.onPrepareTestInstance(test, null);

        // then
        assertThat(test.cluster, is(cluster));
        assertThat(test.session, is(session));
        assertThat(test.keyspace, notNullValue());
        assertThat(test.keyspaceContainer, notNullValue());
        assertThat(test.testSettings, is(testSettings));
    }

    @DataProvider
    public static Object[][] cassandraBeanClasses() {
        return new Object[][] {
                { new CassandraBeanWithUnsupportedType() },
                { new CassandraBeanWithFinal() },
                { new CassandraBeanWithStatic() },
        };
    }

    @Test(dataProvider = "cassandraBeanClasses", expectedExceptions = CassandraTestException.class)
    public void onPrepareTestInstancePopulateCassandraBeanFail(Object test) throws Exception {
        // given
        TestSettings testSettings = createSettings();
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(testSettings);
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        when(session.getCluster()).thenReturn(cluster);
        when(testSettings.getConnectSettings().connect()).thenReturn(session);

        adapter.onBeforeClass(MockTest.class, null);

        // when
        adapter.onPrepareTestInstance(test, null);

        // then
        // CassandraTestException
    }

    @Test
    public void onPrepareTestInstanceUninitialized() throws Exception {
        // given
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(createSettings());

        // when
        adapter.onPrepareTestInstance(this, null);

        // then
        // no exception on uninitialized adapter (onBeforeClass not called).
    }

    @Test
    public void onAfterClass() throws Exception {
        // given
        TestSettings testSettings = createSettings();
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(testSettings);
        MockTest test = new MockTest();
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        when(session.getCluster()).thenReturn(cluster);
        when(testSettings.getConnectSettings().connect()).thenReturn(session);

        adapter.onBeforeClass(MockTest.class, null);
        adapter.onPrepareTestInstance(test, null);

        // when
        adapter.onAfterClass(MockTest.class, null);

        // then
        verify(cluster, times(1)).close();
        verify(testSettings.getRollbackSettings(), times(1)).rollbackAfterClass(Matchers.any(TestRuntime.class));
        verifyNoMoreInteractions(cluster, testSettings.getRollbackSettings());
    }

    @Test
    public void onAfterClassUninitialized() throws Exception {
        // given
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(createSettings());

        // when
        adapter.onAfterClass(MockTest.class, null);

        // then
        // no exception on uninitialized adapter (onBeforeClass not called).
    }

    @Test
    public void onBeforeMethod() throws Exception {
        // given
        TestSettings testSettings = createSettings();
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(testSettings);
        MockTest test = new MockTest();
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        when(session.getCluster()).thenReturn(cluster);
        when(testSettings.getConnectSettings().connect()).thenReturn(session);
        when(testSettings.getKeyspaceSettings().getKeyspace()).thenReturn("test");

        adapter.onBeforeClass(MockTest.class, null);
        adapter.onPrepareTestInstance(test, null);

        // when
        adapter.onBeforeMethod(test, test.getMethod(), null);

        // then
        assertThat(adapter.getRuntime().getTestMethod(), is(test.getMethod()));
        verify(testSettings.getKeyspaceSettings(), times(1)).sync(
                Matchers.any(TestRuntime.class), Matchers.any(KeyspaceStateManager.class));
        verify(testSettings.getDataSettings(), times(1)).load(Matchers.any(TestRuntime.class));
        verify(session, times(1)).execute("use \"test\";");
        verifyNoMoreInteractions(testSettings.getDataSettings());
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void onBeforeMethodUninitialized() throws Exception {
        // given
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(createSettings());
        MockTest test = new MockTest();

        // when
        adapter.onBeforeMethod(test, test.getMethod(), null);

        // then
        // CassandraTestException
    }

    @Test
    public void onAfterMethod() throws Exception {
        // given
        TestSettings testSettings = createSettings();
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(testSettings);
        MockTest test = new MockTest();
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        when(session.getCluster()).thenReturn(cluster);
        when(testSettings.getConnectSettings().connect()).thenReturn(session);

        adapter.onBeforeClass(MockTest.class, null);
        adapter.onPrepareTestInstance(test, null);

        // when
        adapter.onAfterMethod(test, test.getMethod(), null);

        // then
        assertThat(adapter.getRuntime().getTestMethod(), nullValue());
        verify(testSettings.getRollbackSettings(), times(1)).rollbackAfterMethod(adapter.getRuntime());
        verifyNoMoreInteractions(testSettings.getRollbackSettings());
    }

    @Test
    public void onAfterMethodUninitialized() throws Exception {
        // given
        TestEnvironmentAdapter adapter = new TestEnvironmentAdapter(createSettings());
        MockTest test = new MockTest();

        // when
        adapter.onAfterMethod(test, test.getMethod(), null);

        // then
        // no exception on uninitialized adapter (onBeforeClass not called).
    }

    private TestSettings createSettings() {
        return new TestSettings(
                mock(ConnectSettings.class),
                mock(KeyspaceSettings.class),
                mock(DataSettings.class),
                mock(RollbackSettings.class));
    }

    private static class MockTest {
        @CassandraBean
        Session session;

        @CassandraBean
        Cluster cluster;

        @CassandraBean
        Keyspace keyspace;

        @CassandraBean
        KeyspaceContainer keyspaceContainer;

        @CassandraBean
        TestSettings testSettings;

        Method getMethod() {
            return MockTest.class.getDeclaredMethods()[0];
        }
    }

    private static class CassandraBeanWithUnsupportedType {
        @CassandraBean
        Object bad;
    }

    private static class CassandraBeanWithFinal {
        @CassandraBean
        final Keyspace bad = null;
    }

    private static class CassandraBeanWithStatic {
        @CassandraBean
        static Keyspace bad;
    }
}

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

package org.unittested.cassandra.test.spring;

import static org.mockito.Mockito.*;

import org.springframework.test.context.TestContext;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

public class SpringCassandraTestExecutionListenerTest {

    @Test
    public void beforeTestClass() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        // when
        listener.beforeTestClass(testContext);

        // then
        verify(listener.adapter, times(1)).onBeforeClass(testContext.getTestClass(), testContext);
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void beforeTestClassWithNullAdapter() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        listener.adapter = null;

        // when
        listener.beforeTestClass(testContext);

        // then
        // CassandraTestException
    }

    @Test
    public void afterTestClass() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        listener.beforeTestClass(testContext);
        reset(listener.adapter);

        // when
        listener.afterTestClass(testContext);

        // then
        verify(listener.adapter, times(1)).onAfterClass(testContext.getTestClass(), testContext);
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test
    public void afterTestClassButNotInitialized() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        // when
        listener.afterTestClass(testContext);

        // then
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test
    public void beforeTestMethod() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        listener.beforeTestClass(testContext);
        reset(listener.adapter);

        // when
        listener.beforeTestMethod(testContext);

        // then
        verify(listener.adapter, times(1)).onBeforeMethod(testContext.getTestInstance(), testContext.getTestMethod(), testContext);
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test
    public void beforeTestMethodButNotInitialized() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        // when
        listener.beforeTestMethod(testContext);

        // then
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test
    public void afterTestMethod() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        listener.beforeTestClass(testContext);
        listener.beforeTestMethod(testContext);
        reset(listener.adapter);

        // when
        listener.afterTestMethod(testContext);

        // then
        verify(listener.adapter, times(1)).onAfterMethod(testContext.getTestInstance(), testContext.getTestMethod(), testContext);
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test
    public void afterTestMethodButNotInitialized() throws Exception {
        // given
        Listener listener = new Listener();
        TestContext testContext = createTestContext();

        // when
        listener.afterTestMethod(testContext);

        // then
        verifyNoMoreInteractions(listener.adapter);
    }

    @Test
    public void defaultConstructor() throws Exception {
        SpringCassandraTestExecutionListener.class.newInstance();
    }

    @SuppressWarnings("unchecked")
    private TestContext createTestContext() {
        TestContext testContext = mock(TestContext.class);

        when(testContext.getTestInstance()).thenReturn(this);
        when(testContext.getTestClass()).thenReturn((Class)getClass());
        when(testContext.getTestMethod()).thenReturn(getClass().getDeclaredMethods()[0]);

        return testContext;
    }

    public static class Listener extends SpringCassandraTestExecutionListener {

        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);

        @Override
        protected TestEnvironmentAdapter createTestEnvironmentAdapter(final TestContext testContext) {
            return this.adapter;
        }
    }
}

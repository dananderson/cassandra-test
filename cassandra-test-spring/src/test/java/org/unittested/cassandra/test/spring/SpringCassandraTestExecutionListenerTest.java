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

import java.lang.reflect.Method;

import org.springframework.test.context.TestContext;
import org.testng.annotations.Test;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

public class SpringCassandraTestExecutionListenerTest {

    @Test
    public void lifecycle() throws Exception {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        SpringCassandraTestExecutionListener listener = createListener(adapter);
        Method testMethod = getClass().getDeclaredMethods()[0];
        TestContext testContext = mock(TestContext.class);
        @SuppressWarnings("unchecked")
        Class testClass = getClass();

        when(testContext.getTestMethod()).thenReturn(testMethod);
        when(testContext.getTestClass()).thenReturn(testClass);
        when(testContext.getTestInstance()).thenReturn(this);

        // when
        listener.beforeTestClass(testContext);
        listener.prepareTestInstance(testContext);
        listener.beforeTestMethod(testContext);
        listener.afterTestMethod(testContext);
        listener.afterTestClass(testContext);

        // then
        verify(adapter, times(1)).onBeforeClass(getClass(), testContext);
        verify(adapter, times(1)).onPrepareTestInstance(this, testContext);
        verify(adapter, times(1)).onBeforeMethod(this, testMethod, testContext);
        verify(adapter, times(1)).onAfterMethod(this, testMethod, testContext);
        verify(adapter, times(1)).onAfterClass(getClass(), testContext);
        verifyNoMoreInteractions(adapter);
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void beforeClassWithNull() throws Exception {
        // given
        TestContext testContext = mock(TestContext.class);
        SpringCassandraTestExecutionListener listener = createListener(null);

        // when
        listener.beforeTestClass(testContext);

        // then
        // CassandraTestException
    }

    private SpringCassandraTestExecutionListener createListener(final TestEnvironmentAdapter adapter) {
        return new SpringCassandraTestExecutionListener() {
            @Override
            protected TestEnvironmentAdapter createTestEnvironmentAdapter(TestContext testContext) {
                return adapter;
            }
        };
    }

    @Test
    public void defaultConstructor() throws Exception {
        SpringCassandraTestExecutionListener.class.newInstance();
    }
}

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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.testng.annotations.Test;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

public class AbstractTestNGCassandraTestTest extends AbstractTestNGCassandraTest {

    @Test
    public void getters() throws Exception {
        assertThat(getSession(), notNullValue());
        assertThat(getKeyspace(), notNullValue());
        assertThat(getCluster(), notNullValue());
    }

    @Test
    public void lifecycle() throws Exception {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        AbstractTestNGCassandraTest base = createBase(adapter);
        Method testMethod = AbstractTestNGCassandraTestTest.class.getDeclaredMethods()[0];

        // when
        base.beforeClass();
        base.prepareTestInstance();
        base.beforeMethod(testMethod);
        base.afterMethod(testMethod);
        base.afterClass();

        // then
        verify(adapter, times(1)).onBeforeClass(base.getClass());
        verify(adapter, times(1)).onPrepareTestInstance(base);
        verify(adapter, times(1)).onBeforeMethod(base, testMethod);
        verify(adapter, times(1)).onAfterMethod(base, testMethod);
        verify(adapter, times(1)).onAfterClass(base.getClass());
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void lifecycleAfterSetupFailure() throws Exception {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        AbstractTestNGCassandraTest base = createBase(adapter);
        Method testMethod = AbstractTestNGCassandraTestTest.class.getDeclaredMethods()[0];

        // when
        base.prepareTestInstance();
        base.beforeMethod(testMethod);
        base.afterMethod(testMethod);
        base.afterClass();

        // then
        verifyNoMoreInteractions(adapter);
    }

    @Test(expectedExceptions = CassandraTestException.class)
    public void beforeClassWithNullAdapter() throws Exception {
        // given
        AbstractTestNGCassandraTest base = createBase(null);

        // when
        base.beforeClass();

        // then
        // CassandraTestException
    }

    private AbstractTestNGCassandraTest createBase(final TestEnvironmentAdapter adapter) {
        return new AbstractTestNGCassandraTest() {
            @Override
            TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass) {
                return adapter;
            }
        };
    }
}

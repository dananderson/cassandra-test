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

package org.unittested.cassandra.test.junit.rule;

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.mockito.Matchers;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

@RunWith(Parameterized.class)
public class CassandraTestTest {

    @Test
    public void evaluate() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraTestInit init = createCassandraTestInit(adapter);
        Description methodDescription = createDescription(getClass(), "evaluate");
        Statement testMethodInitStatement = createTestMethodInitStatement(init, this, mock(Statement.class), methodDescription);
        Statement initStatement = init.apply(testMethodInitStatement, mock(Description.class));

        // when
        initStatement.evaluate();

        // then
        verify(adapter, times(1)).onBeforeMethod(eq(this), Matchers.any(Method.class), eq(null));
        verify(adapter, times(1)).onAfterMethod(eq(this), Matchers.any(Method.class), eq(null));
        verify(adapter, times(1)).onPrepareTestInstance(eq(this), eq(null));
    }
    @Test
    public void evaluateWithChildStatementFailure() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraTestInit init = createCassandraTestInit(adapter);
        Description methodDescription = createDescription(getClass(), "evaluate");
        Statement testMethodCallingStatement = mock(Statement.class);
        Statement testMethodInitStatement = spy(createTestMethodInitStatement(init, this, testMethodCallingStatement, methodDescription));
        Statement initStatement = init.apply(testMethodInitStatement, mock(Description.class));

        doThrow(Exception.class).when(testMethodCallingStatement).evaluate();

        // when
        try {
            initStatement.evaluate();
        } catch (Exception e) {
            // ignore
        }

        // then
        verify(adapter, times(1)).onBeforeMethod(eq(this), Matchers.any(Method.class), eq(null));
        verify(adapter, times(1)).onAfterMethod(eq(this), Matchers.any(Method.class), eq(null));
        verify(adapter, times(1)).onPrepareTestInstance(eq(this), eq(null));
    }

    @Parameterized.Parameters
    public static Object[][] evaluateWithTestClassMismatchParameters() {
        return new Object [][] {
            { Object.class, "evaluate"},
            { null, "evaluate"},
            { CassandraTestTest.class, "does not exist" },
            { CassandraTestTest.class, "createDescription" },
        };
    }

    @Parameterized.Parameter(0)
    public Class<?> paramTestClass;

    @Parameterized.Parameter(1)
    public String paramMethodName;

    @Test(expected = CassandraTestException.class)
    public void evaluateWithTestClassMismatch() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraTestInit init = createCassandraTestInit(adapter);
        Description methodDescription = createDescription(this.paramTestClass, this.paramMethodName);
        Statement testMethodInitStatement = spy(createTestMethodInitStatement(init, this, mock(Statement.class), methodDescription));
        Statement initStatement = init.apply(testMethodInitStatement, mock(Description.class));

                // when
        initStatement.evaluate();

        // then
        // CassandraTestException
    }

    @Test(expected = CassandraTestException.class)
    public void constructorError() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);

        doThrow(new Exception()).when(adapter).onPrepareTestInstance(this, null);

        CassandraTestInit init = createCassandraTestInit(adapter);

        // when
        new CassandraTest(init, this);

        // then
        // CassandraTestException
    }

    private CassandraTestInit createCassandraTestInit(final TestEnvironmentAdapter adapter) {
        return new CassandraTestInit() {
            @Override
            TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass, Object testEnvironmentContext) {
                return adapter;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Description createDescription(Class<?> testClass, String methodName) {
        Description description = mock(Description.class);

        when(description.getTestClass()).thenReturn((Class)testClass);
        when(description.getMethodName()).thenReturn(methodName);

        return description;
    }

    private Statement createTestMethodInitStatement(final CassandraTestInit init,
                                                final Object testInstance,
                                                final Statement testMethodCallingStatement,
                                                final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                new CassandraTest(init, testInstance).apply(testMethodCallingStatement, description).evaluate();
            }
        };
    }

}

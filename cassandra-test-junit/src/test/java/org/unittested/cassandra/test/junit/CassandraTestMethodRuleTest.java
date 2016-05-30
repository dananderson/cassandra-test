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

package org.unittested.cassandra.test.junit;

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
public class CassandraTestMethodRuleTest {

    @Test
    public void evaluate() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        TestEnvironmentAdapterProvider provider = createProvider(adapter);
        CassandraTestMethodRule rule = new CassandraTestMethodRule(provider, this);
        Description description = createDescription(getClass(), "evaluate");
        Statement statement = rule.apply(mock(Statement.class), description);

        // when
        statement.evaluate();

        // then
        verify(adapter, times(1)).onBeforeMethod(eq(this), Matchers.any(Method.class), eq(null));
        verify(adapter, times(1)).onAfterMethod(eq(this), Matchers.any(Method.class), eq(null));
        verify(adapter, times(1)).onPrepareTestInstance(eq(this), eq(null));
    }
    @Test
    public void evaluateWithChildStatementFailure() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        TestEnvironmentAdapterProvider provider = createProvider(adapter);
        CassandraTestMethodRule rule = new CassandraTestMethodRule(provider, this);
        Description description = createDescription(getClass(), "evaluate");
        Statement baseStatement = mock(Statement.class);
        Statement statement = rule.apply(baseStatement, description);

        doThrow(Exception.class).when(baseStatement).evaluate();

        // when
        try {
            statement.evaluate();
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
            { CassandraTestMethodRuleTest.class, "does not exist" },
            { CassandraTestMethodRuleTest.class, "createDescription" },
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
        TestEnvironmentAdapterProvider provider = createProvider(adapter);
        CassandraTestMethodRule rule = new CassandraTestMethodRule(provider, this);
        Description description = createDescription(this.paramTestClass, this.paramMethodName);
        Statement statement = rule.apply(mock(Statement.class), description);

        // when
        statement.evaluate();

        // then
        // CassandraTestException
    }

    @Test(expected = CassandraTestException.class)
    public void constructorError() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);

        doThrow(new Exception()).when(adapter).onPrepareTestInstance(this, null);

        TestEnvironmentAdapterProvider provider = createProvider(adapter);

        // when
        new CassandraTestMethodRule(provider, this);

        // then
        // CassandraTestException
    }

    private TestEnvironmentAdapterProvider createProvider(final TestEnvironmentAdapter adapter) {
        return new TestEnvironmentAdapterProvider() {
            @Override
            public TestEnvironmentAdapter getAdapter() {
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

}

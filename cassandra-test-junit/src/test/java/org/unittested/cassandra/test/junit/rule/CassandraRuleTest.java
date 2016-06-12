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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;

public class CassandraRuleTest {

    @Test
    public void evaluate() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraClassRule classRule = createCassandraClassRule(adapter);
        CassandraRule test = new CassandraRule(classRule);
        Statement calledStatement = mock(Statement.class);
        FrameworkMethod testMethod = new FrameworkMethod(getClass().getDeclaredMethods()[0]);
        Statement statement = test.apply(calledStatement, testMethod, this);

        // when
        statement.evaluate();

        // then
        verify(calledStatement, times(1)).evaluate();
        verify(adapter, times(1)).onPrepareTestInstance(this);
        verify(adapter, times(1)).onBeforeMethod(this, testMethod.getMethod());
        verify(adapter, times(1)).onAfterMethod(this, testMethod.getMethod());
    }
    @Test
    public void evaluateWhenPrepared() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraClassRule classRule = createCassandraClassRule(adapter);
        CassandraRule test = new CassandraRule(classRule);
        Statement calledStatement = mock(Statement.class);
        FrameworkMethod testMethod = new FrameworkMethod(getClass().getDeclaredMethods()[0]);
        Statement statement = test.apply(calledStatement, testMethod, this);

        statement.evaluate();
        reset(calledStatement, adapter);

        // when
        statement.evaluate();

        // then
        verify(calledStatement, times(1)).evaluate();
        verify(adapter, times(0)).onPrepareTestInstance(this);
        verify(adapter, times(1)).onBeforeMethod(this, testMethod.getMethod());
        verify(adapter, times(1)).onAfterMethod(this, testMethod.getMethod());
    }

    @Test
    public void evaluateAndPrepareInstanceFailure() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraClassRule classRule = createCassandraClassRule(adapter);
        CassandraRule test = new CassandraRule(classRule);
        Statement calledStatement = mock(Statement.class);
        FrameworkMethod testMethod = new FrameworkMethod(getClass().getDeclaredMethods()[0]);
        Statement statement = test.apply(calledStatement, testMethod, this);

        doThrow(new RuntimeException()).when(adapter).onPrepareTestInstance(this);

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            verifyZeroInteractions(calledStatement);
            verify(adapter, times(1)).onPrepareTestInstance(this);
            verify(adapter, times(0)).onBeforeMethod(this, testMethod.getMethod());
            verify(adapter, times(0)).onAfterMethod(this, testMethod.getMethod());

            return;
        }

        Assert.fail("Expected exception from onPrepareInstance.");
    }

    @Test
    public void evaluateAndBeforeMethodFailure() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraClassRule classRule = createCassandraClassRule(adapter);
        CassandraRule test = new CassandraRule(classRule);
        Statement calledStatement = mock(Statement.class);
        FrameworkMethod testMethod = new FrameworkMethod(getClass().getDeclaredMethods()[0]);
        Statement statement = test.apply(calledStatement, testMethod, this);

        doThrow(new RuntimeException()).when(adapter).onBeforeMethod(this, testMethod.getMethod());

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            verifyZeroInteractions(calledStatement);
            verify(adapter, times(1)).onPrepareTestInstance(this);
            verify(adapter, times(1)).onBeforeMethod(this, testMethod.getMethod());
            verify(adapter, times(1)).onAfterMethod(this, testMethod.getMethod());

            return;
        }

        Assert.fail("Expected exception from onBeforeMethod.");
    }

    @Test
    public void evaluateAndAfterMethodFailure() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraClassRule classRule = createCassandraClassRule(adapter);
        CassandraRule test = new CassandraRule(classRule);
        Statement calledStatement = mock(Statement.class);
        FrameworkMethod testMethod = new FrameworkMethod(getClass().getDeclaredMethods()[0]);
        Statement statement = test.apply(calledStatement, testMethod, this);

        doThrow(new RuntimeException()).when(adapter).onAfterMethod(this, testMethod.getMethod());

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            verify(calledStatement, times(1)).evaluate();
            verify(adapter, times(1)).onPrepareTestInstance(this);
            verify(adapter, times(1)).onBeforeMethod(this, testMethod.getMethod());
            verify(adapter, times(1)).onAfterMethod(this, testMethod.getMethod());

            return;
        }

        Assert.fail("Expected exception from onAfterMethod.");
    }

    @Test
    public void evaluateAndStatementFailure() throws Throwable {
        // given
        TestEnvironmentAdapter adapter = mock(TestEnvironmentAdapter.class);
        CassandraClassRule classRule = createCassandraClassRule(adapter);
        CassandraRule test = new CassandraRule(classRule);
        Statement calledStatement = mock(Statement.class);
        FrameworkMethod testMethod = new FrameworkMethod(getClass().getDeclaredMethods()[0]);
        Statement statement = test.apply(calledStatement, testMethod, this);

        doThrow(new RuntimeException()).when(calledStatement).evaluate();

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            verify(calledStatement, times(1)).evaluate();
            verify(adapter, times(1)).onPrepareTestInstance(this);
            verify(adapter, times(1)).onBeforeMethod(this, testMethod.getMethod());
            verify(adapter, times(1)).onAfterMethod(this, testMethod.getMethod());

            return;
        }

        Assert.fail("Expected exception from evaluate.");
    }

    private CassandraClassRule createCassandraClassRule(final TestEnvironmentAdapter adapter) {
        return new CassandraClassRule() {
            @Override
            public TestEnvironmentAdapter getAdapter() {
                return adapter;
            }
        };
    }

}

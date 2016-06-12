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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

public class CassandraClassRuleTest {

    @Test
    public void apply() throws Exception {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);

        // when
        Statement statement = classRule.apply(mock(Statement.class), description);

        // then
        assertThat(statement, notNullValue());
        verifyZeroInteractions(classRule.mockAdapter);
        assertThat(classRule.getAdapter(), nullValue());
    }

    @Test
    public void evaluate() throws Throwable {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);
        Statement childStatement = mock(Statement.class);
        Statement statement = classRule.apply(childStatement, description);

        // when
        statement.evaluate();

        // then
        assertThat(classRule.getAdapter(), nullValue());
        verify(childStatement, times(1)).evaluate();
        verify(classRule.mockAdapter, times(1)).onBeforeClass(CassandraClassRuleTest.class);
        verify(classRule.mockAdapter, times(1)).onAfterClass(CassandraClassRuleTest.class);
    }

    @Test
    public void evaluateWithBeforeClassFailure() throws Throwable {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);
        Statement childStatement = mock(Statement.class);
        Statement statement = classRule.apply(childStatement, description);

        doThrow(Exception.class).when(classRule.mockAdapter).onBeforeClass(CassandraClassRuleTest.class);

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            assertThat(classRule.getAdapter(), nullValue());
            verifyZeroInteractions(childStatement);
            verify(classRule.mockAdapter, times(1)).onBeforeClass(CassandraClassRuleTest.class);
            verify(classRule.mockAdapter, times(0)).onAfterClass(CassandraClassRuleTest.class);

            return;
        }

        Assert.fail("Expected evaluate to throw an exception.");
    }

    @Test
    public void evaluateWithAfterClassFailure() throws Throwable {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);
        Statement childStatement = mock(Statement.class);
        Statement statement = classRule.apply(childStatement, description);

        doThrow(Exception.class).when(classRule.mockAdapter).onAfterClass(CassandraClassRuleTest.class);

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            assertThat(classRule.getAdapter(), nullValue());
            verify(childStatement, times(1)).evaluate();
            verify(classRule.mockAdapter, times(1)).onBeforeClass(CassandraClassRuleTest.class);
            verify(classRule.mockAdapter, times(1)).onAfterClass(CassandraClassRuleTest.class);

            return;
        }

        Assert.fail("Expected evaluate to throw an exception.");
    }



    @Test
    public void evaluateWithChildStatementFailure() throws Throwable {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);
        Statement childStatement = mock(Statement.class);
        Statement statement = classRule.apply(childStatement, description);

        doThrow(Exception.class).when(childStatement).evaluate();

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            assertThat(classRule.getAdapter(), nullValue());
            verify(childStatement, times(1)).evaluate();
            verify(classRule.mockAdapter, times(1)).onBeforeClass(CassandraClassRuleTest.class);
            verify(classRule.mockAdapter, times(1)).onAfterClass(CassandraClassRuleTest.class);

            return;
        }

        Assert.fail("Expected evaluate to throw an exception.");
    }

    @Test
    public void evaluateWithChildStatementFailureAndAfterClassFailure() throws Throwable {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);
        Statement baseStatement = mock(Statement.class);
        Statement statement = classRule.apply(mock(Statement.class), description);

        doThrow(Exception.class).when(classRule.mockAdapter).onAfterClass(CassandraClassRuleTest.class);
        doThrow(Exception.class).when(baseStatement).evaluate();

        try {
            // when
            statement.evaluate();
        } catch (Exception e) {
            // then
            assertThat(classRule.getAdapter(), nullValue());
            verify(classRule.mockAdapter, times(1)).onBeforeClass(CassandraClassRuleTest.class);
            verify(classRule.mockAdapter, times(1)).onAfterClass(CassandraClassRuleTest.class);

            return;
        }

        Assert.fail("Expected evaluate to throw an exception.");
    }

    @Test(expected = CassandraTestException.class)
    public void evaluateWithNullAdapter() throws Throwable {
        // given
        MyCassandraClassRule classRule = new MyCassandraClassRule();
        Description description = createDescription(CassandraClassRuleTest.class);
        Statement statement = classRule.apply(mock(Statement.class), description);

        classRule.mockAdapter = null;

        // when
        statement.evaluate();

        // then
        // CassandraTestException
    }

    private Description createDescription(Class<?> testClass) {
        return Description.createTestDescription(testClass, testClass.getCanonicalName());
    }

    public static final class MyCassandraClassRule extends CassandraClassRule {

        TestEnvironmentAdapter mockAdapter = mock(TestEnvironmentAdapter.class);

        @Override
        protected TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass) {
            return this.mockAdapter;
        }
    }
}

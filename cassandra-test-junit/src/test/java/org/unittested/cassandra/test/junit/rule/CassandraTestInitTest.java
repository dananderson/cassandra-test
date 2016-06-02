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

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.exception.CassandraTestException;

public class CassandraTestInitTest {

    @Test
    @SuppressWarnings("unchecked")
    public void evaluate() throws Throwable {
        // given
        MyCassandraTestInit init = new MyCassandraTestInit();
        Description description = mock(Description.class);

        when(description.getTestClass()).thenReturn((Class)CassandraTestInitTest.class);

        Statement statement = init.apply(mock(Statement.class), description);

        assertThat(statement, notNullValue());
        verifyZeroInteractions(init.mockAdapter);
        assertThat(init.getAdapter(), nullValue());

        // when
        statement.evaluate();

        // then
        assertThat(init.getAdapter(), nullValue());
        verify(init.mockAdapter, times(1)).onBeforeClass(CassandraTestInitTest.class, null);
        verify(init.mockAdapter, times(1)).onAfterClass(CassandraTestInitTest.class, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluateWithChildStatementFailure() throws Throwable {
        // given
        MyCassandraTestInit init = new MyCassandraTestInit();
        Description description = mock(Description.class);

        when(description.getTestClass()).thenReturn((Class)CassandraTestInitTest.class);

        Statement baseStatement = mock(Statement.class);
        Statement statement = init.apply(mock(Statement.class), description);

        doThrow(Exception.class).when(baseStatement).evaluate();

        assertThat(statement, notNullValue());
        verifyZeroInteractions(init.mockAdapter);
        assertThat(init.getAdapter(), nullValue());

        // when
        try {
            statement.evaluate();
        } catch (Exception e) {
            // ignore
        }

        // then
        assertThat(init.getAdapter(), nullValue());
        verify(init.mockAdapter, times(1)).onBeforeClass(CassandraTestInitTest.class, null);
        verify(init.mockAdapter, times(1)).onAfterClass(CassandraTestInitTest.class, null);
    }

    @Test(expected = CassandraTestException.class)
    @SuppressWarnings("unchecked")
    public void evaluateWithNullAdapter() throws Throwable {
        // given
        MyCassandraTestInit init = new MyCassandraTestInit();

        init.mockAdapter = null;

        Description description = mock(Description.class);

        when(description.getTestClass()).thenReturn((Class)CassandraTestInitTest.class);

        Statement statement = init.apply(mock(Statement.class), description);

        // when
        statement.evaluate();

        // then
        // CassandraTestException
    }

    public static final class MyCassandraTestInit extends CassandraTestInit {

        TestEnvironmentAdapter mockAdapter = mock(TestEnvironmentAdapter.class);

        @Override
        TestEnvironmentAdapter createTestEnvironmentAdapter(Class<?> testClass, Object testEnvironmentContext) {
            return this.mockAdapter;
        }
    }
}

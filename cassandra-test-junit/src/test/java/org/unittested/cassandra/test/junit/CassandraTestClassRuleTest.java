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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.unittested.cassandra.test.TestEnvironmentAdapter;

public class CassandraTestClassRuleTest {

    @Test
    public void evaluate() throws Throwable {
        // given
        MyCassandraTestClassRule rule = new MyCassandraTestClassRule();
        Description description = mock(Description.class);

        when(description.getTestClass()).thenReturn((Class)CassandraTestClassRuleTest.class);

        Statement statement = rule.apply(mock(Statement.class), description);

        assertThat(statement, notNullValue());
        verifyZeroInteractions(rule.mockAdapter);
        assertThat(rule.getAdapter(), nullValue());

        // when
        statement.evaluate();

        // then
        assertThat(rule.getAdapter(), nullValue());
        verify(rule.mockAdapter, times(1)).onBeforeClass(CassandraTestClassRuleTest.class, null);
        verify(rule.mockAdapter, times(1)).onAfterClass(CassandraTestClassRuleTest.class, null);
    }

    public static final class MyCassandraTestClassRule extends CassandraTestClassRule {

        TestEnvironmentAdapter mockAdapter = mock(TestEnvironmentAdapter.class);

        @Override
        protected TestEnvironmentAdapter createTestEnvironmentAdapter(final Class<?> testClass) {
            return this.mockAdapter;
        }
    }
}

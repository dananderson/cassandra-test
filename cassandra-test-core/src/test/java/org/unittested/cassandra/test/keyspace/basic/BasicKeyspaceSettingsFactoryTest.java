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

package org.unittested.cassandra.test.keyspace.basic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.FactoryTestAnnotations;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertiesPropertyResolver;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BasicKeyspaceSettingsFactoryTest {

    @Test
    public void create() throws Exception {
        // given
        KeyspaceSettingsFactory keyspaceSettingsFactory = new BasicKeyspaceSettingsFactory();
        CassandraKeyspace cassandraKeyspace = FactoryTestAnnotations.class.getAnnotation(CassandraKeyspace.class);

        // when
        KeyspaceSettings keyspaceSettings = keyspaceSettingsFactory.create(cassandraKeyspace, PropertiesPropertyResolver.SYSTEM);

        // then
        assertThat(keyspaceSettings, instanceOf(BasicKeyspaceSettings.class));
        BasicKeyspaceSettings basicKeyspaceSettings = (BasicKeyspaceSettings)keyspaceSettings;
        assertThat(basicKeyspaceSettings.hashCode(), is(-1203568244));
        assertThat(basicKeyspaceSettings.getKeyspace(), is("test"));
        assertThat(basicKeyspaceSettings.canDropKeyspace(), is(true));
        assertThat(basicKeyspaceSettings.getProtectedTables(), arrayContaining("p"));
    }

    @DataProvider
    public Object[][] invalidAnnotations() {
        return new Object[][] {
                { null },
                { FactoryTestAnnotations.createStubAnnotation() },
        };
    }

    @Test(dataProvider = "invalidAnnotations", expectedExceptions = CassandraTestException.class)
    public void createWithInvalidAnnotations(Annotation annotation) throws Exception {
        // given
        KeyspaceSettingsFactory keyspaceSettingsFactory = new BasicKeyspaceSettingsFactory();

        // when
        keyspaceSettingsFactory.create(annotation, PropertiesPropertyResolver.SYSTEM);

        // then
        // expect IllegalArgumentException
    }
}

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

package org.unittested.cassandra.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import org.apache.commons.lang3.ArrayUtils;
import org.unittested.cassandra.test.annotation.CassandraConnect;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.connect.ConnectSettingsFactory;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettingsFactory;
import org.unittested.cassandra.test.data.DataSettingsFactory;
import org.unittested.cassandra.test.data.basic.BasicDataSettings;
import org.unittested.cassandra.test.data.basic.BasicDataSettingsFactory;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.rollback.RollbackSettingsFactory;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettings;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettingsFactory;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;
import org.unittested.cassandra.test.keyspace.basic.BasicKeyspaceSettingsFactory;
import org.unittested.cassandra.test.keyspace.basic.BasicKeyspaceSettings;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestSettingsBuilderTest {

    private static class WithDefaultSettings {}

    @CassandraConnect(port = "1234")
    private static class WithConnectSettings {}

    @CassandraData(data = "test")
    private static class WithDataSettings {}

    @CassandraKeyspace(keyspace = "test")
    private static class WithKeyspaceSettings {}

    @CassandraRollback(afterMethod = RollbackStrategy.DROP)
    private static class WithRollbackSettings {}

    @CassandraConnect(port = "1234")
    @CassandraData(data = "test")
    @CassandraKeyspace(keyspace = "test")
    @CassandraRollback(afterMethod = RollbackStrategy.DROP)
    private static class WithSettings {}

    @DataProvider
    public static Object[][] annotatedElements() {
        return new Object[][] {
                { WithDefaultSettings.class, 9042, Keyspace.NULL, ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.TRUNCATE },
                { WithConnectSettings.class, 1234, Keyspace.NULL, ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.TRUNCATE },
                { WithDataSettings.class, 9042, Keyspace.NULL, ArrayUtils.toArray("test"), RollbackStrategy.TRUNCATE },
                { WithKeyspaceSettings.class, 9042, "test", ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.TRUNCATE },
                { WithRollbackSettings.class, 9042, Keyspace.NULL, ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.DROP },
                { WithSettings.class, 1234, "test", ArrayUtils.toArray("test"), RollbackStrategy.DROP }
        };
    }

    @Test(dataProvider = "annotatedElements")
    public void fromAnnotatedElement(AnnotatedElement annotatedElement,
                                     int port, String expectedKeyspace,
                                     String [] expectedData,
                                     RollbackStrategy expectedAfterMethodRolback) throws Exception {
        // given
        // data provider

        // when
        TestSettings settings = TestSettingsBuilder.fromAnnotatedElement(annotatedElement);

        // then
        assertThat(settings, notNullValue());

        assertThat(settings.getConnectSettings(), instanceOf(BasicConnectSettings.class));
        assertThat(settings.getConnectSettings().getClusterBuilder().getConfiguration().getProtocolOptions().getPort(),
                is(port));

        assertThat(settings.getDataSettings(), instanceOf(BasicDataSettings.class));
        if (expectedData.length > 0) {
            assertThat(settings.getDataSettings().getData(), arrayContaining(expectedData));
        } else {
            assertThat(settings.getDataSettings().getData(), emptyArray());
        }

        assertThat(settings.getRollbackSettings(), instanceOf(BasicRollbackSettings.class));
        assertThat(settings.getRollbackSettings().getAfterMethod(), is(expectedAfterMethodRolback));

        assertThat(settings.getKeyspaceSettings(), instanceOf(BasicKeyspaceSettings.class));
        assertThat(settings.getKeyspaceSettings().getKeyspace(), is(expectedKeyspace));
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Connect {
        Class<? extends ConnectSettingsFactory> __connectSettingsFactory() default BasicConnectSettingsFactory.class;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Rollback {
        Class<? extends RollbackSettingsFactory> __rollbackSettingsFactory() default BasicRollbackSettingsFactory.class;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Schema {
        Class<? extends KeyspaceSettingsFactory> __keyspaceSettingsFactory() default BasicKeyspaceSettingsFactory.class;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Data {
        Class<? extends DataSettingsFactory> __dataSettingsFactory() default BasicDataSettingsFactory.class;
    }

    @Connect
    @CassandraConnect
    private static class WithDuplicateConnectSettings { }

    @Rollback
    @CassandraConnect
    private static class WithDuplicateRollbackSettings { }

    @Schema
    @CassandraKeyspace(keyspace = "test")
    private static class WithDuplicateKeyspaceSettings { }

    @Data
    @CassandraData(data = "test")
    private static class WithDuplicateDataSettings { }

    @DataProvider
    public static Object[][] duplicateSettings() {
        return new Object[][] {
                { WithDuplicateConnectSettings.class },
                { WithDuplicateRollbackSettings.class },
                { WithDuplicateKeyspaceSettings.class },
                { WithDuplicateDataSettings.class },
        };
    }

    @Test(dataProvider = "duplicateSettings", expectedExceptions = CassandraTestException.class)
    public void fromAnnotatedElementWithDuplicateSettings(AnnotatedElement annotatedElement) throws Exception {
        // given
        // data provider

        // when
        TestSettingsBuilder.fromAnnotatedElement(annotatedElement);

        // then
        // CassandraTestException
    }
}

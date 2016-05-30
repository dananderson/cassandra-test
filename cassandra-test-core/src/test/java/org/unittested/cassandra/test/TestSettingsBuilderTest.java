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
import static org.mockito.Mockito.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matcher;
import org.unittested.cassandra.test.annotation.CassandraConnect;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.connect.ConnectSettingsFactory;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettingsFactory;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.DataSettingsFactory;
import org.unittested.cassandra.test.data.basic.BasicDataSettings;
import org.unittested.cassandra.test.data.basic.BasicDataSettingsFactory;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.rollback.RollbackSettings;
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
    public static Object[][] testClasses() {
        return new Object[][] {
                { WithDefaultSettings.class, 9042, Keyspace.NULL, ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.TRUNCATE },
                { WithConnectSettings.class, 1234, Keyspace.NULL, ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.TRUNCATE },
                { WithDataSettings.class, 9042, Keyspace.NULL, ArrayUtils.toArray("test"), RollbackStrategy.TRUNCATE },
                { WithKeyspaceSettings.class, 9042, "test", ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.TRUNCATE },
                { WithRollbackSettings.class, 9042, Keyspace.NULL, ArrayUtils.EMPTY_STRING_ARRAY, RollbackStrategy.DROP },
                { WithSettings.class, 1234, "test", ArrayUtils.toArray("test"), RollbackStrategy.DROP }
        };
    }

    @Test(dataProvider = "testClasses")
    public void buildWithTestClass(Class<?> testClass,
                                   int port, String expectedKeyspace,
                                   String [] expectedData,
                                   RollbackStrategy expectedAfterMethodRolback) throws Exception {
        // given
        TestSettingsBuilder builder = new TestSettingsBuilder().withTestClass(testClass);

        // when
        TestSettings settings = builder.build();

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

    @DataProvider
    public static Object[][] defaultSettings() {
        ConnectSettings connectSettings = mock(ConnectSettings.class);
        KeyspaceSettings keyspaceSettings = mock(KeyspaceSettings.class);
        DataSettings dataSettings = mock(DataSettings.class);
        RollbackSettings rollbackSettings = mock(RollbackSettings.class);

        return new Object[][]{
            { connectSettings, null, null, null, is(connectSettings), notNullValue(), notNullValue(), notNullValue() },
            { null, keyspaceSettings, null, null, notNullValue(), is(keyspaceSettings), notNullValue(), notNullValue() },
            { null, null, dataSettings, null, notNullValue(), notNullValue(), is(dataSettings), notNullValue() },
            { null, null, null, rollbackSettings, notNullValue(), notNullValue(), notNullValue(), is(rollbackSettings) },
        };
    }

    @Test(dataProvider = "defaultSettings")
    public void buildWithDefaultSettings(ConnectSettings connectSettings,
                                         KeyspaceSettings keyspaceSettings,
                                         DataSettings dataSettings,
                                         RollbackSettings rollbackSettings,
                                         Matcher<ConnectSettings> expectedConnectSettings,
                                         Matcher<KeyspaceSettings> expectedKeyspaceSettings,
                                         Matcher<DataSettings> expectedDataSettings,
                                         Matcher<RollbackSettings> expectedRollbackSettings) throws Exception {
        // given
        TestSettingsBuilder builder = new TestSettingsBuilder()
                .withConnectSettings(connectSettings)
                .withDataSettings(dataSettings)
                .withKeyspaceSettings(keyspaceSettings)
                .withRollbackSettings(rollbackSettings)
                .withTestClass(WithSettings.class);

        // when
        TestSettings testSettings = builder.build();

        // then
        assertThat(testSettings.getConnectSettings(), expectedConnectSettings);
        assertThat(testSettings.getKeyspaceSettings(), expectedKeyspaceSettings);
        assertThat(testSettings.getDataSettings(), expectedDataSettings);
        assertThat(testSettings.getRollbackSettings(), expectedRollbackSettings);
    }

    @DataProvider
    public static Object[][] missingSetting() {
        ConnectSettings connectSettings = mock(ConnectSettings.class);
        KeyspaceSettings keyspaceSettings = mock(KeyspaceSettings.class);
        DataSettings dataSettings = mock(DataSettings.class);
        RollbackSettings rollbackSettings = mock(RollbackSettings.class);

        return new Object[][]{
                { connectSettings, keyspaceSettings, dataSettings, null },
                { connectSettings, keyspaceSettings, null, rollbackSettings },
                { connectSettings, null, dataSettings, rollbackSettings },
                { null, keyspaceSettings, dataSettings, rollbackSettings },
        };
    }

    @Test(dataProvider = "missingSetting", expectedExceptions = CassandraTestException.class)
    public void buildWithMissingSettingAndNoTestClass(ConnectSettings connectSettings,
                                        KeyspaceSettings keyspaceSettings,
                                        DataSettings dataSettings,
                                        RollbackSettings rollbackSettings) throws Exception {
        // given
        TestSettingsBuilder builder = new TestSettingsBuilder()
                .withConnectSettings(connectSettings)
                .withDataSettings(dataSettings)
                .withKeyspaceSettings(keyspaceSettings)
                .withRollbackSettings(rollbackSettings);

        // when
        builder.build();

        // then
        // CassandraTestException
    }

    @Test
    public void buildWithNoTestClass() throws Exception {
        // given
        ConnectSettings connectSettings = mock(ConnectSettings.class);
        KeyspaceSettings keyspaceSettings = mock(KeyspaceSettings.class);
        DataSettings dataSettings = mock(DataSettings.class);
        RollbackSettings rollbackSettings = mock(RollbackSettings.class);
        TestSettingsBuilder builder = new TestSettingsBuilder()
                .withConnectSettings(connectSettings)
                .withDataSettings(dataSettings)
                .withKeyspaceSettings(keyspaceSettings)
                .withRollbackSettings(rollbackSettings);

        // when
        TestSettings testSettings = builder.build();

        // then
        assertThat(testSettings.getConnectSettings(), is(connectSettings));
        assertThat(testSettings.getKeyspaceSettings(), is(keyspaceSettings));
        assertThat(testSettings.getDataSettings(), is(dataSettings));
        assertThat(testSettings.getRollbackSettings(), is(rollbackSettings));

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
    public void fromAnnotatedElementWithDuplicateSettings(Class<?> testClass) throws Exception {
        // given
        TestSettingsBuilder builder = new TestSettingsBuilder().withTestClass(testClass);

        // when
        builder.build();

        // then
        // CassandraTestException
    }
}

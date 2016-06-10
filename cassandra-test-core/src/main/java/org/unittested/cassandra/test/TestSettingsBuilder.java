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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.unittested.cassandra.test.annotation.CassandraProperties;
import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.basic.BasicDataSettings;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertyResolver;
import org.unittested.cassandra.test.properties.PropertiesPropertyResolver;
import org.unittested.cassandra.test.rollback.RollbackSettings;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.basic.BasicKeyspaceSettings;

/**
 * Helper for building {@link TestSettings} objects.
 */
public class TestSettingsBuilder {

    private static final Map<Class<?>, String> SETTINGS_FACTORY_PROPERTY_MAP;
    static {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();

        map.put(ConnectSettings.class, "__connectSettingsFactory");
        map.put(KeyspaceSettings.class, "__keyspaceSettingsFactory");
        map.put(DataSettings.class, "__dataSettingsFactory");
        map.put(RollbackSettings.class, "__rollbackSettingsFactory");

        SETTINGS_FACTORY_PROPERTY_MAP = Collections.unmodifiableMap(map);
    }

    private Class<?> testClass;

    private PropertyResolver propertyResolver;
    private PropertyResolver defaultPropertyResolver = PropertiesPropertyResolver.DEFAULT;

    private ConnectSettings connectSettings;
    private KeyspaceSettings keyspaceSettings;
    private DataSettings dataSettings;
    private RollbackSettings rollbackSettings;

    private Class<? extends ConnectSettings> defaultConnectSettingsClass = BasicConnectSettings.class;
    private Class<? extends KeyspaceSettings> defaultKeyspaceSettingsClass = BasicKeyspaceSettings.class;
    private Class<? extends DataSettings> defaultDataSettingsClass = BasicDataSettings.class;
    private Class<? extends RollbackSettings> defaultRollbackSettingsClass = BasicRollbackSettings.class;

    /**
     * Test class containing Cassandra Test annotations.
     * <p>
     * Each settings object type has an annotation that configures it. If the annotation is not present, the default
     * settings class will be used to create a new instance of the setting with default configuration.
     * <p>
     * The annotation reading and default instance creation can be overridden by specifying the settings instance in
     * this class. For example, if {@link #withConnectSettings(ConnectSettings)} is used, the connect setting annotation
     * will not be considered.
     *
     * @param testClass Test class.
     * @return this
     */
    public TestSettingsBuilder withTestClass(Class<?> testClass) {
        this.testClass = testClass;
        return this;
    }

    /**
     * {@link PropertyResolver} for resolving property references in annotations.
     * <p>
     * If specified, this will be the PropertyResolver that is chosen, taking precedence over PropertyResolvers specified
     * by a {@link CassandraProperties} annotation or the default PropertyResolver in this builder.
     *
     * @param propertyResolver {@link PropertyResolver}
     * @return this
     */
    public TestSettingsBuilder withPropertyResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
        return this;
    }

    /**
     * Default {@link PropertyResolver} used when a {@link CassandraProperties} is not present.
     *
     * @param propertyResolver {@link PropertyResolver}
     * @return this
     */
    public TestSettingsBuilder withDefaultPropertyResolver(PropertyResolver propertyResolver) {
        this.defaultPropertyResolver = propertyResolver;
        return this;
    }

    /**
     * Use this {@link ConnectSettings} object for {@link TestSettings}.
     * <p>
     * If specified, tbe connect settings annotation will be ignored.
     *
     * @param connectSettings {@link ConnectSettings}
     * @return this
     */
    public TestSettingsBuilder withConnectSettings(ConnectSettings connectSettings) {
        this.connectSettings = connectSettings;
        return this;
    }

    /**
     * The default {@link ConnectSettings} object to create if the connect settings is not present.
     *
     * @param defaultConnectSettingsClass {@link ConnectSettings} class
     * @return this
     */
    public TestSettingsBuilder withDefaultConnectSettings(Class<? extends ConnectSettings> defaultConnectSettingsClass) {
        this.defaultConnectSettingsClass = defaultConnectSettingsClass;
        return this;
    }

    /**
     * Use this {@link KeyspaceSettings} object for {@link TestSettings}.
     * <p>
     * If specified, tbe keyspace settings annotation will be ignored.
     *
     * @param keyspaceSettings {@link KeyspaceSettings}
     * @return this
     */
    public TestSettingsBuilder withKeyspaceSettings(KeyspaceSettings keyspaceSettings) {
        this.keyspaceSettings = keyspaceSettings;
        return this;
    }

    /**
     * The default {@link KeyspaceSettings} object to create if the keyspace settings is not present.
     *
     * @param defaultKeyspaceSettingsClass {@link KeyspaceSettings} class
     * @return this
     */
    public TestSettingsBuilder withDefaultKeyspaceSettings(Class<? extends KeyspaceSettings> defaultKeyspaceSettingsClass) {
        this.defaultKeyspaceSettingsClass = defaultKeyspaceSettingsClass;
        return this;
    }

    /**
     * Use this {@link DataSettings} object for {@link TestSettings}.
     * <p>
     * If specified, tbe data settings annotation will be ignored.
     *
     * @param dataSettings {@link DataSettings}
     * @return this
     */
    public TestSettingsBuilder withDataSettings(DataSettings dataSettings) {
        this.dataSettings = dataSettings;
        return this;
    }

    /**
     * The default {@link DataSettings} object to create if the data settings is not present.
     *
     * @param defaultDataSettingsClass {@link DataSettings} class
     * @return this
     */
    public TestSettingsBuilder withDefaultDataSettings(Class<? extends DataSettings> defaultDataSettingsClass) {
        this.defaultDataSettingsClass = defaultDataSettingsClass;
        return this;
    }

    /**
     * Use this {@link RollbackSettings} object for {@link TestSettings}.
     * <p>
     * If specified, tbe rollback settings annotation will be ignored.
     *
     * @param rollbackSettings {@link RollbackSettings}
     * @return this
     */
    public TestSettingsBuilder withRollbackSettings(RollbackSettings rollbackSettings) {
        this.rollbackSettings = rollbackSettings;
        return this;
    }

    /**
     * The default {@link RollbackSettings} object to create if the rollback settings is not present.
     *
     * @param defaultRollbackSettingsClass {@link RollbackSettings} class
     * @return this
     */
    public TestSettingsBuilder withDefaultRollbackSettings(Class<? extends RollbackSettings> defaultRollbackSettingsClass) {
        this.defaultRollbackSettingsClass = defaultRollbackSettingsClass;
        return this;
    }

    /**
     * Create a new {@link TestSettings} object from the current builder state.
     *
     * @return {@link TestSettings}
     */
    public TestSettings build() {
        PropertyResolver selectedPropertyResolver = this.propertyResolver;

        if (selectedPropertyResolver == null) {
            if (this.testClass != null && this.testClass.isAnnotationPresent(CassandraProperties.class)) {
                selectedPropertyResolver = PropertiesPropertyResolver.fromUrl(
                        this.testClass.getAnnotation(CassandraProperties.class).value());
            }

            if (selectedPropertyResolver == null) {
                selectedPropertyResolver = this.defaultPropertyResolver;
            }
        }

        ConnectSettings selectedConnectSettings = getSettingsOrDefault(
                ConnectSettings.class,
                this.defaultConnectSettingsClass,
                this.connectSettings,
                selectedPropertyResolver);

        KeyspaceSettings selectedKeyspaceSettings = getSettingsOrDefault(
                KeyspaceSettings.class,
                this.defaultKeyspaceSettingsClass,
                this.keyspaceSettings,
                selectedPropertyResolver);

        DataSettings selectedDataSettings = getSettingsOrDefault(
                DataSettings.class,
                this.defaultDataSettingsClass,
                this.dataSettings,
                selectedPropertyResolver);

        RollbackSettings selectedRollbackSettings = getSettingsOrDefault(
                RollbackSettings.class,
                this.defaultRollbackSettingsClass,
                this.rollbackSettings,
                selectedPropertyResolver);

        return new TestSettings(selectedConnectSettings, selectedKeyspaceSettings, selectedDataSettings, selectedRollbackSettings);
    }

    private <T> T getSettingsOrDefault(Class<T> settingsType,
                                       Class<? extends T> defaultSettingsClass,
                                       T defaultSettings,
                                       PropertyResolver selectedPropertyResolver) {
        if (defaultSettings != null) {
            return defaultSettings;
        }

        if (this.testClass == null) {
            throw new CassandraTestException("TestSettingsBuilder requires a test class to load %s", settingsType.getSimpleName());
        }

        return getSettings(
                this.testClass,
                SETTINGS_FACTORY_PROPERTY_MAP.get(settingsType),
                settingsType,
                defaultSettingsClass,
                selectedPropertyResolver);
    }

    private <T> T getSettings(AnnotatedElement annotatedElement,
                              String factoryPropertyName,
                              Class<T> settingsClass,
                              Class<? extends T> defaultSettingsClass,
                              PropertyResolver propertyResolver) {

        // 1. Find the annotation with a property named factoryPropertyName.

        Method factoryProperty = null;
        Annotation settingsAnnotation = null;

        for (Annotation annotation : annotatedElement.getAnnotations()) {
            Method property;
            try {
                property = annotation.annotationType().getDeclaredMethod(factoryPropertyName);
            } catch (NoSuchMethodException e) {
                property = null;
            }

            if (property != null) {
                if (factoryProperty != null) {
                    throw new CassandraTestException("Annotations @%s and @%s are in conflict. Use only one annotation with property %s",
                            settingsAnnotation.annotationType().getSimpleName(), annotation.annotationType().getSimpleName(), factoryPropertyName);
                }
                factoryProperty = property;
                settingsAnnotation = annotation;
            }
        }

        // No annotation for this setting has been specified. Create a default instance of this setting.
        if (factoryProperty == null) {
            return defaultSettingsClass.cast(newInstance(defaultSettingsClass));
        }

        // 2. Create an instance of the factory for this setting.

        Class<?> settingsFactoryClass = (Class<?>)invoke(factoryProperty, settingsAnnotation);
        Object settingsFactory = newInstance(settingsFactoryClass);

        // 3. Find the factory's create method.

        Method create;

        try {
            create = settingsFactoryClass.getDeclaredMethod("create", Annotation.class, PropertyResolver.class);
        } catch (NoSuchMethodException e) {
            throw new CassandraTestException("Cannot find method create(Annotation, PropertyResolver) in factory %s",
                    settingsFactoryClass.getSimpleName());
        }

        // 4. Invoke factory's create method to create this setting.

        Object settings = invoke(create, settingsFactory, settingsAnnotation, propertyResolver);

        return settingsClass.cast(settings);
    }

    private Object newInstance(Class<?> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new CassandraTestException("Failed to create instance of class %s", type.getSimpleName(), e);
        }
    }

    private Object invoke(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new CassandraTestException(String.format("Failed to invoke method %s on class %s",
                    method.getName(), instance.getClass().getSimpleName()), e);
        }
    }
}

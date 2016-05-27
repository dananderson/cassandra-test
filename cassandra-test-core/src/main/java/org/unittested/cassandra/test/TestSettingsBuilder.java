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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.unittested.cassandra.test.connect.ConnectSettings;
import org.unittested.cassandra.test.connect.basic.BasicConnectSettings;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.basic.BasicDataSettings;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.property.PropertyResolver;
import org.unittested.cassandra.test.property.system.JavaPropertyResolver;
import org.unittested.cassandra.test.rollback.RollbackSettings;
import org.unittested.cassandra.test.rollback.basic.BasicRollbackSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.basic.BasicKeyspaceSettings;

/**
 * Builds {@link TestSettings} from Cassandra Test annotations.
 */
public class TestSettingsBuilder {

    public static TestSettings fromAnnotatedElement(AnnotatedElement annotatedElement, PropertyResolver propertyResolver) {
        return new TestSettings(
                getSettings(annotatedElement, "__connectSettingsFactory", ConnectSettings.class, BasicConnectSettings.class, propertyResolver),
                getSettings(annotatedElement, "__keyspaceSettingsFactory", KeyspaceSettings.class, BasicKeyspaceSettings.class, propertyResolver),
                getSettings(annotatedElement, "__dataSettingsFactory", DataSettings.class, BasicDataSettings.class, propertyResolver),
                getSettings(annotatedElement, "__rollbackSettingsFactory", RollbackSettings.class, BasicRollbackSettings.class, propertyResolver));
    }

    public static TestSettings fromAnnotatedElement(AnnotatedElement annotatedElement) {
        return fromAnnotatedElement(annotatedElement, new JavaPropertyResolver());
    }

    private static <T> T getSettings(AnnotatedElement annotatedElement,
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

    private static Object newInstance(Class<?> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new CassandraTestException("Failed to create instance of class %s", type.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new CassandraTestException("Failed to create instance of class %s", type.getSimpleName());
        }
    }

    private static Object invoke(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            throw new CassandraTestException(String.format("Failed to invoke method %s on class %s",
                    method.getName(), instance.getClass().getSimpleName()));
        } catch (InvocationTargetException e) {
            throw new CassandraTestException(String.format("Failed to invoke method %s on class %s",
                    method.getName(), instance.getClass().getSimpleName()));
        }
    }
}

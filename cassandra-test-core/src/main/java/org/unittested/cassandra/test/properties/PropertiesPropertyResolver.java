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

package org.unittested.cassandra.test.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.resource.Resource;

/**
 * {@link org.unittested.cassandra.test.properties.PropertyResolver} that reads properties from a Java {@link Properties} source.
 */
public class PropertiesPropertyResolver extends AbstractPropertyResolver {

    /**
     * Default properties file URL.
     */
    public static final String DEFAULT_PROPERTIES_URL = "classpath:cassandra-test.properties";

    /**
     * Default {@link PropertyResolver} composed of {@link #DEFAULT_PROPERTIES_URL} from the class path and
     * System {@link Properties}. If the default properties file does not exist, this PropertyResolver will only
     * contain System Properties.
     */
    public static final PropertyResolver DEFAULT = createDefault(DEFAULT_PROPERTIES_URL);

    private Properties properties;

    public PropertiesPropertyResolver(Properties properties) {
        this.properties = properties;
    }

    /**
     * Create a {@link PropertyResolver} from a {@link Resource} URL.
     *
     * @param url URL to properties file.
     * @param withSystemDefaults If true, System {@link Properties} will be included in the PropertyResolver.
     * @return {@link PropertyResolver}
     */
    public static PropertyResolver fromUrl(String url, boolean withSystemDefaults) {
        Resource resource = new Resource(url);
        InputStream stream = null;
        Properties properties = withSystemDefaults ? new Properties(System.getProperties()) : new Properties();

        try {
            stream = resource.getStream();
            properties.load(stream);
        } catch (Exception e) {
            throw new CassandraTestException("Failed to load properties file '%s'", url, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }

        return new PropertiesPropertyResolver(properties);
    }

    private static PropertyResolver createDefault(String defaultPropertiesUrl) {
        try {
            return fromUrl(defaultPropertiesUrl, true);
        } catch (Exception e) {
            return new PropertiesPropertyResolver(System.getProperties());
        }
    }

    @Override
    public String getProperty(String propertyName, String defaultValue) {
        return this.properties.getProperty(propertyName, defaultValue);
    }
}

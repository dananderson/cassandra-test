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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.unittested.cassandra.test.exception.CassandraTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PropertiesPropertyResolverTest {

    @DataProvider
    public static Object[][] validInputs() {
        return new Object[][]{
                // properties exist
                { "${a}", "a_value" },
                { "${b.c}", "bc_value" },
                { "${d_e}", "de_value" },
                { "  ${a}", "  a_value" },
                { "${a}  ", "a_value  " },
                { "  ${a}   ", "  a_value   " },
                { "${a},${b.c},${d_e}", "a_value,bc_value,de_value" },
                { " ${a} ,  ${b.c}  ,   ${d_e}    ", " a_value ,  bc_value  ,   de_value    " },
                // properties don't exist
                { "${xxx}", "${xxx}" },
                { "  ${xxx}", "  ${xxx}" },
                { "${xxx}   ", "${xxx}   " },
                { "   ${xxx}  ", "   ${xxx}  " },
                // non-properties
                { "hello", "hello" },
        };
    }

    @Test(dataProvider = "validInputs")
    public void resolveReferencesFromProperties(String input, String expectedOutput) throws Exception {
        // given
        PropertyResolver propertyResolver = new PropertiesPropertyResolver(getProperties());

        // when
        String output = propertyResolver.resolveReferences(input);

        // then
        assertThat(output, is(expectedOutput));
    }

    @Test(dataProvider = "validInputs")
    public void resolveReferencesFromUrl(String input, String expectedOutput) throws Exception {
        // given
        PropertyResolver propertyResolver = PropertiesPropertyResolver.fromUrl(
                "classpath:properties-property-resolver-test.properties", false);

        // when
        String output = propertyResolver.resolveReferences(input);

        // then
        assertThat(output, is(expectedOutput));
    }

    @DataProvider
    public static Object[][] invalidUrls() {
        return new Object[][] {
                { "error" },
                { "classpath:does/not/exist.properties" },
                { "text:property=\\uX" }
        };
    }

    @Test(dataProvider = "invalidUrls", expectedExceptions = CassandraTestException.class)
    public void fromUrlWithInvalidInput(String url) throws Exception {
        PropertiesPropertyResolver.fromUrl(url, true);
    }

    @Test
    public void systemProperties() throws Exception {
        assertThat(PropertiesPropertyResolver.DEFAULT.resolveReferences("${java.class.path}"), is(not("${java.class.path}")));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void resolveWithNullInput() throws Exception {
        // given
        PropertyResolver propertyResolver = new PropertiesPropertyResolver(getProperties());

        // when
        propertyResolver.resolveReferences((String)null);

        // then
        // throws NullPointerException
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void resolveArrayWithNullInput() throws Exception {
        // given
        PropertyResolver propertyResolver = new PropertiesPropertyResolver(getProperties());

        // when
        propertyResolver.resolveReferences((String[])null);

        // then
        // throws NullPointerException
    }

    @Test
    public void createDefault() throws Exception {
        PropertyResolver propertyResolver = createDefault("classpath:cassandra-test.properties");

        assertThat(propertyResolver, notNullValue());
        assertThat(propertyResolver.resolveReferences("${cassandra.test.host}"), is("127.0.0.1"));
        assertThat(propertyResolver.resolveReferences("${cassandra.test.port}"), is("9042"));
    }

    @Test
    public void createDefaultWithoutDefaultPropertiesFile() throws Exception {
        PropertyResolver propertyResolver = createDefault("classpath:xxx");

        assertThat(propertyResolver, notNullValue());
        assertThat(propertyResolver.resolveReferences("${cassandra.test.host}"), is("${cassandra.test.host}"));
    }

    private PropertyResolver createDefault(String propertiesUrl) throws Exception {
        Method createDefault = PropertiesPropertyResolver.class.getDeclaredMethod("createDefault", String.class);

        createDefault.setAccessible(true);

        return (PropertyResolver)createDefault.invoke(null, propertiesUrl);
    }

    private Properties getProperties() throws Exception {
        Properties p = new Properties();
        InputStream stream = null;

        try {
            stream = getClass().getClassLoader().getResourceAsStream("properties-property-resolver-test.properties");
            p.load(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return p;
    }
}

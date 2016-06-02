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
                { "  ${a}", "a_value" },
                { "${a}  ", "a_value" },
                { "  ${a}   ", "a_value" },
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
    public void resolveFromProperties(String input, String expectedOutput) throws Exception {
        // given
        PropertyResolver propertyResolver = new PropertiesPropertyResolver(getProperties());

        // when
        String output = propertyResolver.resolve(input);

        // then
        assertThat(output, is(expectedOutput));
    }

    @Test(dataProvider = "validInputs")
    public void resolveFromLocator(String input, String expectedOutput) throws Exception {
        // given
        PropertyResolver propertyResolver = PropertiesPropertyResolver.fromLocator(
                "classpath:properties-property-resolver-test.properties");

        // when
        String output = propertyResolver.resolve(input);

        // then
        assertThat(output, is(expectedOutput));
    }

    @DataProvider
    public static Object[][] badLocators() {
        return new Object[][] {
                { "error" },
                { "classpath:does/not/exist.properties" },
                { "text:property=\\uX" }
        };
    }

    @Test(dataProvider = "badLocators", expectedExceptions = CassandraTestException.class)
    public void fromUriWithBadLocator(String locator) throws Exception {
        PropertiesPropertyResolver.fromLocator(locator);
    }

    @Test
    public void systemProperties() throws Exception {
        assertThat(PropertiesPropertyResolver.SYSTEM.resolve("${java.class.path}"), is(not("${java.class.path}")));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void resolveWithNullInput() throws Exception {
        // given
        PropertyResolver propertyResolver = new PropertiesPropertyResolver(getProperties());

        // when
        propertyResolver.resolve((String)null);

        // then
        // throws NullPointerException
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void resolveArrayWithNullInput() throws Exception {
        // given
        PropertyResolver propertyResolver = new PropertiesPropertyResolver(getProperties());

        // when
        propertyResolver.resolve((String [])null);

        // then
        // throws NullPointerException
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

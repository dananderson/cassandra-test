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

package org.unittested.cassandra.test.property.system;

import java.util.Properties;

import org.unittested.cassandra.test.property.PropertyResolver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class JavaPropertyResolverTest {

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
    public void resolve(String input, String expectedOutput) throws Exception {
        // given
        PropertyResolver propertyResolver = new JavaPropertyResolver(getProperties());

        // when
        String output = propertyResolver.resolve(input);

        // then
        assertThat(output, is(expectedOutput));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void resolveWithNullInput() throws Exception {
        // given
        PropertyResolver propertyResolver = new JavaPropertyResolver(getProperties());

        // when
        propertyResolver.resolve((String)null);

        // then
        // throws NullPointerException
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void resolveArrayWithNullInput() throws Exception {
        // given
        PropertyResolver propertyResolver = new JavaPropertyResolver(getProperties());

        // when
        propertyResolver.resolve((String [])null);

        // then
        // throws NullPointerException
    }

    private Properties getProperties() {
        Properties p = new Properties();

        p.setProperty("a", "a_value");
        p.setProperty("b.c", "bc_value");
        p.setProperty("d_e", "de_value");

        return p;
    }
}

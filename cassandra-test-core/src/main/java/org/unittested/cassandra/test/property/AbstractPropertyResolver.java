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

package org.unittested.cassandra.test.property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common code for {@link PropertyResolver} implementations.
 */
public abstract class AbstractPropertyResolver implements PropertyResolver {

    protected static final Pattern PROPERTY_NAME = Pattern.compile("^\\s*\\$\\{(.+)\\}\\s*$");

    @Override
    public String resolve(final String annotationValue) {
        if (annotationValue == null) {
            throw new NullPointerException("annotationValue must be set.");
        }

        Matcher m = PROPERTY_NAME.matcher(annotationValue);

        if (m.matches()) {
            return getProperty(m.group(1), annotationValue);
        }

        return annotationValue;
    }

    @Override
    public String[] resolve(final String[] annotationValue) {
        if (annotationValue == null) {
            throw new NullPointerException("annotationValue array must be set.");
        }

        for (int i = 0; i < annotationValue.length; i++) {
            annotationValue[i] = resolve(annotationValue[i]);
        }

        return annotationValue;
    }

    abstract protected String getProperty(String propertyName, String defaultValue);
}

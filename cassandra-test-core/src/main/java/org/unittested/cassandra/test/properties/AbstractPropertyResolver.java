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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common code for {@link PropertyResolver} implementations.
 */
public abstract class AbstractPropertyResolver implements PropertyResolver {

    protected static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{(.+?)\\}");

    @Override
    public String resolveReferences(String text) {
        if (text == null) {
            throw new NullPointerException("'text' cannot be null.");
        }

        Matcher m = PROPERTY_REFERENCE.matcher(text);
        StringBuilder buffer = new StringBuilder();
        int bufferIndex = 0;

        while (m.find()) {
            if (bufferIndex < m.start()) {
                buffer.append(text.substring(bufferIndex, m.start()));
                bufferIndex = m.start();
            }

            String reference = text.substring(m.start(), m.end());

            buffer.append(getProperty(m.group(1), reference));
            bufferIndex += reference.length();
        }

        if (bufferIndex > 0) {
            return buffer.append(text.substring(bufferIndex, text.length())).toString();
        }

        return text;
    }

    @Override
    public String[] resolveReferences(String[] text) {
        if (text == null) {
            throw new NullPointerException("annotationValue array must be set.");
        }

        for (int i = 0; i < text.length; i++) {
            text[i] = resolveReferences(text[i]);
        }

        return text;
    }

}

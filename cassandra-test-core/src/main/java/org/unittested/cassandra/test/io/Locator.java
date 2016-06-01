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

package org.unittested.cassandra.test.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.util.Utils;

/**
 * Represents a pointer to a text resources.
 *
 * Locator URLs are in the format of source:path or source.content:path. Source is where to go to get the resource.
 * Content is the format of the resource. Path is the location of the resource in the source. Depending on the source,
 * the path can also be the resource itself.
 * <p>
 * Source Types
 * <ul>
 *     <li>"classpath" - Path is absolute path to a classpath file.</li>
 *     <li>"file" - Path is absolute or relative path to a file on the filesystem.</li>
 *     <li>"text" - Path is the String resource.</li>
 * </ul>
 * Content Types
 * <ul>
 *     <li>"cql" - CQL statements</li>
 * </ul>
 * Examples
 * <ul>
 *     <li>classpath:my/file.cql</li>
 *     <li>file:path/to/file.cql</li>
 *     <li>text: SELECT * FROM ...</li>
 *     <li>classpath.cql:my/file.cql</li>
 * </ul>
 * @see org.unittested.cassandra.test.io.Locator.Source
 * @see org.unittested.cassandra.test.io.Locator.Content
 */
public class Locator {

    private static Pattern URL = Pattern.compile("^(\\w+)(?:.(\\w+))?:(.+)$", Pattern.DOTALL | Pattern.MULTILINE);
    private static Charset UTF_8 = Charset.forName("UTF-8");

    private Source source;
    private Content content;
    private String path;

    /**
     * Create a {@link Locator} from a locator URL.
     *
     * @param locatorUrl Locator URL.
     * @return {@link Locator}
     */
    public static Locator fromUrl(String locatorUrl) {
        Source source;
        Content content;
        String path;
        Matcher m = URL.matcher(StringUtils.trimToEmpty(locatorUrl.trim()));

        if (!m.matches()) {
            throw new CassandraTestException("'%s' is not a valid locator. Use locator format: 'source.content:path'", locatorUrl);
        }

        // source
        try {
            source = Source.valueOf(m.group(1).toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new CassandraTestException("'%s' is not a valid locator source type.", m.group(1));
        }

        // content (optional)
        if (m.group(2) != null) {
            try {
                content = Content.valueOf(m.group(2).toUpperCase());
            } catch(IllegalArgumentException e) {
                throw new CassandraTestException("'%s' is not a valid locator content type.", m.group(2));
            }
        } else {
            content = Content.CQL;
        }

        // path
        path = m.group(3).trim();

        return new Locator(source, content, path);
    }

    /**
     * Create a {@link Locator} from a CQL source string.
     * <p>
     * The string can contain ; delimited CQL statements OR a locator URL.
     *
     * @param cqlSource CQL source string.
     * @return {@link Locator}
     */
    public static Locator fromCqlSource(String cqlSource) {
        if (Utils.isCqlLike(cqlSource)) {
            return new Locator(Source.TEXT, Content.CQL, cqlSource);
        }

        return fromUrl(cqlSource);
    }

    private Locator(Source source, Content content, String path) {
        this.source = source;
        this.content = content;
        this.path = path;
    }

    /**
     * Get the resource source.
     *
     * @return {@link org.unittested.cassandra.test.io.Locator.Source}
     */
    public Source getSource() {
        return this.source;
    }

    /**
     * Get the resource format.
     *
     * @return {@link org.unittested.cassandra.test.io.Locator.Content}
     */
    public Content getContent() {
        return this.content;
    }

    /**
     * Get the path to the resource.
     *
     * @return Resource path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Get an {@link InputStream} to read this text resource.
     *
     * @return {@link InputStream}
     * @throws IOException if the resource cannot be read or found.
     */
    public InputStream getStream() throws IOException {
        return this.source.getStream(this.path);
    }

    /**
     * Get a {@link Reader} to read this text resource.
     *
     * @return {@link Reader}
     * @throws IOException if the resource cannot be read or found.
     */
    public Reader getReader() throws IOException {
        return this.source.getReader(this.path);
    }

    /**
     * Text resource source.
     */
    public enum Source {

        /**
         * From the classpath. Locator prefix is 'classpath:'.
         */
        CLASSPATH {
            @Override
            public InputStream getStream(final String path) throws IOException {
                URL resource = getClass().getClassLoader().getResource(path);

                if (resource == null) {
                    throw new FileNotFoundException("Could not find file '" + path + "' in classpath.");
                }

                if (resource.getProtocol().equals("file")) {
                    File file;

                    try {
                        file = new File(resource.toURI());
                    } catch (URISyntaxException e) {
                        throw new IOException("Failed to load file '" + path + "' in classpath.", e);
                    }

                    if (file.isDirectory()) {
                        throw new UnsupportedOperationException("Directory locators are not supported. Classpath file = '"
                                + path + "'");
                    }
                }

                return resource.openStream();
            }
        },

        /**
         * From the filesystem. Locator prefix is 'file:'.
         */
        FILE {
            @Override
            public InputStream getStream(final String path) throws IOException {
                File file = new File(path);

                if (file.isDirectory()) {
                    throw new UnsupportedOperationException("Directory locators are not supported. File = '"
                        + path + "'");
                }

                return new FileInputStream(path);
            }
        },

        /**
         * From a String. Locator prefix is 'text:'.
         */
        TEXT {
            @Override
            public InputStream getStream(final String path) throws IOException {
                return new ByteArrayInputStream(path.getBytes(UTF_8));
            }

            @Override
            public Reader getReader(final String path) throws IOException {
                return new StringReader(path);
            }
        };

        abstract InputStream getStream(String path) throws IOException;

        Reader getReader(final String path) throws IOException {
            return new InputStreamReader(getStream(path));
        }
    }

    /**
     * Format of the text resource.
     */
    public enum Content {

        /**
         * CQL statements delimited by ";" and comments.
         */
        CQL
    }
}

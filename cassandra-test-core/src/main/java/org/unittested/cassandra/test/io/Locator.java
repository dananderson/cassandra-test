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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.util.Utils;

/**
 * Represents a URI to a text resources.
 *
 * Locator URIs are standard URIs used to describe files on the filesystem or in the classpath. Locators are meant to
 * point to text files containing CQL statements. In most cases, locator URIs will be used in Cassandra Test annotations.
 * <p>
 * Locators call the the scheme/protocol a "source", which can be classpath or file. Locators to classpath
 * and filesystem files must be URL encoded; however, spaces in the locator are allowed for ease of use.
 * <p>
 * Content type of the resource is determined by the contentType query parameter. If the query parameter is not present,
 * the file extension is used. Otherwise, content type of CQL is assumed. Currently, the only supported content type is
 * CQL.
 * <p>
 * Examples Locators
 * <ul>
 *     <li>file:path/to/file.cql</li>
 *     <li>file://path/to/file.cql</li>
 *     <li>classpath://path/to/file.cql</li>
 *     <li>classpath:path/to/file.cql</li>
 * </ul>
 *
 * @see org.unittested.cassandra.test.io.Locator.Source
 * @see org.unittested.cassandra.test.io.Locator.ContentType
 */
public class Locator {

    private static Charset UTF_8 = Charset.forName("UTF-8");

    private Source source;
    private ContentType contentType;
    private String path;

    /**
     * Create a {@link Locator} from a locator URI.
     *
     * @param locatorUri Locator URI.
     * @return {@link Locator}
     */
    public static Locator fromUri(String locatorUri) {
        // Handle text "URI"s as a special case. Do not decode.
        if (locatorUri.startsWith("text:")) {
            return new Locator(Source.TEXT, ContentType.CQL, StringUtils.removeStart(locatorUri, "text:"));
        }

        URI uri;

        // Tolerate spaces in the URI because it is just easier that way.
        locatorUri = StringUtils.replace(locatorUri, " ", "%20");

        // Basic URI parsing and validation.
        try {
            uri = URI.create(locatorUri);
        } catch (Exception e) {
            throw new CassandraTestException("Invalid URI format for locator. '%s'", locatorUri, e);
        }

        if (!uri.isAbsolute()) {
            throw new CassandraTestException("Locator is missing protocol/scheme prefix. '%s'", locatorUri);
        }

        // Get and validate the protocol/scheme is supported.
        Source source;

        try {
            source = Source.valueOf(uri.getScheme().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CassandraTestException("Invalid protocol/scheme prefix. '%s'", uri.getScheme(), e);
        }

        // Remove the query string from the scheme specific part. Since filenames appear in the scheme specific part,
        // URI may not parse out the whole filename. Go into the raw data and strip out the query string to get the path.
        String path = StringUtils.substringBefore(uri.getRawSchemeSpecificPart(), "?");

        // If the locator is file://blah, the scheme specific part will be prefixed with //. Remove it.
        path = StringUtils.stripStart(path, "//");

        // Finally, URL decode the path.
        try {
            path = URLDecoder.decode(path, UTF_8.name());
        } catch (Exception e) {
            throw new CassandraTestException("Failed to decode path in URI. '%s'", locatorUri, e);
        }

        if (StringUtils.isBlank(path)) {
            throw new CassandraTestException("Path is blank. '%s", locatorUri);
        }

        return new Locator(source, ContentType.CQL, path);
    }

    /**
     * Create a {@link Locator} from a CQL source string.
     * <p>
     * The string can contain ; delimited CQL statements OR a locator URI.
     *
     * @param cqlSource CQL source string.
     * @return {@link Locator}
     */
    public static Locator fromCqlSource(String cqlSource) {
        if (StringUtils.isBlank(cqlSource)) {
            return new Locator(Source.TEXT, ContentType.CQL, "");
        }

        if (Utils.isCqlLike(cqlSource)) {
            return new Locator(Source.TEXT, ContentType.CQL, cqlSource);
        }

        return fromUri(cqlSource);
    }

    private Locator(Source source, ContentType contentType, String path) {
        this.source = source;
        this.contentType = contentType;
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
     * @return {@link ContentType}
     */
    public ContentType getContentType() {
        return this.contentType;
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
     * The source to go to get an {@link InputStream} for the text resource.
     */
    public enum Source {

        /**
         * Load from files from the classpath / jar.
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
         * Load files from the filesystem.
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
         * Load text from a Java String (for testing convenience only).
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
     * Content type or format of the text resource.
     */
    public enum ContentType {

        /**
         * CQL text.
         * <p>
         * The representation of CQL in this content type is similar to .cql files that cqlsh accepts. The content
         * contains legal CQL statements delimited by &quot;;&quot;, including batch statments. The consistency command
         * from cqlsh is also supported. Line and block comments supported by cqlsh are also legal.
         */
        CQL
    }
}

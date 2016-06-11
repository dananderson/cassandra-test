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

package org.unittested.cassandra.test.resource;

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
 * Text resource loader.
 *
 * Provides access to an {@link InputStream} or {@link Reader} for a text resource. Resource abstracts the source of the
 * resource, such as file or class path, using the protocol of the resource's URL.
 * <p>
 * The content type of the resource is assumed to be a text resource. In most cases, Cassandra Test interprets the
 * text resource as CQL statements. {@link org.unittested.cassandra.test.resource.Resource.ContentType#CQL}
 * <p>
 * Supported Protocols
 * <ul>
 *     <li>file:[path] or file://[path] - Filesystem file.</li>
 *     <li>classpath:[path] or classpath://[path] - Class path file.</li>
 * </ul>
 * The resource URL format uses URL encoded spaces, but literal spaces are supported as well. For example, file://path/to/my file.txt
 * will point to path/to/my file.txt on the filesystem.
 */
public class Resource {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private Source source;
    private String path;

    /**
     * Create a {@link Resource} from a CQL statement or resource URL.
     *
     * @param cqlOrUrl CQL statement or resource URL.
     * @return {@link Resource}
     */
    public static Resource fromCqlOrUrl(String cqlOrUrl) {
        if (StringUtils.isBlank(cqlOrUrl)) {
            return new Resource(Source.TEXT, "");
        }

        if (Utils.isCqlLike(cqlOrUrl)) {
            return new Resource(Source.TEXT, cqlOrUrl);
        }

        return new Resource(cqlOrUrl);
    }

    public Resource(String url) {
        parseUrl(url);
    }

    public Resource(Source source, String path) {
        this.source = source;
        this.path = path;
    }

    /**
     * Get the resource source.
     *
     * @return {@link Resource.Source}
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
        return ContentType.CQL;
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

    private void parseUrl(String url) {
        // Handle text "URI"s as a special case. Do not decode.
        if (url.startsWith("text:")) {
            this.source = Source.TEXT;
            this.path = StringUtils.removeStart(url, "text:");
            return;
        }

        URI uri;

        // Tolerate spaces in the URI because it is just easier that way.
        url = StringUtils.replace(url, " ", "%20");

        // Basic URI parsing and validation.
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            throw new CassandraTestException("Invalid URL format. URL = '%s'", url, e);
        }

        if (!uri.isAbsolute()) {
            throw new CassandraTestException("URL is missing protocol/scheme prefix. URL = '%s'", url);
        }

        // Get and validate the protocol/scheme is supported.
        Source urlSource;

        try {
            urlSource = Source.valueOf(uri.getScheme().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CassandraTestException("Invalid protocol URL prefix of '%s'", uri.getScheme(), e);
        }

        // Remove the query string from the scheme specific part. Since filenames appear in the scheme specific part,
        // URI may not parse out the whole filename. Go into the raw data and strip out the query string to get the path.
        String urlPath = StringUtils.substringBefore(uri.getRawSchemeSpecificPart(), "?");

        // If the URL is file://blah, the scheme specific part will be prefixed with //. Remove it.
        urlPath = StringUtils.stripStart(urlPath, "//");

        // Finally, URL decode the path.
        try {
            urlPath = URLDecoder.decode(urlPath, UTF_8.name());
        } catch (Exception e) {
            throw new CassandraTestException("Failed to decode path in URL. URL = '%s'", url, e);
        }

        if (StringUtils.isBlank(urlPath)) {
            throw new CassandraTestException("Path is blank. URL = '%s", url);
        }

        this.source = urlSource;
        this.path = urlPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource)o;

        if (source != resource.source) return false;
        return !(path != null ? !path.equals(resource.path) : resource.path != null);

    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    /**
     * The protocol or how to get an {@link InputStream} for the text resource.
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
                        throw new UnsupportedOperationException("Directory URLs are not supported. Classpath file = '"
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
                    throw new UnsupportedOperationException("Directory URLs are not supported. File = '"
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

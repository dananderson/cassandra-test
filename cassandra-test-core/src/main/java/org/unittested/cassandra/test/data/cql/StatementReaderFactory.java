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

package org.unittested.cassandra.test.data.cql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.util.Utils;

class StatementReaderFactory {

    private static final Pattern PREFIX = Pattern.compile("^\\s*(:?(:?(\\w+)\\.(\\w+):)|(?:(\\w+):))\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);

    StatementReader createStatementReader(String cqlSource) throws IOException {
        Matcher matcher = PREFIX.matcher(cqlSource);
        Protocol protocol;
        ContentType contentType;
        String locator;

        if (matcher.matches()) {
            if (matcher.group(3) != null && matcher.group(4) != null) {
                protocol = toProtocol(matcher.group(3));
                contentType = toContentType(matcher.group(4));
            } else if (matcher.group(5) != null) {
                protocol = toProtocol(matcher.group(5));
                contentType = ContentType.CQL;
            } else {
                throw new CassandraTestException("Cannot read CQL source protocol in %s", cqlSource);
            }
            locator = matcher.group(6);
        } else {
            protocol = Protocol.STRING;
            contentType = ContentType.CQL;
            locator = cqlSource;
        }

        if (protocol == Protocol.STRING && !Utils.isCqlLike(locator)) {
            throw new CassandraTestException("Invalid CQL source: %s", cqlSource);
        } else if (StringUtils.isBlank(locator)) {
            throw new CassandraTestException("Invalid CQL locator.");
        }

        Reader reader;

        switch (protocol) {
            case STRING:
                reader = new StringReader(locator);
                break;
            case CLASSPATH:
                URL url = StatementReaderFactory.class.getClassLoader().getResource(locator);

                if (url == null) {
                    throw new FileNotFoundException("Could not find classpath file " + locator);
                }

                if (!"file".equals(url.getProtocol())) {
                    throw new CassandraTestException("Unsupported protocol %s", url.getProtocol());
                }

                URI uri;

                try {
                    uri = url.toURI();
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }

                reader = createFileReader(new File(uri));
            break;
            case FILE:
                reader = createFileReader(new File(locator));
                break;
            default:
                throw new CassandraTestException("Unsupported protocol: %s", protocol);
        }

        switch (contentType) {
            case CQL:
                return new CqlStatementReader(reader);
            default:
                throw new CassandraTestException("Unsupported contentType: %s", contentType);
        }
    }

    private Protocol toProtocol(String str) {
        try {
            return Protocol.valueOf(str.toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new CassandraTestException("Failed to parse protocol: %s", str);
        }
    }

    private ContentType toContentType(String str) {
        try {
            return ContentType.valueOf(str.toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new CassandraTestException("Failed to parse content type: %s", str);
        }
    }

    private Reader createFileReader(File file) throws IOException {
        if (file.isDirectory()) {
            throw new UnsupportedOperationException();
        }

        return new BufferedReader(new FileReader(file));
    }

    private enum Protocol {
        CLASSPATH,
        FILE,
        STRING
    }

    private enum ContentType {
        CQL
    }
}

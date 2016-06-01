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

import static org.unittested.cassandra.test.io.Locator.Source.*;
import static org.unittested.cassandra.test.io.Locator.Content.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.FileNotFoundException;

import org.unittested.cassandra.test.exception.CassandraTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LocatorTest {

    @DataProvider(name = "cqlSourceData")
    public static Object[][] cqlSourceData() {
        String cqlInsert = "INSERT INTO test_table(id, name) VALUES (1000, 'insert_from_file');";
        String classpathFile = "cql/sample-data.cql";
        String file = "target/test-classes/cql/sample-data.cql";

        return new Object[][] {
                { cqlInsert, TEXT, CQL, cqlInsert },
                { "text:" + cqlInsert, TEXT, CQL, cqlInsert },
                { "text.cql:" + cqlInsert, TEXT, CQL, cqlInsert },
                { "classpath:" + classpathFile, CLASSPATH, CQL, classpathFile },
                { "classpath: " + classpathFile, CLASSPATH, CQL, classpathFile },
                { "classpath.cql:" + classpathFile, CLASSPATH, CQL, classpathFile },
                { "classpath.cql: " + classpathFile, CLASSPATH, CQL, classpathFile },
                { "file:" + file, FILE, CQL, file  },
                { "file: " + file, FILE, CQL, file },
                { "file.cql:" + file, FILE, CQL, file },
                { "file.cql: " + file, FILE, CQL, file },
                { ";", TEXT, CQL, ";" },
                { "text:;", TEXT, CQL, ";" },
                { "text.cql:;", TEXT, CQL, ";" },
        };
    }

    @Test(dataProvider = "cqlSourceData")
    public void fromCqlSource(String cqlSource,
                              Locator.Source expectedSource,
                              Locator.Content expectedConent,
                              String expectedPath) throws Exception {
        Locator locator;

        locator = Locator.fromCqlSource(cqlSource);

        assertThat(locator.getSource(), is(expectedSource));
        assertThat(locator.getContent(), is(expectedConent));
        assertThat(locator.getReader(), notNullValue());
        assertThat(locator.getStream(), notNullValue());
        assertThat(locator.getPath(), is(expectedPath));
    }

    @DataProvider(name = "invalidCqlSourceFormatData")
    public static Object[][] invalidCqlSourceFormatData() {
        return new Object[][] {
                { "invalid:..." },
                { "invalid.invalid:..." },
                { "classpath.invalid:..." },
                { "cql:not cql" },
                { "" },
                { "cql:" },
                { "text:" },
                { "text.cql:" },
                { "file:" },
                { "classpath:" },
                { " " },
                { "cql: " },
                { "text: " },
                { "text.cql: " },
                { "file: " },
                { "classpath: " },
        };
    }

    @Test(dataProvider = "invalidCqlSourceFormatData", expectedExceptions = CassandraTestException.class)
    public void fromUrlWithInvalidCqlSourceFormat(String cqlSource) throws Exception {
        // given, then
        Locator.fromUrl(cqlSource);

        // then
        // CassandraTestException
    }

    @Test(dataProvider = "invalidCqlSourceFormatData", expectedExceptions = CassandraTestException.class)
    public void fromCqlSourceWithInvalidCqlSourceFormat(String cqlSource) throws Exception {
        // given, when
        Locator.fromCqlSource(cqlSource);

        // then
        // CassandraTestException
    }

    @DataProvider(name = "fileDoesNotExistData")
    public static Object[][] fileDoesNotExistData() {
        return new Object[][] {
                { "file:target/dasdlajsdjasdjlasjdlajsdkl" },
                { "file.cql:target/dasdlajsdjasdjlasjdlajsdkl" },
                { "classpath:dasdasdasdasdasdasd" },
                { "classpath.cql:dasdasdasdasdasdasd" },
        };
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void getReaderWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromUrl(cqlSource);

        // when
        locator.getReader();

        // then
        // FileNotFoundException
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void getStreamWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromUrl(cqlSource);

        // when
        locator.getStream();

        // then
        // FileNotFoundException
    }

    @DataProvider(name = "directoryData")
    public static Object[][] directoryData() {
        return new Object[][] {
                { "file:target" },
                { "file.cql:target" },
                { "classpath:cql" },
                { "classpath.cql:cql" },
        };
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void getStreamWithDirectory(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromUrl(cqlSource);

        // when
        locator.getStream();

        // then
        // UnsupportedOperationException
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void getReaderWithDirectory(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromUrl(cqlSource);

        // when
        locator.getReader();

        // then
        // UnsupportedOperationException
    }
}

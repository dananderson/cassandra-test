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
import static org.unittested.cassandra.test.io.Locator.ContentType.*;
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
                // CQL statement, no source prefix
                { cqlInsert, TEXT, CQL, cqlInsert },
                { ";", TEXT, CQL, ";" },
                // text source
                { "text:" + cqlInsert, TEXT, CQL, cqlInsert },
                { "text: " + cqlInsert, TEXT, CQL, " " + cqlInsert },
                { "text:;", TEXT, CQL, ";" },
                { "text: ", TEXT, CQL, " " },
                { "text: =? ", TEXT, CQL, " =? " },
                { "text:", TEXT, CQL, "" },
                // classpath source
                { "classpath:" + classpathFile, CLASSPATH, CQL, classpathFile },
                // file source
                { "file:" + file, FILE, CQL, file  },
                // blank string
                { "", TEXT, CQL, "" },
                { " ", TEXT, CQL, "" },
        };
    }

    @Test(dataProvider = "cqlSourceData")
    public void fromCqlSource(String cqlSource,
                              Locator.Source expectedSource,
                              Locator.ContentType expectedConent,
                              String expectedPath) throws Exception {
        // given
        // data provider

        // when
        Locator locator = Locator.fromCqlSource(cqlSource);

        // then
        assertThat(locator.getSource(), is(expectedSource));
        assertThat(locator.getContentType(), is(expectedConent));
        assertThat(locator.getReader(), notNullValue());
        assertThat(locator.getStream(), notNullValue());
        assertThat(locator.getPath(), is(expectedPath));
    }

    @DataProvider(name = "invalidCqlSourceFormatData")
    public static Object[][] invalidCqlSourceFormatData() {
        return new Object[][] {
                // unknown source
                { "invalid:path.cql" },
                // blank paths
                { "file:" },
                { "file: " },
                { "classpath:" },
                { "classpath: " },
                // garbage
                { "dasdasdasd" },
        };
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
                { "classpath:dasdasdasdasdasdasd" },
        };
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void getReaderWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromCqlSource(cqlSource);

        // when
        locator.getReader();

        // then
        // FileNotFoundException
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void getStreamWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromCqlSource(cqlSource);

        // when
        locator.getStream();

        // then
        // FileNotFoundException
    }

    @DataProvider(name = "directoryData")
    public static Object[][] directoryData() {
        return new Object[][] {
                { "file:target" },
                { "classpath:cql" },
        };
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void getStreamWithDirectory(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromCqlSource(cqlSource);

        // when
        locator.getStream();

        // then
        // UnsupportedOperationException
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void getReaderWithDirectory(String cqlSource) throws Exception {
        // given
        Locator locator = Locator.fromUri(cqlSource);

        // when
        locator.getReader();

        // then
        // UnsupportedOperationException
    }
}

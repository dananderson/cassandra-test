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

import static org.unittested.cassandra.test.resource.Resource.Source.*;
import static org.unittested.cassandra.test.resource.Resource.ContentType.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.FileNotFoundException;

import org.unittested.cassandra.test.exception.CassandraTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ResourceTest {

    @DataProvider
    public static Object[][] validCqlOrUrlInput() {
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

    @Test(dataProvider = "validCqlOrUrlInput")
    public void fromCqlOrUrl(String cqlSource,
                             Resource.Source expectedSource,
                             Resource.ContentType expectedContent,
                             String expectedPath) throws Exception {
        // given
        // data provider

        // when
        Resource resource = Resource.fromCqlOrUrl(cqlSource);

        // then
        assertThat(resource.getSource(), is(expectedSource));
        assertThat(resource.getContentType(), is(expectedContent));
        assertThat(resource.getReader(), notNullValue());
        assertThat(resource.getStream(), notNullValue());
        assertThat(resource.getPath(), is(expectedPath));
    }

    @DataProvider
    public static Object[][] invalidCqlOrUrlInput() {
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

    @Test(dataProvider = "invalidCqlOrUrlInput", expectedExceptions = CassandraTestException.class)
    public void fromCqlOrUrlWithInvalidInput(String cqlSource) throws Exception {
        // given, when
        Resource.fromCqlOrUrl(cqlSource);

        // then
        // CassandraTestException
    }

    @DataProvider
    public static Object[][] fileDoesNotExistData() {
        return new Object[][] {
                { "file:target/dasdlajsdjasdjlasjdlajsdkl" },
                { "classpath:dasdasdasdasdasdasd" },
        };
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void getReaderWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        Resource resource = Resource.fromCqlOrUrl(cqlSource);

        // when
        resource.getReader();

        // then
        // FileNotFoundException
    }

    @Test(dataProvider = "fileDoesNotExistData", expectedExceptions = FileNotFoundException.class)
    public void getStreamWithFileThatDoesNotExist(String cqlSource) throws Exception {
        // given
        Resource resource = Resource.fromCqlOrUrl(cqlSource);

        // when
        resource.getStream();

        // then
        // FileNotFoundException
    }

    @DataProvider
    public static Object[][] directoryData() {
        return new Object[][] {
                { "file:target" },
                { "classpath:cql" },
        };
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void getStreamWithDirectory(String cqlSource) throws Exception {
        // given
        Resource resource = Resource.fromCqlOrUrl(cqlSource);

        // when
        resource.getStream();

        // then
        // UnsupportedOperationException
    }

    @Test(dataProvider = "directoryData", expectedExceptions = UnsupportedOperationException.class)
    public void getReaderWithDirectory(String cqlSource) throws Exception {
        // given
        Resource resource = Resource.fromCqlOrUrl(cqlSource);

        // when
        resource.getReader();

        // then
        // UnsupportedOperationException
    }

    @Test
    public void equalsAndHashCode() throws Exception {
        EqualsVerifier.forClass(Resource.class)
                .usingGetClass()
                .allFieldsShouldBeUsed()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}

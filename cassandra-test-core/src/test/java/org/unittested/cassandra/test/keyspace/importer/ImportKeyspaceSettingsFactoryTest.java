package org.unittested.cassandra.test.keyspace.importer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.FactoryTestAnnotations;
import org.unittested.cassandra.test.annotation.CassandraImportKeyspace;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertiesPropertyResolver;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImportKeyspaceSettingsFactoryTest {

    @Test
    public void create() throws Exception {
        // given
        KeyspaceSettingsFactory keyspaceSettingsFactory = new ImportKeyspaceSettingsFactory();
        CassandraImportKeyspace immutable = FactoryTestAnnotations.class.getAnnotation(CassandraImportKeyspace.class);

        // when
        KeyspaceSettings keyspaceSettings = keyspaceSettingsFactory.create(immutable, PropertiesPropertyResolver.DEFAULT);

        // then
        assertThat(keyspaceSettings, instanceOf(ImportKeyspaceSettings.class));
        ImportKeyspaceSettings importKeyspaceSettings = (ImportKeyspaceSettings)keyspaceSettings;
        assertThat(importKeyspaceSettings.hashCode(), is(723417303));
        assertThat(importKeyspaceSettings.getKeyspace(), is("test"));
        assertThat(importKeyspaceSettings.canDropKeyspace(), is(false));
        assertThat(importKeyspaceSettings.getProtectedTables(), arrayContaining("p"));
    }

    @DataProvider
    public Object[][] invalidAnnotations() {
        return new Object[][] {
                { null },
                { FactoryTestAnnotations.createStubAnnotation() },
        };
    }

    @Test(dataProvider = "invalidAnnotations", expectedExceptions = CassandraTestException.class)
    public void createWithInvalidAnnotations(Annotation annotation) throws Exception {
        // given
        KeyspaceSettingsFactory keyspaceSettingsFactory = new ImportKeyspaceSettingsFactory();

        // when
        keyspaceSettingsFactory.create(annotation, PropertiesPropertyResolver.DEFAULT);

        // then
        // expect IllegalArgumentException
    }
}

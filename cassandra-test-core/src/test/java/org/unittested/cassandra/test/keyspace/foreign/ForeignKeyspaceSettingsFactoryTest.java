package org.unittested.cassandra.test.keyspace.foreign;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.FactoryTestAnnotations;
import org.unittested.cassandra.test.annotation.CassandraForeignKeyspace;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.property.system.JavaPropertyResolver;
import org.unittested.cassandra.test.keyspace.KeyspaceSettings;
import org.unittested.cassandra.test.keyspace.KeyspaceSettingsFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ForeignKeyspaceSettingsFactoryTest {

    @Test
    public void create() throws Exception {
        // given
        KeyspaceSettingsFactory keyspaceSettingsFactory = new ForeignKeyspaceSettingsFactory();
        CassandraForeignKeyspace immutable = FactoryTestAnnotations.class.getAnnotation(CassandraForeignKeyspace.class);

        // when
        KeyspaceSettings keyspaceSettings = keyspaceSettingsFactory.create(immutable, new JavaPropertyResolver());

        // then
        assertThat(keyspaceSettings, instanceOf(ForeignKeyspaceSettings.class));
        ForeignKeyspaceSettings foreignSchemaSettings = (ForeignKeyspaceSettings)keyspaceSettings;
        assertThat(foreignSchemaSettings.hashCode(), is(723417303));
        assertThat(foreignSchemaSettings.getKeyspace(), is("test"));
        assertThat(foreignSchemaSettings.canDropKeyspace(), is(false));
        assertThat(foreignSchemaSettings.getProtectedTables(), arrayContaining("p"));
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
        KeyspaceSettingsFactory keyspaceSettingsFactory = new ForeignKeyspaceSettingsFactory();

        // when
        keyspaceSettingsFactory.create(annotation, new JavaPropertyResolver());

        // then
        // expect IllegalArgumentException
    }
}

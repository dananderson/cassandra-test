package org.unittested.cassandra.test.data.basic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.FactoryTestAnnotations;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.DataSettingsFactory;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.property.system.JavaPropertyResolver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BasicDataSettingsFactoryTest {

    @Test
    public void create() throws Exception {
        // given
        DataSettingsFactory dataSettingsFactory = new BasicDataSettingsFactory();
        CassandraData data = FactoryTestAnnotations.class.getAnnotation(CassandraData.class);

        // when
        DataSettings dataSettings = dataSettingsFactory.create(data, new JavaPropertyResolver());

        // then
        assertThat(dataSettings, instanceOf(BasicDataSettings.class));
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
        DataSettingsFactory dataSettingsFactory = new BasicDataSettingsFactory();

        // when
        dataSettingsFactory.create(annotation, new JavaPropertyResolver());

        // then
        // expect IllegalArgumentException
    }
}

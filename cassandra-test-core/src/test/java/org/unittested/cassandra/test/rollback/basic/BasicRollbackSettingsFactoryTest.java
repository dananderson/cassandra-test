package org.unittested.cassandra.test.rollback.basic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.lang.annotation.Annotation;

import org.unittested.cassandra.test.FactoryTestAnnotations;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.properties.PropertiesPropertyResolver;
import org.unittested.cassandra.test.rollback.RollbackSettings;
import org.unittested.cassandra.test.rollback.RollbackSettingsFactory;
import org.unittested.cassandra.test.rollback.RollbackStrategy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BasicRollbackSettingsFactoryTest {

    @Test
    public void create() throws Exception {
        // given
        RollbackSettingsFactory rollbackSettingsFactory = new BasicRollbackSettingsFactory();
        CassandraRollback rollback = FactoryTestAnnotations.class.getAnnotation(CassandraRollback.class);

        // when
        RollbackSettings rollbackSettings = rollbackSettingsFactory.create(rollback, PropertiesPropertyResolver.SYSTEM);

        // then
        assertThat(rollbackSettings, instanceOf(BasicRollbackSettings.class));
        BasicRollbackSettings basicRollbackSettings = (BasicRollbackSettings)rollbackSettings;
        assertThat(basicRollbackSettings.getTableExclusions().length, is(0));
        assertThat(basicRollbackSettings.getTableInclusions(), arrayContaining("i"));
        assertThat(basicRollbackSettings.getAfterClass(), is(RollbackStrategy.TRUNCATE));
        assertThat(basicRollbackSettings.getAfterMethod(), is(RollbackStrategy.NONE));
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
        RollbackSettingsFactory rollbackSettingsFactory = new BasicRollbackSettingsFactory();

        // when
        rollbackSettingsFactory.create(annotation, PropertiesPropertyResolver.SYSTEM);

        // then
        // expect IllegalArgumentException
    }
}

package org.unittested.cassandra.test.spring;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.unittested.cassandra.test.TestEnvironmentAdapter;
import org.unittested.cassandra.test.TestSettings;
import org.unittested.cassandra.test.TestSettingsBuilder;
import org.unittested.cassandra.test.exception.CassandraTestException;

/**
 * {@link TestExecutionListener} that provides support for writing tests against a Cassandra database configured by
 * Cassandra Test annotations that control connection, schema, data import and rollbacks.
 * <p>
 * This test execution listener is attached to a JUnit or TestNG Spring Test using the {@link TestExecutionListeners}
 * annotation.
 * <p>
 * No Cassandra Test base test classes are provided in the Spring environment due to multiple inheritance issues that
 * would quickly arise. The Cassandra Test base classes provide API to objects that test writers will find useful, including
 * {@link com.datastax.driver.core.Session} and {@link org.unittested.cassandra.test.Keyspace}. For tests using this
 * test execution lister, the {@link org.unittested.cassandra.test.annotation.CassandraBean} annotation can be used
 * to autowire these objects.
 */
public class SpringCassandraTestExecutionListener implements TestExecutionListener {

    private TestEnvironmentAdapter adapter;

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        if (this.adapter != null) {
            this.adapter.onPrepareTestInstance(testContext.getTestInstance(), testContext);
        }
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        TestSettings testSettings = createTestSettings(testContext);

        this.adapter = createAdapter(testContext, testSettings);

        if (this.adapter == null) {
            throw new CassandraTestException("Failed to create a TestEnvironmentAdapter.");
        }

        this.adapter.onBeforeClass(testContext.getTestClass(), testContext);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        if (this.adapter != null) {
            this.adapter.onBeforeMethod(testContext.getTestInstance(), testContext.getTestMethod(), testContext);
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (this.adapter != null) {
            this.adapter.onAfterMethod(testContext.getTestInstance(), testContext.getTestMethod(), testContext);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        if (this.adapter != null) {
            this.adapter.onAfterClass(testContext.getTestClass(), testContext);
        }
    }

    protected TestSettings createTestSettings(TestContext testContext) {
        return TestSettingsBuilder.fromAnnotatedElement(
                testContext.getTestClass(),
                new SpringEnvironmentPropertyResolver(testContext.getApplicationContext().getEnvironment()));
    }

    protected TestEnvironmentAdapter createAdapter(TestContext testContext, TestSettings testSettings) {
        return new TestEnvironmentAdapter(testSettings);
    }
}

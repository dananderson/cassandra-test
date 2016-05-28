package org.unittested.cassandra.test.spring;

import org.springframework.core.env.Environment;
import org.unittested.cassandra.test.property.AbstractPropertyResolver;

/**
 * {@link org.unittested.cassandra.test.property.PropertyResolver} that reads properties from a Spring
 * {@link Environment}.
 */
public class SpringEnvironmentPropertyResolver extends AbstractPropertyResolver {

    private Environment environment;

    public SpringEnvironmentPropertyResolver(final Environment environment) {
        this.environment = environment;
    }

    @Override
    protected String getProperty(final String propertyName, final String defaultValue) {
        return this.environment.getProperty(propertyName, defaultValue);
    }
}

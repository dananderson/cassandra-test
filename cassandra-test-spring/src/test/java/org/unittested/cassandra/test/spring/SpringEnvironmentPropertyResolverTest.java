package org.unittested.cassandra.test.spring;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.springframework.mock.env.MockEnvironment;
import org.testng.annotations.Test;

public class SpringEnvironmentPropertyResolverTest {

    @Test
    public void resolve() throws Exception {
        // given
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("key", "value");
        SpringEnvironmentPropertyResolver resolver = new SpringEnvironmentPropertyResolver(environment);

        // when
        String value = resolver.resolve("${key}");

        // then
        assertThat(value, is("value"));
    }

    @Test
    public void resolveArray() throws Exception {
        // given
        MockEnvironment environment = new MockEnvironment();
        environment.withProperty("key1", "value1").withProperty("key2", "value2");
        SpringEnvironmentPropertyResolver resolver = new SpringEnvironmentPropertyResolver(environment);

        // when
        String [] value = resolver.resolve(new String [] { "${key1}", "${key2}" });

        // then
        assertThat(value, arrayContaining("value1", "value2"));
    }
}

# Cassandra Test
[![Build Status](https://travis-ci.org/dananderson/cassandra-test.svg?branch=master)](https://travis-ci.org/dananderson/cassandra-test.svg?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.unittested/cassandra-test-project/badge.svg)](http://mvnrepository.com/artifact/org.unittested/cassandra-test-junit/1.0.0)
[![codecov.io](https://codecov.io/github/dananderson/cassandra-test/coverage.svg?branch=master)](https://codecov.io/github/dananderson/cassandra-test?branch=master)

Cassandra Test is a unit test and integration test framework featuring support for multiple test environments, schema management, table data loading and connection management.

```java
@CassandraKeyspace(keyspace = "testng_cassandra_test", schema = "classpath:sample-schema.cql")
@CassandraData(data = "classpath:sample-data.cql")
public class TestNGSampleTest extends AbstractTestNGCassandraTest {

    @Test
    public void timeseriesRowCount() throws Exception {
        assertThat(getKeyspace().getTable("timeseries).getCount(), is(3L));
    }
}
```

## Getting Cassandra Test

Cassandra Test modules are organized by the supported test environments: TestNG, JUnit and Spring Test. To include
Cassandra Test in your project, choose the artifactId that matches your test environment.

[TestNG](casandra-test-testng)
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-testng</artifactId>
      <version>1.0.0</version>
    </dependency>
```
[JUnit](casandra-test-junit)
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-junit</artifactId>
      <version>1.0.0</version>
    </dependency>
```
[Spring Test](casandra-test-spring)
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-spring</artifactId>
      <version>1.0.0</version>
    </dependency>
```

## Compatibility
Cassandra Test has been built and tested with:

| Technology            | Supported Versions           |
| --------------------- | ---------------------------- |
| Datastax Java Driver  | 2.0.2+, 2.1.0+, 3.0.0+       |
| Apache Cassandra      | 2.0.4+ 2.1.0+ 2.2.0+ 3.0.0+  |
| Java                  | 6+                           |

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Contributing
Pull requests for bug fixes and new features are welcome.

The following items are being considered for future releases of Cassandra Test.
- Improve README documentation across the project.
- Wiki documentation.
- Create an example usage project.
- Add method level annotations.
- Investigate other data loading source file formats, including JSON, YAML, XML and CSV.
- Investigate "golden" data for table data state verification.
- Add plugins for schema management tools.
- Investigate support for managing Cassandra instances with CCM, Embedded Cassandra and SCassandra.
- Investigate negative testing tools to put Cassandra into bad states (node down, etc).
- Performance: session cache, file read cache, etc.
- Parallel test support (why?).
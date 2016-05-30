# Cassandra Test
[![Build Status](https://travis-ci.org/dananderson/cassandra-test.svg?branch=master)](https://travis-ci.org/dananderson/cassandra-test.svg?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.unittested/cassandra-test-project/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.unittested/cassandra-test-project/badge.svg)
[![codecov.io](https://codecov.io/github/dananderson/cassandra-test/coverage.svg?branch=master)](https://codecov.io/github/dananderson/cassandra-test?branch=master)
Java unit testing framework for Cassandra.

## Features

- Works with multiple test environments, including TestNG, JUnit and Spring Test.
- Schema installation, control and monitoring.
- Pseudo-rollbacks to ensure a Cassandra keyspace is in a known state before a test rungs.
- Load table data from CQL files.
- Ability to plug in specialized connection, schema, rollback and data importing behavior.

## Getting Cassandra Test
Cassandra Test modules are organized by test environment integration. Pick an environment that fits your project and start writing some Cassandra tests.
### TestNG
**Maven**
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-testng</artifactId>
      <version>1.0.0</version>
    </dependency>
```
**Gradle**
```
compile group: 'org.unittested', name: 'cassandra-test-testng', version: '1.0.0'
```
### JUnit
**Maven**
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-junit</artifactId>
      <version>1.0.0</version>
    </dependency>
```
**Gradle**
```
compile group: 'org.unittested', name: 'cassandra-test-junit', version: '1.0.0'
```
### Spring Test
**Maven**
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-spring</artifactId>
      <version>1.0.0</version>
    </dependency>
```
**Gradle**
```
compile group: 'org.unittested', name: 'cassandra-test-spring', version: '1.0.0'
```
### Custom
**Maven**
```
    <dependency>
      <groupId>org.unittested</groupId>
      <artifactId>cassandra-test-core</artifactId>
      <version>1.0.0</version>
    </dependency>
```
**Gradle**
```
compile group: 'org.unittested', name: 'cassandra-test-core', version: '1.0.0'
```

## Example Test
This test connects to a Cassandra cluster at 127.0.0.1:9042, creates a keyspace,
installs schema from a cql file and populates table data from a cql file. The test has
access to the driver Session. When the test completes, the keyspace is cleaned up, leaving
Cassandra ready for the next test.

```java
@CassandraKeyspace(keyspace = "testng_cassandra_test", schema = "classpath:sample-schema.cql")
@CassandraData(data = "classpath:sample-data.cql")
public class TestNGSampleTest extends AbstractTestNGCassandraTest {

    @Test
    public void timeseriesRowCount() throws Exception {
        ResultSet result = getSession().execute("SELECT COUNT(*) FROM timeseries");
        assertThat(result.one().getLong(0), is(3L));
    }
}

```


## Compatibility
Cassandra Test has been built and tested with the following technologies.

**Datastax Java Driver**
- 2.0.2+
- 2.1.0+
- 3.0.0+

**Apache Cassandra**
- 2.0.4+
- 2.1.0+
- 2.2.0+
- 3.0.0+

**Java**
- 6+

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Roadmap
These work items are being considered for Cassandra Test.

- Publish artifacts on Maven Central.
- README.md docs for each module.
- Wiki documentation.
- Create an examples project.
- JSON data loading.
- XML data loading.
- YAML data loading.
- CCM support.
- Embedded Cassandra support.
- SCassandra support.
- Add plugins for popular schema managers.
- Performance: file caching, session caching, etc.
- Parallel test support (?)
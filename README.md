# Cassandra Test
[![Build Status](https://travis-ci.org/dananderson/cassandra-test.svg?branch=master)](https://travis-ci.org/dananderson/cassandra-test.svg?branch=master)
[![codecov.io](https://codecov.io/github/dananderson/cassandra-test/coverage.svg?branch=master)](https://codecov.io/github/dananderson/cassandra-test?branch=master)

Cassandra Test is a Java test framework for writing unit tests and integration tests against a Cassandra database.

## Features

- Connection management
- Keyspace and schema management
- Data loading from CQL files
- Rollbacks
- TestNG, JUnit 4 and Spring Test support
- Backwards compatible with Java 6+, Cassandra 2.x & 3.x and Java Driver 2.x & 3.x
- Highly configurable


## Getting Cassandra Test

Cassandra Test publishes an artifact for each supported test framework.

| Test Framework | Cassandra Test Artifact |
| --- | --- |
| TestNG | [org.unittested:cassandra-test-testng:1.0.3](http://search.maven.org/#artifactdetails%7Corg.unittested%7Ccassandra-test-testng%7C1.0.3%7Cjar) |
| JUnit 4 | [org.unittested:cassandra-test-junit:1.0.3](http://search.maven.org/#artifactdetails%7Corg.unittested%7Ccassandra-test-junit%7C1.0.3%7Cjar) |
| Spring Test | [org.unittested:cassandra-test-spring:1.0.3](http://search.maven.org/#artifactdetails%7Corg.unittested%7Ccassandra-test-spring%7C1.0.3%7Cjar) |

If your environment requires a custom integration, use the Cassandra Test Core artifact: [org.unittested:cassandra-test-core:1.0.3](http://search.maven.org/#artifactdetails%7Corg.unittested%7Ccassandra-test-core%7C1.0.3%7Cjar). The **TestEnvironmentAdapter** can be used to connect test life cycle events to Cassandra Test.

## Writing Tests

### Using Test Framework Bindings

Each test framework has a slightly different way to bind with Cassandra Test. Once bound, any test in any test framework can use Cassandra Test annotations to configure Cassandra behavior.

#### TestNG

In the TestNG environment, tests can extend **AbstractTestNGCassandraTest** to be a Cassandra Test. **Session** and **Cluster** connection objects are available through this base class.

```java
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```
#### JUnit 4

In the JUnit 4 environment, multiple binding options are available.

First, tests can extend **AbstractJUnit4CassandraTest** to be a Cassandra Test. **Session** and **Cluster** connection objects are available through this base class.


```java
public class MyTest extends AbstractJUnit4CassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```

Second, tests can use **CassandraClassRule** and **CassandraRule** to be a Cassandra Test. **Session** and **Cluster** connection objects are available through the **CassandraRule** instance.

```java
public class MyTest {
    @ClassRule
    public static CassandraClassRule classRule = new CassandraClassRule();

    @Rule
    public CassandraRule cassandraRule = new CassandraRule(classRule);

    @Test
    public cassandraTest() {
        ...
    }
}
```
#### Spring Test

In the Spring Test environment, tests add **SpringCassandraTestExecutionListener** to be a Cassandra Test. **Session** and **Cluster** connection objects are available through the Autowire-like annotation @CassandraBean.

```java
@TestExecutionListeners(value = SpringCassandraTestExecutionListener.class, mergeMode = MERGE_WITH_DEFAULTS)
public class MyTest extends AbstractTestNGSpringContextTests {

    @CassandraBean
    private Session session;

    @Test
    public cassandraTest() {
        ...
    }
}
```

### Configuration

Cassandra Tests can be configured through Java annotations.

#### Connection

When a test is bound to Cassandra Test, the test will automatically try to connect to a Cassandra cluster
at 127.0.0.1:9042 with no authentication. If the connection requires a different configuration, @CassandraConnect
can be used.

```java
@Connect(host = "127.0.0.2", user = "cassandra", password = "cassandra")
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```
#### Keyspace and Schema

The primary use case for Cassandra Test is to run tests against a single keyspace. Cassandra Test manages this
keyspace by installing schema, detecting schema modifications and rebuilding schema as necessary to maintain
consistent state across tests.

The @CassandraKeyspace annotation gives Cassandra Test full control of a keyspace. The annotation describes
how to create the keyspace and how to create it's schema. If Cassandra Test detects that a keyspace has been
altered from this state, Cassandra Test will drop and re-create the keyspace before the next test run.

```java
@CassandraKeyspace(value = "my_keyspace", schema = "classpath:my_keyspace_schema.cql")
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```

The @CassandraImportKeyspace annotation declares a schema and keyspace that has been configured outside of
the control of Cassandra Test. Cassandra Test cannot re-create or drop this keyspace. If a test modifies the
keyspace schema in any way, the test automatically fails.

```java
@CassandraImportKeyspace("my_keyspace")
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```

#### Data Loading

Table data can be loaded using the @CassandraData annotation. Currently, data loading supports CQL files containing
CQL statements delimited by ";". The format is exactly the same as CQL files executable by cqlsh.

```java
@CassandraKeyspace(value = "my_keyspace", schema = "classpath:my_keyspace_schema.cql")
@CassandraData("classpath:my_keyspace_table_data.cql")
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```

#### Rollback

When a test completes, Cassandra Test will perform a pseudo-rollback or clean up of the keyspace. The clean up
can be truncation of keyspace tables or dropping the keyspace. The rollback behavior is configured through the
@CassandraRollback annotation.

```java
@CassandraKeyspace(value = "my_keyspace", schema = "classpath:my_keyspace_schema.cql")
@CassandraData("classpath:my_keyspace_table_data.cql")
@CassandraRollback(afterClass=RollbackStrategy.DROP)
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```

#### Properties

For larger projects, hard coding keyspace names and hosts in annotations may not be clean or practical. Cassandra Test
supports property references in annotations to address the problem. Properties are sourced from a Java properties file. By
default, /cassandra-test.properties from the class path is used. Otherwise, a properties file can be specified with the @CassandraProperties
annotation.

```java
@CassandraImportKeyspace("${cassandra.test.keyspace}")
public class MyTest extends AbstractTestNGCassandraTest {
    @Test
    public cassandraTest() {
        ...
    }
}
```

## Compatibility
Cassandra Test has been built and tested with:

| Technology            | Supported Versions              |
| --------------------- | ----------------------------    |
| Datastax Java Driver  | 2.0.2+, 2.1.0+, 3.0.0+          |
| Apache Cassandra      | 2.0.4+, 2.1.0+, 2.2.0+, 3.0.0+  |
| Java                  | 6+                              |

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
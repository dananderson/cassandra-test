# Cassandra Test

Cassandra Test is a comprehensive Java unit test framework writing tests against a Cassandra database.

## Features

- Schema management.
- Rollback table mutations and schema changes.
- Table data import.
- Support for multiple test environments.

## Usage

Start writing some tests! Example with the TestNG module:

```java
@CassandraConnect(host = "127.0.0.1")
@CassandraKeyspace(keyspace = "test", schema = "CREATE TABLE t (id INT PRIMARY KEY);")
@CassandraData(data = "INSERT INTO t(id) VALUES (1000);")
@CassandraRollback(afterMethod = RollbackStrategy.TRUNCATE)
public class TestNgExampleTest extends AbstractTestNgCassandraTest {

    /*
     * Before the test method executes:
     * 1. The connection to the Cassandra cluster at 127.0.0.1 is established. The Session
     *    object is available to the test method.
     * 2. The "test" keyspace is created and the "t" table is added to it.
     * 3. Data is loaded into the "t" table.
     */

    @Test
    public void test() throws Exception {
        Row row = getSession().execute("SELECT * FROM t").one();

        assertThat(row.getInt("id"), is(1000));
    }

    /*
     * After the test method executes:
     * 1. Truncate the "t" table to make sure that Cassandra is in a known state before executing the next test.
     */

     ...

```


## Compatibility

Cassandra Test strives to be backwards compatible with older versions of Cassandra, Datastax Driver and Java. Many projects
are not at the bleeding edge of these technologies, but they should still be able to write tests.

- Apache Cassandra 2.0.0+
- Java 6+
- Datastax Java Driver 2.0.x+, 2.1.x+, 3.x+.

## Roadmap

The following items are being considered for future versions of Cassandra Test.

- Publish artifacts on Maven Central.
- Spring test environment module.
- JUnit test environment module.
- JSON data loading.
- XML data loading.
- YAML data loading.
- CCM support.
- Embedded Cassandra support.
- SCassandra support.
- Add plugins for popular schema managers.
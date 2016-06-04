## 1.0.1
- Overriding TestSettings is now consistent across environments.
- Clean up urls and resource loading.
- Made TestNG, Spring, JUnit and Driver provided dependencies.
- Add @CassandraProperties for specifying a properties file for connection and keyspace settings.
- Rename @CassandraForeignKeyspace to @CassandraImportKeyspace
- Rename JUnit rules.
- Add Table class for common table operations.
- Fix bug in schema change detection.
- General doc updates, bug fixes and expansion of tests.
## 1.0.0
- JUnit, TestNG and Spring Test test environment integrations.
- Simple connection specification with @CassandraConnect.
- Test (internal) keyspace and schema management with @CassandraForeignKeyspace.
- Foreign (external) keyspace management with @CassandraKeyspace.
- Data import from CQL files with @CassandraData.
- Data rollbacks with truncate tables or drop keyspace with @CassandraRollback.
- Load connection and keyspace settings from properties file.
- Autowire Cassandra Test objects with @CassandraBean.

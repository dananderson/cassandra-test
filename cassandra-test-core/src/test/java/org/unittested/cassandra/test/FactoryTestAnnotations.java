package org.unittested.cassandra.test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

import org.unittested.cassandra.test.annotation.CassandraConnect;
import org.unittested.cassandra.test.annotation.CassandraData;
import org.unittested.cassandra.test.annotation.CassandraImportKeyspace;
import org.unittested.cassandra.test.annotation.CassandraRollback;
import org.unittested.cassandra.test.annotation.CassandraKeyspace;
import org.unittested.cassandra.test.rollback.RollbackStrategy;

@CassandraConnect(username = "cassandra", password = "cassandra")
@CassandraKeyspace(value = "test", autoCreateKeyspace = "true", schema = "classpath-cql:schema.cql", protectedTables = "p")
@CassandraImportKeyspace(value = "test", protectedTables = "p")
@CassandraRollback(tableInclusions = "i", afterMethod = RollbackStrategy.NONE, afterClass = RollbackStrategy.TRUNCATE)
@CassandraData("d")
public class FactoryTestAnnotations {

    public static Annotation createStubAnnotation() {
        return new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Inherited.class;
            }
        };
    }
}

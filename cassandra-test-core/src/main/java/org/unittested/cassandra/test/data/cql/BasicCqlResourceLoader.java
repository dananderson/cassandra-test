/*
 * Copyright (C) 2016 Daniel Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.unittested.cassandra.test.data.cql;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.resource.Resource;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public class BasicCqlResourceLoader implements CqlResourceLoader {

    private Map<Resource, Collection<Statement>> cache;

    public BasicCqlResourceLoader() {
        this(true);
    }

    public BasicCqlResourceLoader(boolean enableCache) {
        this.cache = (enableCache ? new HashMap<Resource, Collection<Statement>>() : null);
    }

    @Override
    public void loadCqlResource(TestRuntime runtime, Resource resource) throws IOException {
        StatementReader reader = null;
        ConsistencyStatement consistency = null;

        try {
            if (this.cache == null) {
                reader = new CqlStatementReader(resource.getReader());

                while (reader.hasMore()) {
                    consistency = executeStatement(runtime.getKeyspace().getSession(), reader.one(), consistency);
                }
            } else {
                if (!this.cache.containsKey(resource)) {
                    reader = new CqlStatementReader(resource.getReader());
                    this.cache.put(resource, reader.all());
                }

                for (Statement statement : this.cache.get(resource)) {
                    consistency = executeStatement(runtime.getKeyspace().getSession(), statement, consistency);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private ConsistencyStatement executeStatement(Session session, Statement statement, ConsistencyStatement consistency) {
        if (statement instanceof ConsistencyStatement) {
            return (ConsistencyStatement)statement;
        }

        if (consistency != null) {
            consistency.applyConsistency(statement);
        }

        session.execute(statement);

        return consistency;
    }
}

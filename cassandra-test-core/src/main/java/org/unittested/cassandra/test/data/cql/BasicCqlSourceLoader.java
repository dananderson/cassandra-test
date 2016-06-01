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

import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.io.Locator;

import com.datastax.driver.core.Statement;

public class BasicCqlSourceLoader implements CqlSourceLoader {

    public BasicCqlSourceLoader() {

    }

    @Override
    public void loadCqlSource(TestRuntime runtime, String cqlSource) throws IOException {
        StatementReader reader = null;

        try {
            ConsistencyStatement consistency = null;
            reader = createStatementReader(cqlSource);

            while (reader.hasMore()) {
                Statement statement = reader.one();

                if (statement instanceof ConsistencyStatement) {
                    consistency = (ConsistencyStatement)statement;
                } else {
                    if (consistency != null) {
                        consistency.applyConsistency(statement);
                    }
                    runtime.getKeyspace().getSession().execute(statement);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private StatementReader createStatementReader(String cqlSource) throws IOException {
        Locator locator = Locator.fromCqlSource(cqlSource);
        return new CqlStatementReader(locator.getReader());
    }
}

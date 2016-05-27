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

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/**
 * Represents a cqlsh CONSISTENCY command.
 *
 * This {@link Statement} is not directly executed by the driver. The {@link Statement} class is used so the command
 * can be put into a {@link StatementReader} stream. The stream reader is responsible for applying the consistency to
 * subsequent statements in the stream.
 */
class ConsistencyStatement extends SimpleStatement {

    private final ConsistencyLevel consistencyLevel;

    public ConsistencyStatement(ConsistencyLevel consistencyLevel) {
        super(null);
        this.consistencyLevel = consistencyLevel;
    }

    public Statement applyConsistency(Statement target) {
        switch(this.consistencyLevel) {
            case SERIAL:
            case LOCAL_SERIAL:
                target.setSerialConsistencyLevel(this.consistencyLevel);
                break;
            default:
                target.setConsistencyLevel(this.consistencyLevel);
                break;
        }

        return target;
    }
}

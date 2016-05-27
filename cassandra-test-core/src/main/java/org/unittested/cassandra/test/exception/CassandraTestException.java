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

package org.unittested.cassandra.test.exception;

/**
 * Top level class for exceptions thrown by Cassandra Test.
 */
public class CassandraTestException extends RuntimeException {

    public CassandraTestException(String message, Object ... args) {
        super(String.format(message, args));

        if (args.length > 0) {
            Object o = args[args.length - 1];
            if (o instanceof Throwable) {
                this.initCause((Throwable)o);
            }
        }
    }
}

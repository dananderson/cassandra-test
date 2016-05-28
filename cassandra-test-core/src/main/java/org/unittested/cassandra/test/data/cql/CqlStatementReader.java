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
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unittested.cassandra.test.util.Utils;
import org.unittested.cassandra.test.exception.CassandraTestException;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

/**
 * CQL statement reader.
 *
 * This reader parses streams of CQL statements delimited by ';', similar to the format of .cql files accepted by cqlsh.
 * The CQL parsing is loose to keep the parser simple. CQL statement validation leverages the Cassandra server.
 * <p>
 * The parser supports batch statements and consistency commands.
 */
class CqlStatementReader implements StatementReader {

    private static final Pattern CONSISTENCY = Pattern.compile("^CONSISTENCY\\s+(\\w+)\\s*;?$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern BEGIN_BATCH = Pattern.compile("^BEGIN\\s+(UNLOGGED\\s+|COUNTER\\s+)?BATCH(\\s+USING\\s+TIMESTAMP\\s+(\\d+))?\\s*;?\\s*(.*;)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern END_BATCH = Pattern.compile("^APPLY\\s+BATCH\\s*;?$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private PushbackReader reader;
    private StringBuilder cqlStringBuilder = new StringBuilder();
    private boolean atBeginningOfNextStatement;

    public CqlStatementReader(Reader reader) {
        this.reader = new PushbackReader(reader);
    }

    @Override
    public boolean hasMore() throws IOException {
        return skipToBeginningOfNextStatement();
    }

    @Override
    public Statement one() throws IOException {
        BatchStatement batchStatement = null;

        while (skipToBeginningOfNextStatement()) {
            String cql = readNextStatement();

            Matcher consistency = CONSISTENCY.matcher(cql);

            if (consistency.matches()) {
                if (batchStatement != null) {
                    throw new CassandraTestException("CONSISTENCY command cannot appear in a BATCH statement.");
                }

                return new ConsistencyStatement(ConsistencyLevel.valueOf(consistency.group(1).toUpperCase()));
            }

            // Close an open batch statement.
            if (END_BATCH.matcher(cql).matches()) {
                if (batchStatement == null) {
                    throw new CassandraTestException("APPLY BATCH without matching BEGIN BATCH.");
                }

                if (batchStatement.getStatements().isEmpty()) {
                    throw new CassandraTestException("Empty batch statement.");
                }

                break;
            }

            // Open a new batch statement.
            Matcher beginBatch = BEGIN_BATCH.matcher(cql);

            if (beginBatch.matches()) {
                if (batchStatement != null) {
                    throw new CassandraTestException("Nested BEGIN BATCH in batch statement %s", batchStatement.toString());
                }

                batchStatement = createBatchStatement(beginBatch.group(1), beginBatch.group(4), beginBatch.group(3));
                continue;
            }

            // Append to an open batch statement.
            if (batchStatement != null) {
                batchStatement.add(createSimpleStatement(cql));
                continue;
            }

            // Not processing a batch statement, so return the statement.
            return createSimpleStatement(cql);
        }

        return batchStatement;
    }

    @Override
    public Collection<Statement> all() throws IOException {
        Collection<Statement> statementList = new ArrayList<Statement>();

        while (hasMore()) {
            statementList.add(one());
        }

        return statementList;
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    private boolean skipToBeginningOfNextStatement() throws IOException {
        if (this.atBeginningOfNextStatement) {
            return true;
        }

        while (true) {
            int ch = next();

            if (ch == '-' || ch == '/') {
                skipComment(ch);
            } else if (ch == -1) {
                return false;
            } else if (ch != ';' && !Character.isWhitespace(ch)) {
                this.cqlStringBuilder.setLength(0);
                this.cqlStringBuilder.appendCodePoint(ch);
                this.atBeginningOfNextStatement = true;
                return true;
            }
        }
    }

    private String readNextStatement() throws IOException {
        StringBuilder cql = this.cqlStringBuilder;

        while (true) {
            int ch = next();

            if (ch == '\'' || ch == '"') {
                cql.appendCodePoint(ch);
                readQuotedString(ch, cql);
            } else if (ch == '/') {
                skipComment(ch);
            } else if (ch == '-') {
                // Inside of a statement a '-' could be part of a UUID.
                int next = next();

                if (Character.digit(next, 16) != -1) {
                    cql.appendCodePoint(ch);
                    cql.appendCodePoint(next);
                } else {
                    skipComment(ch, next);
                }
            } else if (ch == ';') {
                cql.appendCodePoint(ch);
                break;
            } else if (ch == -1) {
                break;
            } else {
                cql.appendCodePoint(ch);
            }
        }

        this.atBeginningOfNextStatement = false;
        return cql.toString();
    }

    private void skipComment(int commentChar) throws IOException {
        skipComment(commentChar, next());
    }

    private void skipComment(int commentChar, int next) throws IOException {
        if (next == commentChar) {
            skipToLineSeparator();
        } else if (commentChar == '/' && next == '*') {
            skipBlockComment();
        } else {
            throw new CassandraTestException("Illegal character '%s'.", codePointToString(commentChar));
        }
    }

    private void readQuotedString(int quoteCodePoint, StringBuilder target) throws IOException {
        while (true) {
            int ch = next();

            if (ch == -1) {
                throw new CassandraTestException("No closing '%s'.", codePointToString(quoteCodePoint));
            }

            target.appendCodePoint(ch);

            if (ch == quoteCodePoint) {
                int p = peek();
                if (p == quoteCodePoint) {
                    next();
                } else {
                    break;
                }
            }
        }
    }

    private void skipToLineSeparator() throws IOException {
        while (true) {
            int ch = next();

            if (ch == '\n' || ch == -1) {
                break;
            }
        }
    }

    private void skipBlockComment() throws IOException {
        int previous = -1;

        while (true) {
            int current = next();

            if (current == -1) {
                throw new CassandraTestException("Unterminated block comment.");
            }

            if (previous == '*' && current == '/') {
                break;
            }

            previous = current;
        }
    }

    private int next() throws IOException {
        return this.reader.read();
    }

    private int peek() throws IOException {
        int ch = this.reader.read();

        if (ch != -1) {
            this.reader.unread(ch);
        }

        return ch;
    }

    private BatchStatement createBatchStatement(String batchType, String firstStatement, String timestamp) {
        BatchStatement.Type type = (batchType != null ? BatchStatement.Type.valueOf(batchType.trim().toUpperCase()) : BatchStatement.Type.LOGGED);
        BatchStatement batchStatement = new BatchStatement(type);

        if (firstStatement == null || END_BATCH.matcher(firstStatement).matches()) {
            throw new CassandraTestException("Empty batch statement.");
        }

        if (!firstStatement.trim().equals(";")) {
            batchStatement.add(createSimpleStatement(firstStatement));
        }

        if (timestamp != null) {
            Utils.setDefaultTimestamp(batchStatement, Long.parseLong(timestamp));
        }

        return batchStatement;
    }

    private Statement createSimpleStatement(String cql) {
        if (!Utils.isCqlLike(cql)) {
            throw new CassandraTestException("Illegal CQL statement: %s", cql);
        }

        return new SimpleStatement(cql);
    }

    private String codePointToString(int codePoint) {
        return new String(new int [] { codePoint }, 0, 1);
    }
}

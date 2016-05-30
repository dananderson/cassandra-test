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

package org.unittested.cassandra.test.util;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

import com.datastax.driver.core.Session;

/**
 * Internal utilities.
 */
public final class Utils {

    private static final Pattern CQL = Pattern.compile("^\\s*(?:ALTER|BEGIN|CONSISTENCY|CREATE|DELETE|DROP|GRANT|INSERT|LIST|REVOKE|SELECT|SERIAL|TRUNCATE|UPDATE|USE)\\s",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern EMPTY_STATEMENT = Pattern.compile("^\\s*;\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private Utils() {

    }

    /**
     * Get system.local.schema_version from Cassandra.
     *
     * @param session {@link Session}
     * @return system.local.schema_version value
     */
    public static UUID getSchemaVersion(Session session) {
        return session
                .execute("select schema_version from system.local")
                .one()
                .getUUID("schema_version");
    }

    /**
     * Checks if a string kinda looks like a CQL statement.
     *
     * @param str String to check
     * @return {@link Boolean}
     */
    public static boolean isCqlLike(String str) {
        return (EMPTY_STATEMENT.matcher(str).matches() || CQL.matcher(str).find());
    }

    /**
     * Expand comma delimited entries in input array.
     * <p>
     * Example: [ "a,b", "c", "d,e"] to [ "a", "b", "c", "d", "e" ]
     *
     * @param input String array
     * @return Expanded string array
     */
    public static String [] expandCommaDelimitedEntries(String [] input) {
        ArrayList<String> output = new ArrayList<String>();

        for (String i : input) {
            for (String e : i.split(",")) {
                output.add(e.trim());
            }
        }

        return output.toArray(new String[output.size()]);
    }

}

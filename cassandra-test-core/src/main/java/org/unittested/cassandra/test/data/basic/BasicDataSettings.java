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

package org.unittested.cassandra.test.data.basic;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.unittested.cassandra.test.TestRuntime;
import org.unittested.cassandra.test.data.DataSettings;
import org.unittested.cassandra.test.data.cql.BasicCqlResourceLoader;
import org.unittested.cassandra.test.data.cql.CqlResourceLoader;
import org.unittested.cassandra.test.exception.CassandraTestException;
import org.unittested.cassandra.test.resource.Resource;

public class BasicDataSettings implements DataSettings {

    private String [] data;
    private CqlResourceLoader cqlResourceLoader;

    public BasicDataSettings() {
        this(ArrayUtils.EMPTY_STRING_ARRAY, new BasicCqlResourceLoader());
    }

    public BasicDataSettings(String[] data, CqlResourceLoader cqlResourceLoader) {
        this.data = data;
        this.cqlResourceLoader = cqlResourceLoader;
    }

    @Override
    public String[] getData() {
        return this.data;
    }

    @Override
    public void load(TestRuntime runtime) {
        runtime.getKeyspace().use();

        for (String cqlOrUrl : this.data) {

            Resource resource = Resource.fromCqlOrUrl(cqlOrUrl);

            try {
                this.cqlResourceLoader.loadCqlResource(runtime, resource);
            } catch(IOException e) {
                throw new CassandraTestException("Failed to load data from '%s'", cqlOrUrl, e);
            }
        }
    }
}

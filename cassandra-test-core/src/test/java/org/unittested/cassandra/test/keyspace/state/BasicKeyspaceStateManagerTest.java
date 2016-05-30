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

package org.unittested.cassandra.test.keyspace.state;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.Test;

public class BasicKeyspaceStateManagerTest {

    @Test
    public void track() throws Exception {
        // given
        KeyspaceStateManager manager = new BasicKeyspaceStateManager();
        UUID schemaVersion = UUID.randomUUID();
        int keyspaceSignature = 9999;

        // when
        manager.track(1, schemaVersion, keyspaceSignature);

        // then
        assertThat(manager.isTracked(1), is(true));
        assertThat(manager.hasKeyspaceCqlSignatureChanged(1, keyspaceSignature), is(false));
        assertThat(manager.hasKeyspaceCqlSignatureChanged(1, 44), is(true));
        assertThat(manager.hasClusterSchemaVersionChanged(1, schemaVersion), is(false));
        assertThat(manager.hasClusterSchemaVersionChanged(1, UUID.randomUUID()), is(true));
    }

    @Test
    public void notTracked() throws Exception {
        // given
        KeyspaceStateManager manager = new BasicKeyspaceStateManager();

        // when, then
        assertThat(manager.hasKeyspaceCqlSignatureChanged(1, 44), is(true));
        assertThat(manager.hasClusterSchemaVersionChanged(1, UUID.randomUUID()), is(true));
    }
}

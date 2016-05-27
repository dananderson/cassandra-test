package org.unittested.cassandra.test.keyspace.state;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BasicStateManagerTest {

    private static final UUID SCHEMA_VERSION_1 = UUID.randomUUID();
    private static final int KEYSPACE_SIGNATURE_1 = 200;

    private static final UUID SCHEMA_VERSION_2 = UUID.randomUUID();
    private static final int KEYSPACE_SIGNATURE_2 = 300;

    private KeyspaceStateManager keyspaceStateManager;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        this.keyspaceStateManager = new BasicKeyspaceStateManager();
        this.keyspaceStateManager.track(1, SCHEMA_VERSION_1, KEYSPACE_SIGNATURE_1);
        this.keyspaceStateManager.track(2, SCHEMA_VERSION_2, KEYSPACE_SIGNATURE_2);
    }

    @DataProvider
    public static Object[][] isTracked() {
        return new Object[][] {
                { 0, false },
                { 1, true },
                { 2, true },
        };
    }

    @Test(dataProvider = "isTracked")
    public void isTracked(int key, boolean expectedResult) throws Exception {
        // given
        // data provider

        // when
        boolean result = this.keyspaceStateManager.isTracked(key);

        // then
        assertThat(result, is(expectedResult));
    }

    @Test
    public void track() throws Exception {
        // given
        UUID schemaVersion = UUID.randomUUID();
        int keyspaceSignature = 9999;

        // when
        this.keyspaceStateManager.track(1, schemaVersion, keyspaceSignature);

        // then
        assertThat(this.keyspaceStateManager.isTracked(1), is(true));
        assertThat(this.keyspaceStateManager.hasKeyspaceCqlSignatureChanged(1, keyspaceSignature), is(false));
        assertThat(this.keyspaceStateManager.hasKeyspaceCqlSignatureChanged(1, 44), is(true));
        assertThat(this.keyspaceStateManager.hasClusterSchemaVersionChanged(1, schemaVersion), is(false));
        assertThat(this.keyspaceStateManager.hasClusterSchemaVersionChanged(1, UUID.randomUUID()), is(true));
    }
}

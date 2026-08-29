package io.cloink.client.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TUNParametersUnitTest {
    @Test
    public void detectsRouteChanges() {
        TUNParameters parameters = new TUNParameters(
                "100.80.55.181/16", "", 1280, "", "", "10.10.10.0/24");

        assertFalse(parameters.didRoutesChange("10.10.10.0/24"));
        assertTrue(parameters.didRoutesChange("10.10.10.0/24;192.168.124.0/24"));
        assertTrue(parameters.didRoutesChange(""));
    }

    @Test
    public void handlesInitialNullRouteSnapshot() {
        TUNParameters parameters = new TUNParameters(
                "100.80.55.181/16", "", 1280, "", "", null);

        assertFalse(parameters.didRoutesChange(null));
        assertTrue(parameters.didRoutesChange("10.10.10.0/24"));
    }
}

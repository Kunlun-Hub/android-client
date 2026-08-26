package io.cloink.client;

import org.junit.Assert;
import org.junit.Test;

public class UpdateCheckerTest {
    @Test
    public void detectsNewerSemanticVersions() {
        Assert.assertTrue(UpdateChecker.isNewer("0.77.2", "v0.77.1"));
        Assert.assertTrue(UpdateChecker.isNewer("1.0.0", "0.99.9"));
        Assert.assertFalse(UpdateChecker.isNewer("0.77.1", "v0.77.1"));
        Assert.assertFalse(UpdateChecker.isNewer("0.77.0", "0.77.1"));
        Assert.assertFalse(UpdateChecker.isNewer("0.77.2-beta", "0.77.2"));
    }
}

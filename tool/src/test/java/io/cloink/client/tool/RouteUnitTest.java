package io.cloink.client.tool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RouteUnitTest {
    @Test
    public void parsesIPv4AndIPv6Prefixes() throws Exception {
        Route ipv4 = new Route("10.10.10.0/24");
        assertEquals("10.10.10.0", ipv4.addr);
        assertEquals(24, ipv4.prefixLength);

        Route ipv6 = new Route("100::/64");
        assertEquals("100::", ipv6.addr);
        assertEquals(64, ipv6.prefixLength);
    }

    @Test
    public void duplicateRoutesAreCollapsedBeforeBuildingVpn() {
        var routes = IFace.toRoutes("10.10.10.0/24;10.10.10.0/24;192.168.124.0/24");

        assertEquals(2, routes.size());
        assertEquals("10.10.10.0", routes.get(0).addr);
        assertEquals("192.168.124.0", routes.get(1).addr);
    }
}

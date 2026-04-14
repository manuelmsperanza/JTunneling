package com.hoffnungland.jTunneling;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TunnelingMonitor}.
 */
class TunnelingMonitorTest {

    /**
     * Ensures that {@link TunnelingMonitor#closeAll()} iterates through all
     * registered tunnels and invokes disconnect on each one.
     */
    @Test
    void closeAllDisconnectsEveryTunnel() {
        TunnelingMonitor monitor = new TunnelingMonitor();
        TestPortForwarding firstTunnel = new TestPortForwarding();
        TestPortForwarding secondTunnel = new TestPortForwarding();

        monitor.getListTunnels().put("first", firstTunnel);
        monitor.getListTunnels().put("second", secondTunnel);

        monitor.closeAll();

        assertTrue(firstTunnel.disconnected, "Expected first tunnel to be disconnected");
        assertTrue(secondTunnel.disconnected, "Expected second tunnel to be disconnected");
    }

    /**
     * Test stub for tracking disconnect invocations.
     */
    private static final class TestPortForwarding extends PortForwarding {
        private boolean disconnected;

        @Override
        public void disconnect() {
            disconnected = true;
        }
    }
}

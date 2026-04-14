package com.hoffnungland.jTunneling;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Background monitor that periodically validates active SSH tunnels and can
 * close all tunnels when the application exits.
 */
public class TunnelingMonitor implements Runnable {

	private static final Logger logger = LogManager.getLogger(TunnelingMonitor.class);

	private JFrame winFrame;
	private ConcurrentMap<String, PortForwarding> listTunnels;

	/**
	 * Creates an empty monitor with no registered tunnels.
	 */
	public TunnelingMonitor() {
		this.listTunnels = new ConcurrentHashMap<String, PortForwarding>();
	}

	/**
	 * Returns the map of registered tunnels keyed by tunnel name.
	 *
	 * @return thread-safe map of tunnel names to forwarding instances
	 */
	public ConcurrentMap<String, PortForwarding> getListTunnels() {
		return listTunnels;
	}

	@Override
	public void run() {
		logger.traceEntry();
		try {
			while (true) {
				for (PortForwarding curTunnelling : this.listTunnels.values()) {
					curTunnelling.checkConnection();
				}
				Thread.sleep(2000);
			}

		} catch (InterruptedException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.winFrame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
			this.winFrame.dispose();
		}

		logger.traceExit();

	}

	/**
	 * Disconnects and removes all currently active tunnels from the monitor.
	 */
	public void closeAll() {
		logger.traceEntry();
		for (PortForwarding curTunnelling : this.listTunnels.values()) {
			curTunnelling.disconnect();
		}
		logger.traceExit();
	}

}

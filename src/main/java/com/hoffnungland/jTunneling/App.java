package com.hoffnungland.jTunneling;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.Session;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class App {

	private static final Logger logger = LogManager.getLogger(App.class);
	public Session session;
	public int keepAliveSleep = 1000;

	private JFrame frmTunneling;
	private JScrollPane scrollPane;
	private JPanel panel;
	
	private Thread tunnelingMonitorThread;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		logger.traceEntry();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App window = new App();
					window.frmTunneling.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		logger.traceExit();
	}

	/**
	 * Create the application.
	 */
	public App() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		logger.traceEntry();
		this.frmTunneling = new JFrame();
		this.frmTunneling.setTitle("Tunneling");
		this.frmTunneling.setBounds(100, 100, 450, 300);
		this.frmTunneling.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.scrollPane = new JScrollPane();
		this.frmTunneling.getContentPane().add(scrollPane, BorderLayout.CENTER);

		this.panel = new JPanel();
		this.scrollPane.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		/*gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};*/
		panel.setLayout(gbl_panel);
		
		int rowIdx = 0;
		TunnelingMonitor tunnelingMonitor = new TunnelingMonitor();
		this.tunnelingMonitorThread = new Thread(tunnelingMonitor);
		this.tunnelingMonitorThread.setDaemon(true);
		this.tunnelingMonitorThread.start();
		
		File tunnelPropertiesDir = new File("./etc/tunnels");
		for (File curPropFile : tunnelPropertiesDir.listFiles()) {
			if(curPropFile.isFile() && curPropFile.getName().endsWith(".properties")) {
				try (FileInputStream configFile = new FileInputStream(curPropFile)) {
					
					logger.debug("Loading " + curPropFile.getName());
					
					Properties tunnelsProperties = new Properties();
					tunnelsProperties.load(configFile);
					
					String tunnnelingName = curPropFile.getName().substring(0, curPropFile.getName().indexOf(".properties"));
					PortForwarding portForwarding = new PortForwarding(this.frmTunneling);
					
					portForwarding.init(tunnnelingName, tunnelsProperties, tunnelingMonitor);
					
					GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
					gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
					gbc_lblNewLabel.gridx = 0;
					gbc_lblNewLabel.gridy = rowIdx;
					panel.add(portForwarding.getTunnelLabel(), gbc_lblNewLabel);
					
					GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
					gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
					gbc_btnNewButton.gridx = 1;
					gbc_btnNewButton.gridy = rowIdx;
					panel.add(portForwarding.getStatusButton(), gbc_btnNewButton);
					
					rowIdx++;
					
				} catch (IOException e) {
					logger.error(e);
					JOptionPane.showMessageDialog(this.frmTunneling, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		logger.traceExit();
	}

}

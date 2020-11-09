package com.hoffnungland.jTunneling;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class PortForwarding implements ActionListener {

	private static final Logger logger = LogManager.getLogger(PortForwarding.class);
	private static JSch jsch = new JSch();

	private static final int emojiQuestionMark = 0x2753; //black question mark ornament
	private static final int emojiCheckMark = 0x2705; //white heavy check mark
	private static final int emojiCrossMark = 0x274C; //cross mark

	private JFrame winFrame;
	private Session session;
	private String host;
	private String user;
	private String password;
	private String[] lportArray;
	private String[] rhostArray;
	private String[] rportArray;
	private JLabel tunnelLabel;
	private JButton statusButton;
	
	private TunnelingMonitor tunnelingMonitor;
	private String name;

	public PortForwarding(JFrame winFrame) {
		super();
		this.winFrame = winFrame;
	}

	public JLabel getTunnelLabel() {
		return tunnelLabel;
	}

	public JButton getStatusButton() {
		return statusButton;
	}

	public void init(String name, Properties tunnelsProperties, TunnelingMonitor tunnelingMonitor) {
		logger.traceEntry();
		this.name = name;
		this.tunnelingMonitor = tunnelingMonitor; 
		this.host = tunnelsProperties.getProperty("host");
		this.user = tunnelsProperties.getProperty("user");
		this.password = tunnelsProperties.getProperty("password");

		this.lportArray = tunnelsProperties.getProperty("lport").split(",");
		this.rhostArray = tunnelsProperties.getProperty("rhost").split(",");
		this.rportArray = tunnelsProperties.getProperty("rport").split(",");

		this.tunnelLabel = new JLabel(this.name);
		this.statusButton = new JButton(new String(Character.toChars(PortForwarding.emojiQuestionMark)));
		this.statusButton.addActionListener(this);

		logger.traceExit();
	}

	public void checkConnection() {
		logger.traceEntry();
		if(this.session == null || !this.session.isConnected()) {
			this.disconnect();
		}
		logger.traceExit();
	}

	public void disconnect() {
		logger.traceEntry();
		try {
			this.statusButton.setEnabled(false);
			if(this.session != null) {
				String[] listFw = this.session.getPortForwardingL(); //lport:host:hostport
				if(listFw != null) {
					for(String fwPort : listFw) {
						logger.debug("Removing localhost: " + fwPort);
						int lport = Integer.parseInt(fwPort.split(":")[0]);
						this.session.delPortForwardingL(lport);
					}
				}
				if(this.session.isConnected()) {
					logger.info("Disconnect");
					this.session.disconnect();
				}
			}
			this.session = null;
			this.statusButton.setText(new String(Character.toChars(PortForwarding.emojiCrossMark)));
		} catch (JSchException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.winFrame, e.getMessage(), this.name + " exception", JOptionPane.ERROR_MESSAGE);
		} finally {
			this.tunnelingMonitor.getListTunnels().remove(this.name);
			this.statusButton.setEnabled(true);
		}
		logger.traceExit();
	}

	public void connect() {
		logger.traceEntry();
		try {
			
			this.statusButton.setEnabled(false);
			
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");

			this.session = PortForwarding.jsch.getSession(user, host, 22);

			this.session.setConfig(config);

			this.session.setPassword(password);
			logger.info("Connect");
			this.session.connect();
			this.session.setServerAliveInterval(1000);

			for(int fwIdx = 0; fwIdx < this.lportArray.length; fwIdx++) {	

				int lport = Integer.parseInt(this.lportArray[fwIdx]);
				String rhost = this.rhostArray[fwIdx];
				int rport = Integer.parseInt(this.rportArray[fwIdx]);

				int assinged_port = this.session.setPortForwardingL(lport, rhost, rport);
				logger.debug("localhost: " + assinged_port + " -> " + rhost + ":" + rport);
			}
			this.tunnelingMonitor.getListTunnels().put(this.name, this);
			this.statusButton.setText(new String(Character.toChars(PortForwarding.emojiCheckMark)));

		} catch (JSchException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.winFrame, e.getMessage(), this.name + " exception", JOptionPane.ERROR_MESSAGE);
		} finally {
			this.statusButton.setEnabled(true);
		}

		logger.traceExit();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		logger.traceEntry();
		
		new Thread((new Runnable() {
			public void run() {
				if(session == null) {
					connect();
				} else {
					disconnect();
				}
			}
			
		})).start();
		logger.traceExit();
	}

}

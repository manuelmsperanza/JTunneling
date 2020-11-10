package com.hoffnungland.jTunneling;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.RandomStringUtils;
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
	private KeyStore ks;
	private Session session;
	private String host;
	private String user;
	private String passwordType;
	private String password;
	private String[] lportArray;
	private String[] rhostArray;
	private String[] rportArray;
	private JLabel tunnelLabel;
	private JButton statusButton;
	
	private TunnelingMonitor tunnelingMonitor;
	private String name;

	public JLabel getTunnelLabel() {
		return tunnelLabel;
	}

	public JButton getStatusButton() {
		return statusButton;
	}

	public void init(String name, File propertyFile, App app, String passwordKs) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, CertificateException, IOException {
		logger.traceEntry();
		
		Properties tunnelsProperties = new Properties();
		try (FileInputStream configFile = new FileInputStream(propertyFile)) {
			tunnelsProperties.load(configFile);
		}
		
		this.name = name;
		this.ks = app.getKs();
		this.tunnelingMonitor = app.getTunnelingMonitor();
		this.winFrame = app.getFrmTunneling();
		
		this.host = tunnelsProperties.getProperty("host");
		this.user = tunnelsProperties.getProperty("user");
		this.passwordType = tunnelsProperties.getProperty("passwordType");
		
		if("encrypt".equals(this.passwordType)) {
			
			this.password = RandomStringUtils.randomAlphanumeric(12);
			this.passwordType = "encrypted";
			writePasswordToKeyStore(this.ks, app.getKeyStorePath(), passwordKs, this.password, this.name + ".password", tunnelsProperties.getProperty("password"));
			tunnelsProperties.setProperty("passwordType", this.passwordType);
			tunnelsProperties.setProperty("password", this.password);
			
			try(FileOutputStream out = new FileOutputStream(propertyFile)) {			
				tunnelsProperties.store(out, "---No Comment---");
			}
			
		} else {
			this.password = tunnelsProperties.getProperty("password");
		}

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
			
			this.session = PortForwarding.jsch.getSession(this.user, this.host, 22);
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			this.session.setConfig(config);
			
			switch(this.passwordType) {
			case "encrypted":
				
				this.session.setPassword(readPasswordFromKeyStore(this.ks, this.password, this.name + ".password"));
				break;
			case "oneTimePassword":
				PasswordPanel passwordPanel = new PasswordPanel();
				int option = JOptionPane.showOptionDialog(this.winFrame, passwordPanel, this.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				if(option == JOptionPane.OK_OPTION) { // pressing OK button
				    char[] passwd = passwordPanel.getPasswordField().getPassword();
				    this.session.setPassword(new String(passwd));
				} else {
					logger.traceExit();
					return;
				}
				break;
			}
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

		} catch (JSchException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidKeySpecException e) {
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
				if(session == null || !session.isConnected()) {
					connect();
				} else {
					disconnect();
				}
			}
			
		})).start();
		logger.traceExit();
	}
	
	private static String readPasswordFromKeyStore(KeyStore keyStore, String passwordPassword, String passwordAlias) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException{

        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(passwordPassword.toCharArray());

        KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry)keyStore.getEntry(passwordAlias, keyStorePP);
        if(ske == null) {
        	System.err.println("Password for " + passwordAlias + " does not exist");
        	return null;
        }
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        PBEKeySpec keySpec = (PBEKeySpec)factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);

        return new String(keySpec.getPassword());
    }
	
	private static void writePasswordToKeyStore(KeyStore keyStore, String keyStorePath, String keyStorePassword, String passwordPassword, String alias, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, CertificateException, IOException{

        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(passwordPassword.toCharArray());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret = factory.generateSecret(new PBEKeySpec(password.toCharArray(), RandomStringUtils.randomAlphanumeric(24).getBytes(), 13));

        keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePP);

        FileOutputStream outputStream = new FileOutputStream(new File(keyStorePath));
        keyStore.store(outputStream, keyStorePassword.toCharArray());
    }
	
}

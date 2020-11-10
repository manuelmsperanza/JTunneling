package com.hoffnungland.jTunneling;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class App {

	private static final Logger logger = LogManager.getLogger(App.class);
	private static String fileSeparator = System.getProperty("file.separator");
	
	public Session session;
	public int keepAliveSleep = 1000;

	private JFrame frmTunneling;
	private JScrollPane scrollPane;
	private JPanel panel;

	private KeyStore ks;
	private String keyStorePath;
	private Thread tunnelingMonitorThread;
	private TunnelingMonitor tunnelingMonitor;
	
	public JFrame getFrmTunneling() {
		return frmTunneling;
	}

	public KeyStore getKs() {
		return ks;
	}
	
	public String getKeyStorePath() {
		return keyStorePath;
	}

	public TunnelingMonitor getTunnelingMonitor() {
		return tunnelingMonitor;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		logger.traceEntry();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			logger.error(e);
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					String passwordKs = null;
					if(args.length > 0) {
						passwordKs = args[0];
					} else {
						PasswordPanel passwordPanel = new PasswordPanel();
						int option = JOptionPane.showOptionDialog(null, passwordPanel, "Vault password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
						if(option == JOptionPane.OK_OPTION) { // pressing OK button
							char[] passwd = passwordPanel.getPasswordField().getPassword();
							passwordKs = new String(passwd);
						} else {
							logger.traceExit();
							return;
						}
					}

					App window = new App(passwordKs);
					window.frmTunneling.setVisible(true);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
		logger.traceExit();
	}

	/**
	 * Create the application.
	 */
	public App(String passwordKs) {
		initialize(passwordKs);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String passwordKs) {
		logger.traceEntry();

		try {
			this.ks = KeyStore.getInstance("pkcs12");
			
			this.keyStorePath = System.getProperty("user.home") + fileSeparator + "OneDrive"+ fileSeparator + "JTunnelingKStore.jks";
			try (FileInputStream fis = new FileInputStream(keyStorePath)) {
				this.ks.load(fis, passwordKs.toCharArray());
			}
			
			this.frmTunneling = new JFrame();
			frmTunneling.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					tunnelingMonitor.closeAll();
				}
			});
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
			this.tunnelingMonitor = new TunnelingMonitor();
			this.tunnelingMonitorThread = new Thread(this.tunnelingMonitor);
			this.tunnelingMonitorThread.setDaemon(true);
			this.tunnelingMonitorThread.start();

			File tunnelPropertiesDir = new File("./etc/tunnels");
			for (File curPropFile : tunnelPropertiesDir.listFiles()) {
				if(curPropFile.isFile() && curPropFile.getName().endsWith(".properties")) {
					
					logger.debug("Loading " + curPropFile.getName());

					String tunnnelingName = curPropFile.getName().substring(0, curPropFile.getName().indexOf(".properties"));
					PortForwarding portForwarding = new PortForwarding();

					portForwarding.init(tunnnelingName, curPropFile, this, passwordKs);

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
				}
			}
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | InvalidKeySpecException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.frmTunneling, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
		}
		logger.traceExit();
	}

}

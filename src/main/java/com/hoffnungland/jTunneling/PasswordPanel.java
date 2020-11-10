package com.hoffnungland.jTunneling;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

public class PasswordPanel extends JPanel {
	private JPasswordField passwordField;

	/**
	 * Create the panel.
	 */
	public PasswordPanel() {
		
		JLabel passwdLabel = new JLabel("Enter a password:");
		add(passwdLabel);
		
		this.passwordField = new JPasswordField();
		this.passwordField.setColumns(20);
		add(this.passwordField);

	}

	public JPasswordField getPasswordField() {
		return passwordField;
	}
	
}

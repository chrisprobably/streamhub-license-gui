package com.streamhub;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

@SuppressWarnings("serial")
public class StreamHubLicenseGenerator extends JFrame {
	private static final String CRLF = "\r\n";
	private static final String DATE_FORMAT = "ddMMyyyy";
	private static final String USELESS_KEY = "str34mhub";
	private static final String DEFAULT_NUM_USERS = "60000";
	private static final String DEFAULT_CUSTOMERS_DIRECTORY = "C:\\";
	private static final int ROWS = 10;
	private final List<JPanel> macAddressPanels = new ArrayList<JPanel>();
	private JFileChooser folderChooser;

	public StreamHubLicenseGenerator() {
		super("StreamHub License Generator");
		this.setLayout(new GridLayout(0, 1));
		for (int i = 0; i < ROWS; i++) {
			JPanel row = createMacAddressRow();
			macAddressPanels.add(row);
			this.add(row);
		}
		JPanel actionsRow = createActionsRow();
		this.add(actionsRow);
		this.setSize(new Dimension(900, 500));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private JPanel createActionsRow() {
		JPanel actionsRow = new JPanel(new FlowLayout());
		File baseFolder = new File(DEFAULT_CUSTOMERS_DIRECTORY);
		JButton generate = new JButton("Generate");
		generate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JPanel row : macAddressPanels) {
					try {
						String macAddress = ((JTextField) row.getComponent(1)).getText();
						if (macAddress.length() > 0) {
							String startDate = ((JTextField) row.getComponent(3)).getText();
							String expiryDate = ((JTextField) row.getComponent(5)).getText();
							String edition = ((JComboBox) row.getComponent(6)).getSelectedItem().toString();
							macAddress = macAddress.replaceAll("-", ":").trim();
							String name = ((JTextField) row.getComponent(8)).getText().trim();
							String numUsers = ((JTextField) row.getComponent(10)).getText();
							String licenseString = startDate + "-" + expiryDate + "-" + numUsers + "-" + macAddress + "-" + edition + ":" + name;
							String hashInput = licenseString + USELESS_KEY;
							MessageDigest m = MessageDigest.getInstance("SHA-512");
							m.update(hashInput.getBytes(), 0, hashInput.length());
							String hash = "==" + new BigInteger(1, m.digest()).toString(16) + "==";
							StringBuilder licenseText = new StringBuilder();
							licenseText.append("--").append(licenseString).append("--");
							licenseText.append(CRLF);
							licenseText.append(hash);
							licenseText.append(CRLF);
							System.out.println(name + ":");
							System.out.println();
							System.out.println(licenseText);
							
							File licenseDir = new File(folderChooser.getSelectedFile(), name);
							if (!licenseDir.isDirectory() && !licenseDir.exists()) {
								licenseDir.mkdir();
							}
							File licenseFile = new File(licenseDir, "license.txt");
							System.out.println("writing to " + licenseFile.getAbsolutePath());
							FileUtils.writeStringToFile(licenseFile, licenseText.toString());
						}
					} catch (Exception exception) {
						System.out.println("Could not generate license");
						exception.printStackTrace();
					}
				}
			}
		});
		final JButton chooseFolder = new JButton("Choose Folder");
		final JTextField folderDisplay = new JTextField(baseFolder.getAbsolutePath());
		folderChooser = new JFileChooser(baseFolder);
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		folderChooser.setSelectedFile(baseFolder);
		chooseFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == chooseFolder) {
					int returnVal = folderChooser.showOpenDialog(StreamHubLicenseGenerator.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File folder = folderChooser.getSelectedFile();
						folderDisplay.setText(folder.getAbsolutePath());
					}
				}
			}
		});

		actionsRow.add(folderDisplay);
		actionsRow.add(chooseFolder);
		actionsRow.add(generate);
		return actionsRow;
	}

	private static JPanel createMacAddressRow() {
		JPanel macAddressPanel = new JPanel(new FlowLayout());

		Calendar yesterday = Calendar.getInstance();
		yesterday.setTime(new Date());
		yesterday.add(Calendar.DAY_OF_MONTH, -1);

		Calendar expiry = Calendar.getInstance();
		expiry.setTime(yesterday.getTime());
		expiry.add(Calendar.DAY_OF_MONTH, 60);

		JLabel macAddressLabel = new JLabel("MAC Address:");
		JTextField macAddress = new JTextField(17);
		JLabel startDateLabel = new JLabel("Start Date");
		final JFormattedTextField startDate = new JFormattedTextField(new SimpleDateFormat(DATE_FORMAT));
		startDate.setValue(yesterday.getTime());

		JLabel expiryDateLabel = new JLabel("Expiry Date");
		final JFormattedTextField expiryDate = new JFormattedTextField(new SimpleDateFormat(DATE_FORMAT));
		expiryDate.setValue(expiry.getTime());

		startDate.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				// Get the new date and change expiry to suit
				Date date = (Date) startDate.getValue();
				Calendar tempCal = Calendar.getInstance();
				tempCal.setTime(date);
				tempCal.add(Calendar.DAY_OF_MONTH, 60);
				expiryDate.setValue(tempCal.getTime());
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		JComboBox edition = new JComboBox(new String[] { "web", "enterprise" });
		JLabel nameLabel = new JLabel("Name:");
		JTextField name = new JTextField(15);
		
		JLabel numUsersLabel = new JLabel("Num. Users:");
		JTextField numUsers = new JTextField(DEFAULT_NUM_USERS);
		
		macAddressPanel.add(macAddressLabel);
		macAddressPanel.add(macAddress);
		macAddressPanel.add(startDateLabel);
		macAddressPanel.add(startDate);
		macAddressPanel.add(expiryDateLabel);
		macAddressPanel.add(expiryDate);
		macAddressPanel.add(edition);
		macAddressPanel.add(nameLabel);
		macAddressPanel.add(name);
		macAddressPanel.add(numUsersLabel);
		macAddressPanel.add(numUsers);
		return macAddressPanel;
	}

	public static void main(String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(true);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
				} catch (Exception e) {
					System.out.println("Substance Graphite failed to initialize");
				}
				StreamHubLicenseGenerator w = new StreamHubLicenseGenerator();
				w.setVisible(true);
			}
		});
	}
}

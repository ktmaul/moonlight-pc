package com.limelight.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.limelight.Limelight;
import com.limelight.binding.PlatformBinding;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.http.NvHTTP;
import com.limelight.settings.PreferencesManager;
import com.limelight.settings.PreferencesManager.Preferences;

/**
 * The main frame of Limelight that allows the user to specify the host and begin the stream.
 * @author Diego Waxemberg
 * <br>Cameron Gutman
 */
public class MainFrame {
	private JTextField hostField;
	private JButton pair;
	private JButton stream;
	private JFrame limeFrame;

	/**
	 * Gets the actual JFrame this class creates
	 * @return the JFrame that is the main frame
	 */
	public JFrame getLimeFrame() {
		return limeFrame;
	}

	/**
	 * Builds all components of the frame, including the frame itself and displays it to the user.
	 */
	public void build() {
		limeFrame = new JFrame("Limelight");
		limeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container mainPane = limeFrame.getContentPane();

		mainPane.setLayout(new BorderLayout());

		JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));

		Preferences prefs = PreferencesManager.getPreferences();

		hostField = new JTextField();
		hostField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		hostField.setToolTipText("Enter host name or IP address");
		hostField.setText(prefs.getHost());
		hostField.setSelectionStart(0);
		hostField.setSelectionEnd(hostField.getText().length());

		stream = new JButton("Start Streaming");
		stream.addActionListener(createStreamButtonListener());
		stream.setToolTipText("Start the GeForce stream");

		pair = new JButton("Pair");
		pair.addActionListener(createPairButtonListener());
		pair.setToolTipText("Send pair request to GeForce PC");

		Box streamBox = Box.createHorizontalBox();
		streamBox.add(Box.createHorizontalGlue());
		streamBox.add(stream);
		streamBox.add(Box.createHorizontalGlue());

		Box pairBox = Box.createHorizontalBox();
		pairBox.add(Box.createHorizontalGlue());
		pairBox.add(pair);
		pairBox.add(Box.createHorizontalGlue());

		Box hostBox = Box.createHorizontalBox();
		hostBox.add(Box.createHorizontalStrut(20));
		hostBox.add(hostField);
		hostBox.add(Box.createHorizontalStrut(20));


		Box contentBox = Box.createVerticalBox();
		contentBox.add(Box.createVerticalStrut(20));
		contentBox.add(hostBox);
		contentBox.add(Box.createVerticalStrut(5));
		contentBox.add(streamBox);
		contentBox.add(Box.createVerticalStrut(10));
		contentBox.add(pairBox);

		contentBox.add(Box.createVerticalGlue());

		centerPane.add(contentBox);
		mainPane.add(centerPane, "Center");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		limeFrame.setJMenuBar(createMenuBar());
		limeFrame.getRootPane().setDefaultButton(stream);
		limeFrame.setSize(300, 200);
		limeFrame.setLocation(dim.width/2-limeFrame.getSize().width/2, dim.height/2-limeFrame.getSize().height/2);
		limeFrame.setResizable(false);
		limeFrame.setVisible(true);
	}

	/*
	 * Creates the menu bar for the user to go to preferences, mappings, etc.
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu optionsMenu = new JMenu("Options");
		JMenuItem gamepadSettings = new JMenuItem("Gamepad Settings");
		JMenuItem generalSettings = new JMenuItem("Preferences");

		gamepadSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new GamepadConfigFrame().build();
			}
		});

		generalSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new PreferencesFrame().build();
			}
		});

		optionsMenu.add(gamepadSettings);
		optionsMenu.add(generalSettings);

		menuBar.add(optionsMenu);

		return menuBar;
	}

	/*
	 * Creates the listener for the stream button- starts the stream process
	 */
	private ActionListener createStreamButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                            String host = hostField.getText();
                            Preferences prefs = PreferencesManager.getPreferences();
                            if (!host.equals(prefs.getHost())) {
                                prefs.setHost(host);
                                PreferencesManager.writePreferences(prefs);
                            }
                            // Limelight.createInstance(host);
                            showApps();
                        }
                };
	}

	/*
	 * Creates the listener for the pair button- requests a pairing with the specified host
	 */
	private ActionListener createPairButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						Preferences prefs = PreferencesManager.getPreferences();
						
						// Save preferences to preserve possibly new unique ID
						PreferencesManager.writePreferences(prefs);
						
						String msg = Limelight.pair(prefs.getUniqueId(), hostField.getText());
						JOptionPane.showMessageDialog(limeFrame, msg, "Limelight", JOptionPane.INFORMATION_MESSAGE);
					}
				}).start();
			}
		};
	}
	
	private void showApps() {
                String host = hostField.getText();
                String macAddress = null;
                try {
                        macAddress = InetAddress.getByName(host).getHostAddress();
                } catch (UnknownHostException e) {
                        e.printStackTrace();
                }


                if (macAddress == null) {
                        System.out.println("Couldn't find a MAC address");
                        return;
                }

                NvHTTP httpConn;
                try {
                        httpConn = new NvHTTP(InetAddress.getByName(host),
                                              macAddress, PlatformBinding.getDeviceName(), PlatformBinding.getCryptoProvider());
                        AppsFrame appsFrame = new AppsFrame(httpConn, host);
                        appsFrame.build();
                } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                }
        }
}
/*
 * Twitch MCBot - Posts which servers you are in, lobbies and mini-games.
 * Copyright (C) 2015  Sam Murphy
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.bitbucket.master_mas.twitchBotMC;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.pircbotx.PircBotX;

public class Launcher extends JFrame {

	private static final long serialVersionUID = -6830326952068691403L;
	private static Dimension programRes = new Dimension(400, 330);
	private JLabel connectionStatus;
	private JLabel otherStatus;
	private PircBotX bot;
	private Timer timer;
	
	private Thread botThread;
	private Thread checkerThread;
	
	private Launcher instance;
	
	public static void main(String[] args) {
		new Launcher();
	}
	
	public Launcher() {
		this.instance = this;
		timer = new Timer();
		
		this.setSize(programRes);
		this.setLocationRelativeTo(null);
		this.setTitle("Twitch MC Bot - 1.0.3");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.setIconImage(null);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				String buttons[] = {"Yes", "No"};
				int result = JOptionPane.showOptionDialog(
						null, "Are you sure you want to exit?", 
						instance.getTitle(), 
						JOptionPane.DEFAULT_OPTION, 
						JOptionPane.WARNING_MESSAGE, 
						null, buttons, buttons[1]);
				if(result == 0)
					System.exit(0);
			}
		});
		
		this.add(buildInterface(read()));
		
		this.setVisible(true);
	}

	private JPanel buildInterface(Map<String, String> settings) {
		final JPanel container = new JPanel();
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		container.setLayout(new GridLayout(12, 2, 5, 5));
		
		JLabel userNameLabel = new JLabel("Username of bot");
		container.add(userNameLabel);
		
		final JTextField username = new JTextField();
		username.setToolTipText("Username of the Account to Post");
		if(settings != null)
			username.setText(settings.get("username"));
		container.add(username);
		
		JLabel oAuthKeyLabel = new JLabel("OAuth Key of Bot");
		container.add(oAuthKeyLabel);
		
		final JPasswordField oauthKey = new JPasswordField();
		oauthKey.setToolTipText("Get a OAuth Key at http://twitchapps.com/tmi/");
		if(settings != null)
			oauthKey.setText(settings.get("oauth"));
		container.add(oauthKey);
		
		JLabel channelLabel = new JLabel("Username of Twitch Channel to post on");
		container.add(channelLabel);
		
		final JTextField channel = new JTextField();
		channel.setToolTipText("Username of Channel to Post On");
		if(settings != null)
			channel.setText(settings.get("channel"));
		container.add(channel);
		
		final JLabel saveSettingsLabel = new JLabel("Save Configuration");
		container.add(saveSettingsLabel);
		
		final JCheckBox saveSettings = new JCheckBox();
		saveSettings.setSelected(true);
		container.add(saveSettings);
		
		JButton confirm = new JButton();
		confirm.setText("Start Bot");
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MinecraftCurrentInfo.castersChannel = username.getText();
				botThread = new Thread(new BotHandler(username.getText(), new String(oauthKey.getPassword()), channel.getText(), saveSettings.isSelected(), instance), "IRC Connection");
				botThread.start();
				checkerThread = new Thread(new BotConnectionChecker(instance), "Checker");
				checkerThread.start();
				new MinecraftPoller(instance);
				new Thread(new BotMessagePoster(instance)).start();
				
				container.remove((JButton)e.getSource());
				container.remove(saveSettings);
				container.remove(saveSettingsLabel);
				username.setEditable(false);
				oauthKey.setEditable(false);
				channel.setEditable(false);
				programRes.height = 300;
				instance.setSize(programRes);
				
				container.setLayout(new GridLayout(9, 2, 5, 5));
			}
		});
		container.add(confirm);
		
		connectionStatus = new JLabel("", SwingConstants.CENTER);
		changeConnectionStatusLabel("Bot not Started", "gray");
		container.add(connectionStatus);
		
		otherStatus = new JLabel("", SwingConstants.CENTER);
		changeStatusLabel("Nothing to Report", "gray");
		container.add(otherStatus);
		
		JLabel me = new JLabel("<html><font color='#bdbdbd'>Developed by Sam Murphy Independent Software Development</font></html>", SwingConstants.CENTER);
		me.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if(Desktop.isDesktopSupported())
				{
					try {
						Desktop.getDesktop().browse(new URI("http://www.sammurphysoftware.com"));
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		container.add(me);
		
		return container;
	}
	
	private Map<String, String> read() {
		String location = System.getenv("APPDATA") + "\\twitch\\bots\\mc\\config.properties";
		File file = new File(location);
		if(!file.exists())
			return null;
		
		Properties properties = new Properties();
		Map<String, String> data = new HashMap<String, String>();
		try {
			properties.load(new FileReader(file));
			data.put("username", properties.getProperty("username"));
			data.put("oauth", properties.getProperty("oauth"));
			data.put("channel", properties.getProperty("channel"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	private TimerTask currentStatusTimer = null;
	public void changeStatusLabel(String status, String color) {
		otherStatus.setText("<html><font color='" + color + "'>" + status + "</font></html>");
		
		if(currentStatusTimer != null)
			currentStatusTimer.cancel();
		
		timer.schedule(currentStatusTimer = new TimerTask() {
			@Override
			public void run() {
				instance.changeStatusLabel("Nothing to Report", "gray");
				currentStatusTimer = null;
			}
		}, 1000 * 30);
	}
	
	public void changeConnectionStatusLabel(String status, String color) {
		connectionStatus.setText("<html><font color='" + color + "'>" + status + "</font></html>");
	}

	public void setBot(PircBotX pircBotX) {
		this.bot = pircBotX;
	}

	public PircBotX getBot() {
		return bot;
	}
	
	public void startCheckerThread() {
		checkerThread.start();
	}
	
	public Timer getTimer() {
		return timer;
	}
}

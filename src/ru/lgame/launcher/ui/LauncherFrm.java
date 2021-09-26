package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.utils.logging.Log;
import ru.lgame.launcher.utils.ui.StackLayout;
import java.awt.Color;

/**
 * Окно лаунчера
 * @author Shinovon
 */
public class LauncherFrm extends JFrame implements WindowListener {

	private static final long serialVersionUID = 5754803808367340968L;
	
	private JPanel contentPane;
	LauncherPane mainPanel;
	StackLayout layout;
	boolean settingsShowing;
	JPanel settingsPanel;

	ActionListener settingsListener;

	private Thread repaintThread;

	/**
	 * Создает окно
	 */
	public LauncherFrm() {
		setTitle(Launcher.getFrmTitle());
		setUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(35, 36, 40));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(layout = new StackLayout());
		setContentPane(contentPane);
		addWindowListener(this);
		repaintThread = new Thread() {
			public void run() {
				try {
					while(Launcher.running) {
						repaint();
						Thread.sleep(1000);
						Thread.yield();
					}
				} catch (Exception e) {
				}
			}
		};
		repaintThread.start();

		settingsListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settingsShowing = !settingsShowing;
				layout.showComponent(settingsShowing ? settingsPanel : mainPanel, LauncherFrm.this);
			}
		};
		
		
		settingsPanel = new SettingsPane(this);
		contentPane.add(settingsPanel, BorderLayout.CENTER);

		mainPanel = new LauncherPane(this);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
		layout.showComponent(mainPanel, this);
		contentPane.setPreferredSize(new Dimension(1000, 600));
		
		pack();
		setLocationRelativeTo(null);
		
	}

	/**
	 * Установить LAF на системный
	 */
	private static void setUI() {
		try {
			String x = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(x);
		} catch (Exception e1) {
			Log.error("setUI()", e1);
		}
		Color bg = new Color(35, 36, 40);
		Color fg = new Color(255, 255, 255);
		UIManager.put("Panel.background", new ColorUIResource(bg));
		UIManager.put("Button.background", new ColorUIResource(bg));
		UIManager.put("CheckBox.background", new ColorUIResource(bg));
		UIManager.put("CheckBox.foreground", new ColorUIResource(fg));
		UIManager.put("Panel.foreground", new ColorUIResource(fg));
		UIManager.put("Label.foreground", new ColorUIResource(fg));
	}

	public LauncherPane mainPane() {
		return mainPanel;
	}

	public void updateAuth() {
		mainPanel.updateAuth();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Launcher.running = false;
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

}

package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.utils.logging.Log;
import ru.lgame.launcher.utils.ui.StackLayout;

/**
 * Окно лаунчера
 * @author Shinovon
 */
public class LauncherFrm extends JFrame {

	private static final long serialVersionUID = 5754803808367340968L;
	
	private JPanel contentPane;
	LauncherPane mainPanel;
	StackLayout layout;
	boolean settingsShowing;
	JPanel settingsPanel;

	ActionListener settingsListener;

	/**
	 * Создает окно
	 */
	public LauncherFrm() {
		setTitle(Launcher.getFrmTitle());
		setUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(layout = new StackLayout());
		setContentPane(contentPane);

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

	}

	public LauncherPane mainPane() {
		return mainPanel;
	}

	public void updateAuth() {
		mainPanel.updateAuth();
	}

}

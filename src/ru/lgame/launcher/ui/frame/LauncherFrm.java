package ru.lgame.launcher.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.ui.Fonts;
import ru.lgame.launcher.ui.pane.LauncherPane;
import ru.lgame.launcher.ui.pane.SettingsPane;
import ru.lgame.launcher.ui.swing.ButtonUI;
import ru.lgame.launcher.utils.logging.Log;
import ru.lgame.launcher.utils.ui.StackLayout;
import java.awt.FlowLayout;

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

	public ActionListener settingsListener;

	private Thread repaintThread;

	/**
	 * Создает окно
	 */
	public LauncherFrm() {
		setTitle(Launcher.getFrmTitle());
		setUI(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		FlowLayout flowLayout = (FlowLayout) contentPane.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		if(!Config.getBoolean("legacyLook")) contentPane.setBackground(new Color(35, 36, 40));
		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(layout = new StackLayout());
		setContentPane(contentPane);
		addWindowListener(this);
		repaintThread = new Thread("UI repaint thread") {
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
		setMinimumSize(new Dimension(900, 580));
		
		pack();
		setLocationRelativeTo(null);
		
	}

	/**
	 * Установить LAF на системный
	 */
	private static void setUI(JFrame frm) {
		try {
			String x = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(x);
		} catch (Exception e1) {
			Log.error("setUI()", e1);
		}
		
		Color bg = new Color(35, 36, 40);
		Color dbg = new Color(51, 52, 56);
		Color fg = new Color(255, 255, 255);
		if(!Config.getBoolean("legacyLook")) {
			UIManager.put("ButtonUI", ButtonUI.class.getName());
			if(Fonts.label != null) {
				UIManager.put("Label.font", Fonts.label);
				UIManager.put("CheckBox.font", Fonts.label);
				UIManager.put("Slider.font", Fonts.label);
				UIManager.put("Panel.font", Fonts.label);
			}
			UIManager.put("Panel.background", new ColorUIResource(bg));
			UIManager.put("Button.background", new ColorUIResource(bg));
			UIManager.put("CheckBox.background", new ColorUIResource(bg));
			UIManager.put("OptionPane.background", new ColorUIResource(bg));
			UIManager.put("Slider.background", new ColorUIResource(bg));
			UIManager.put("TextField.background", new ColorUIResource(bg));
			UIManager.put("List.background", new ColorUIResource(bg));
			//UIManager.put("ComboBox.background", new ColorUIResource(bg));
			UIManager.put("PasswordField.background", new ColorUIResource(bg));
			
			UIManager.put("TextField.disabledBackground", new ColorUIResource(dbg));
			UIManager.put("PasswordField.disabledBackground", new ColorUIResource(dbg));
			//UIManager.put("ComboBox.disabledBackground", new ColorUIResource(dbg));
			
			UIManager.put("Panel.foreground", new ColorUIResource(fg));
			UIManager.put("Label.foreground", new ColorUIResource(fg));
			UIManager.put("OptionPane.foreground", new ColorUIResource(fg));
			UIManager.put("OptionPane.messageForeground", new ColorUIResource(fg));
			UIManager.put("Slider.foreground", new ColorUIResource(fg));
			UIManager.put("TextField.foreground", new ColorUIResource(fg));
			UIManager.put("List.foreground", new ColorUIResource(fg));
			UIManager.put("PasswordField.foreground", new ColorUIResource(fg));
			UIManager.put("CheckBox.foreground", new ColorUIResource(fg));
			//UIManager.put("ComboBox.foreground", new ColorUIResource(fg));
			//UIManager.put("ComboBox.selectionForeground", new ColorUIResource(fg));
			UIManager.put("Button.foreground", new ColorUIResource(new Color(255, 255, 255)));
			UIManager.put("Button.disabledText", new ColorUIResource(new Color(225, 25, 255)));
			UIManager.put("Button.disabledText", new ColorUIResource(new Color(225, 25, 255)));
		}
		Launcher.setFrameIcon(frm);
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

	public void start() {
		mainPanel.start();
	}

}

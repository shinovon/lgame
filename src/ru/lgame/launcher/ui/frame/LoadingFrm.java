package ru.lgame.launcher.ui.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.ui.Fonts;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Окно загрузки
 * @author Shinovon
 */
public class LoadingFrm extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;

	private JLabel label;

	//private JProgressBar progressBar;

	public LoadingFrm() {
		setUndecorated(true);
		super.setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Launcher.setFrameIcon(this);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(35, 36, 40));
		contentPane.setPreferredSize(new Dimension(399, 133));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//progressBar = new JProgressBar();
		//progressBar.setBackground(new Color(35, 36, 40));
		//progressBar.setIndeterminate(true);
		//contentPane.add(progressBar, BorderLayout.CENTER);
		
		label = new JLabel(" ");
		label.setFont(Fonts.loading);
		label.setForeground(Color.WHITE);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(0, 100, 399, 16);
		contentPane.add(label);

		JLabel img = new JLabel();
		File f = new File(Launcher.getLauncherDir() + "bg.png");
		if(f.exists()) {
			img.setIcon(new ImageIcon(f.getAbsolutePath()));
		} else {
			img.setIcon(new ImageIcon(LoadingFrm.class.getResource("/launch133px.png")));
		}
		img.setBounds(0, 0, 399, 133);
		contentPane.add(img);
		pack();
		setLocationRelativeTo(null);
	}
	
	public void disableExitOnClose() {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	public void setText(String s) {
		label.setText(s);
	}

}

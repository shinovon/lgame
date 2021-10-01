package ru.lgame.launcher.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.ui.Fonts;

import javax.swing.JProgressBar;
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

	private JProgressBar progressBar;

	public LoadingFrm() {
		super.setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Launcher.setFrameIcon(this);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(35, 36, 40));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		progressBar = new JProgressBar();
		//progressBar.setBackground(new Color(35, 36, 40));
		progressBar.setIndeterminate(true);
		contentPane.add(progressBar, BorderLayout.CENTER);
		
		label = new JLabel(" ");
		label.setFont(Fonts.loading);
		label.setForeground(Color.WHITE);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(label, BorderLayout.SOUTH);
		
		contentPane.setPreferredSize(new Dimension(200, 50));
		pack();
		setLocationRelativeTo(null);
	}
	
	public void setText(String s) {
		label.setText(s);
	}

}

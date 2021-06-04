package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LoadingFrm extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;

	private JLabel label;

	/**
	 * Create the frame.
	 */
	public LoadingFrm() {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		contentPane.add(progressBar, BorderLayout.CENTER);
		
		label = new JLabel(" ");
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

package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.utils.logging.Log;

public class ErrorUI {

	@SuppressWarnings("deprecation")
	public static void clientError(String title, String s, String trace) {
		Launcher l = Launcher.inst;
		JDialog dialog = new JDialog(l.frame(), title, true);
		dialog.setAlwaysOnTop(true);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel pane = new JPanel();
        contentPane.add(pane, BorderLayout.CENTER);
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		pane.setLayout(new BorderLayout());
		if(s != null && s.length() > 0) {
			JTextPane infopane = new JTextPane();
			infopane.setBackground(SystemColor.menu);
			infopane.setEditable(false);
			infopane.setText(s);
			pane.add(infopane, BorderLayout.NORTH);
		}
		if(trace != null) {
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(600, 200));
			pane.add(scrollPane, BorderLayout.CENTER);
			JTextArea textArea = new JTextArea();
			textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
			textArea.setEditable(false);
			textArea.setText(trace);
			scrollPane.setViewportView(textArea);
		}
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPane, BorderLayout.SOUTH);
		{
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			dialog.getRootPane().setDefaultButton(okButton);
			JButton logsButton = new JButton("Показать логи");
			logsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					l.showLoggerFrame();
					dialog.dispose();
				}
			});
			logsButton.setActionCommand("OK");
			buttonPane.add(logsButton);
		}
		dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(l.frame());
        dialog.show();
        dialog.dispose();
	}

	@SuppressWarnings("deprecation")
	public static void showError(String title, String text, String trace) {
		Launcher l = Launcher.inst;
		Log.error("showError(): " + trace);
		JDialog dialog = new JDialog(l.frame(), title, true);
		dialog.setAlwaysOnTop(true);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel pane = new JPanel();
        contentPane.add(pane, BorderLayout.CENTER);
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		pane.setLayout(new BorderLayout());
		if(text != null && text.length() > 0) {
			JTextPane infopane = new JTextPane();
			infopane.setBackground(SystemColor.menu);
			infopane.setEditable(false);
			infopane.setText(text);
			pane.add(infopane, BorderLayout.NORTH);
		}
		if(trace != null) {
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(600, 200));
			pane.add(scrollPane, BorderLayout.CENTER);
			JTextArea textArea = new JTextArea();
			textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
			textArea.setEditable(false);
			textArea.setText(trace);
			scrollPane.setViewportView(textArea);
		}
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPane, BorderLayout.SOUTH);
		{
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			dialog.getRootPane().setDefaultButton(okButton);
		}
		dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(l.frame());
        dialog.show();
        dialog.dispose();
	}

	public static void showError(String title, String text) {
		showError(title, text, (String) null);
	}
	
	public static void showError(String title, String text, Throwable e) {
		showError(title, text, Log.exceptionToString(e));
	}

}

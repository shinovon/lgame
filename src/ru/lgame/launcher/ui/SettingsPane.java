package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class SettingsPane extends JPanel {

	private static final long serialVersionUID = -8711782187461079094L;

	/**
	 * Create the panel.
	 */
	public SettingsPane(LauncherFrm frm) {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		add(panel, BorderLayout.SOUTH);
		
		JButton settsBackBtn = new JButton("Назад");
		settsBackBtn.addActionListener(frm.settingsListener);
		panel.add(settsBackBtn);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(panel_1, BorderLayout.CENTER);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Launcher.inst.showLoggerFrame();
			}
		});
		panel_1.add(btnNewButton);
		
		JSpinner spinner = new JSpinner();
		spinner.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Config.set("xmx", (Integer)spinner.getValue());
				Config.saveConfig();
			}
		});
		spinner.setModel(new SpinnerNumberModel(Config.getInt("xmx"), 512, 16384, 512));
		panel_1.add(spinner);
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_4.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		add(panel_4, BorderLayout.NORTH);
		
		JLabel label = new JLabel("Настройки");
		panel_4.add(label);
		setPreferredSize(new Dimension(1000, 600));
	}

}

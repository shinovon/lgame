package ru.lgame.launcher.ui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import li.flor.nativejfilechooser.NativeJFileChooser;
import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.locale.Text;
import ru.lgame.launcher.ui.frame.LauncherFrm;

/**
 * @author Shinovon
 */
public class SettingsPane extends JPanel {

	private static final long serialVersionUID = -8711782187461079094L;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Create the panel.
	 */
	public SettingsPane(LauncherFrm frm) {
		/*
		long max1 = Runtime.getRuntime().maxMemory();
		if(max1 == Long.MAX_VALUE) max1 = 16384 * 1024 * 1024;
		int max = (int) (max1 / 1024L / 1024L);
		Log.debug("jvm max memory ret: " + max + "m");
		if(max < 5120) max = 5120;
		*/
		// Максимальное количество выделяемой памяти (MB)
		int max = 16384;
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.RIGHT);
		add(panel, BorderLayout.SOUTH);
		
		JButton openDirBtn = new JButton(Text.get("button.openlibrarydir"));
		openDirBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec("explorer \""+textField_1.getText()+"\"");
				} catch (IOException e1) {
				}
			}
		});
		panel.add(openDirBtn);
		
		JButton btnNewButton = new JButton(Text.get("button.showlogger"));
		panel.add(btnNewButton);
		
		JButton settsBackBtn = new JButton(Text.get("button.back"));
		settsBackBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.set("javapath", textField.getText());
				Config.set("path", textField_1.getText());
				Config.saveLater();
			}
		});
		settsBackBtn.addActionListener(frm.settingsListener);
		panel.add(settsBackBtn);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Launcher.inst.showLoggerFrame();
			}
		});

		JPanel pane = new JPanel();
		add(pane, BorderLayout.CENTER);
		pane.setLayout(null);
		JPanel content = new JPanel();
		pane.add(content);
		addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle r = content.getBounds();
				content.setBounds((pane.getWidth() - r.width) / 2, (pane.getHeight() - r.height) / 2, r.width, r.height);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				Rectangle r = content.getBounds();
				content.setBounds((pane.getWidth() - r.width) / 2, (pane.getHeight() - r.height) / 2, r.width, r.height);
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
			
		});
		content.setBounds(187, 69, 610, 400);
		Rectangle r = content.getBounds();
		if(true && true) {
			content.setBounds((pane.getWidth() - r.width) / 2, (pane.getHeight() - r.height) / 2, r.width, r.height);
		}
		content.setLayout(new GridBagLayout());
		GridBagConstraints gbc = gbc();
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		content.add(panel_2, gbc);
		
		JPanel panel_9 = new JPanel();
		panel_9.setPreferredSize(new Dimension(610, 32));
		panel_2.add(panel_9);
		JSlider slider = new JSlider();
		panel_9.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_11 = new JPanel();
		FlowLayout flowLayout_7 = (FlowLayout) panel_11.getLayout();
		flowLayout_7.setHgap(0);
		flowLayout_7.setAlignment(FlowLayout.RIGHT);
		panel_9.add(panel_11);
		
		JSpinner spinner = new JSpinner();
		panel_11.add(spinner);
		spinner.setModel(new SpinnerNumberModel(Config.getInt("xmx"), 512, max, 512));
		
		JPanel panel_13 = new JPanel();
		panel_11.add(panel_13);
		
		JPanel panel_12 = new JPanel();
		FlowLayout flowLayout_6 = (FlowLayout) panel_12.getLayout();
		flowLayout_6.setVgap(10);
		panel_9.add(panel_12, BorderLayout.WEST);
		
		JLabel lblNewLabel = new JLabel(Text.get("label.memory") + ": ");
		panel_12.add(lblNewLabel);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if((Integer)spinner.getValue() == 0) return;
				slider.setValue((Integer)spinner.getValue());
				Config.set("xmx", (Integer)spinner.getValue());
				Config.saveLater();
			}
		});

		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_7.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		content.add(panel_7, gbc.clone());

		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(256);
		slider.setMajorTickSpacing(512);
		slider.setPreferredSize(new Dimension(600, 36));
		slider.setMinimum(512);
		slider.setMaximum(max);
		slider.setValue(Config.getInt("xmx"));
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(slider.getValue() == 0) return;
				spinner.setValue(slider.getValue());
				Config.set("xmx", slider.getValue());
				Config.saveLater();
			}
		});
		panel_7.add(slider);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		content.add(panel_3, gbc.clone());
		
		JLabel lblJava = new JLabel(Text.get("label.javapath") + ": ");
		panel_3.add(lblJava);
		
		textField = new JTextField();
		textField.addInputMethodListener(new InputMethodListener() {
			public void caretPositionChanged(InputMethodEvent arg0) {
			}
			public void inputMethodTextChanged(InputMethodEvent arg0) {
				Config.set("javapath", textField.getText());
				Config.saveLater();
			}
		});
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.set("javapath", textField.getText());
				Config.saveLater();
			}
		});
		
		JPanel panel_8 = new JPanel();
		panel_8.setPreferredSize(new Dimension(143, 10));
		panel_3.add(panel_8);
		textField.setText(Config.get("javapath"));
		panel_3.add(textField);
		textField.setColumns(40);
		
		JButton btnNewButton_1 = new JButton("...");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String j = Config.get("javapath");
				if(j == null || j == "") {
					j = System.getProperty("java.home");
				}
				JFileChooser c = new NativeJFileChooser(j);
				c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(c.showOpenDialog(SettingsPane.this) == JFileChooser.APPROVE_OPTION) {
					File f = c.getSelectedFile();
					if(f != null) {
						String s = f.toString();
						Config.set("javapath", s);
						textField.setText(s);
					}
				}
			}
		});
		panel_3.add(btnNewButton_1);
		JPanel panel_l3 = new JPanel();
		panel_l3.setLayout(new FlowLayout(FlowLayout.RIGHT));
		content.add(panel_l3, gbc.clone());
		JLabel lblNewLabel_1 = new JLabel(Text.get("label.javapathnote"));
		panel_l3.add(lblNewLabel_1);
		JPanel pl3 = new JPanel();
		pl3.setPreferredSize(new Dimension(50, 10));
		panel_l3.add(pl3);

		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_5.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		content.add(panel_5, gbc.clone());
		
		JLabel label_1 = new JLabel(Text.get("label.savedir") + ": ");
		panel_5.add(label_1);
		
		textField_1 = new JTextField();
		textField_1.addInputMethodListener(new InputMethodListener() {
			public void caretPositionChanged(InputMethodEvent event) {
			}
			public void inputMethodTextChanged(InputMethodEvent event) {
				Config.set("path", textField_1.getText());
				Launcher.inst.pathChanged();
				Config.saveLater();
			}
		});
		textField_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Config.set("path", textField_1.getText());
				Launcher.inst.pathChanged();
				Config.saveLater();
			}
		});
		
		JPanel panel_10 = new JPanel();
		panel_10.setPreferredSize(new Dimension(50, 10));
		panel_5.add(panel_10);
		textField_1.setText(Config.get("path"));
		textField_1.setColumns(40);
		panel_5.add(textField_1);
		
		JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new NativeJFileChooser(Config.get("path"));
				c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(c.showOpenDialog(SettingsPane.this) == JFileChooser.APPROVE_OPTION) {
					File f = c.getSelectedFile();
					if(f != null) {
						String s = f.toString();
						Config.set("path", s);
						Launcher.inst.pathChanged();
						textField_1.setText(s);
					}
				}
			}
		});
		panel_5.add(button);
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_4.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		add(panel_4, BorderLayout.NORTH);
		
		JLabel label = new JLabel(Text.get("label.settings"));
		panel_4.add(label);
		setPreferredSize(new Dimension(1000, 600));
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.WEST);
		
		JPanel panel_6 = new JPanel();
		add(panel_6, BorderLayout.EAST);
	}

	private GridBagConstraints gbc() {
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}
}

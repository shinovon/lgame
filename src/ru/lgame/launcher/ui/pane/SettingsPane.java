package ru.lgame.launcher.ui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;

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
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		add(panel, BorderLayout.SOUTH);
		
		JButton settsBackBtn = new JButton(Text.get("button.back", "Назад"));
		settsBackBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.set("javapath", textField.getText());
				Config.set("path", textField_1.getText());
				Config.saveLater();
			}
		});
		settsBackBtn.addActionListener(frm.settingsListener);
		panel.add(settsBackBtn);
		
		JButton btnNewButton = new JButton("logger");
		panel.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Launcher.inst.showLoggerFrame();
			}
		});
		
		JPanel content = new JPanel();
		add(content, BorderLayout.CENTER);
		content.setLayout(new GridBagLayout());
		GridBagConstraints gbc = gbc();
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		content.add(panel_2, gbc);
		
		JLabel lblNewLabel = new JLabel(Text.get("label.memory", "Выделяемая память (Мегабайты)") + ": ");
		panel_2.add(lblNewLabel);
		
		JSpinner spinner = new JSpinner();
		panel_2.add(spinner);
		JSlider slider = new JSlider();
		spinner.setModel(new SpinnerNumberModel(Config.getInt("xmx"), 512, max, 512));
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
		
		JLabel lblJava = new JLabel(Text.get("label.javapath", "Путь к Java") + ": ");
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
		
		JLabel lblNewLabel_1 = new JLabel("(оставьте пустым для автоматического определения)");
		panel_3.add(lblNewLabel_1);

		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_5.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		content.add(panel_5, gbc.clone());
		
		JLabel label_1 = new JLabel(Text.get("label.savedir", "Папка для сохранения сборок") + ": ");
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
		
		JLabel label = new JLabel(Text.get("label.settings", "Настройки"));
		panel_4.add(label);
		setPreferredSize(new Dimension(1000, 600));
	}

	private GridBagConstraints gbc() {
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}

}

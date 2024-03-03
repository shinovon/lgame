package ru.lgame.launcher.ui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import li.flor.nativejfilechooser.NativeJFileChooser;
import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.ui.frame.LauncherFrm;
import ru.lgame.launcher.ui.locale.Text;

/**
 * @author Shinovon
 */
public class SettingsPane extends JPanel {

	private static final long serialVersionUID = -8711782187461079094L;
	private JTextField javaPathField;
	private JTextField libraryPathField;

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
		
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.RIGHT);
		add(panel, BorderLayout.SOUTH);
		
		JButton openDirBtn = new JButton(Text.get("button.openlibrarydir"));
		openDirBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec("explorer \""+libraryPathField.getText()+"\"");
				} catch (IOException e1) {
				}
			}
		});
		
		JButton showTeamBtn = new JButton("Обо мне");
		showTeamBtn.setVisible(false);
		panel.add(showTeamBtn);
		//showTeamBtn.setVisible(false);
		showTeamBtn.addActionListener(new ActionListener() {

			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				
				
				final String[] c = new String[] {
						"LGame Launcher",
						"",
						"Люди которые делали его:",
						" Арман Д. (@shinovon)",
						"  Лид разработчик, Дизайн, Идея",
						" Александр К. (@h4x4d)",
						"  Дизайн, Тестирование",
						"Тестеры:",
						" Иван Ж. (@algorithmlx)",
						" Remuru T. (@slime79)",
						" Тимур С. (@timursagitov)",
						" Максим Б. (@barahtanovm)",
						"",
						"Донаты на сервер",
						"  donationalerts.com/r/rehdzi",
						"",
						"Связаться с разработчиком",
						"  t.me/shinovon",
						"  vk.com/shinovon",
				};
				String s = "";
				for(String x: c) {
					s += x + "\n";
				}
				Launcher l = Launcher.inst;
				//if(lgameFrame == null)
				//	lgameFrame = new LgameFrm();
				//lgameFrame.setVisible(true);
				JDialog dialog = new JDialog(l.frame(), "", true);
				dialog.setAlwaysOnTop(true);
				Container contentPane = dialog.getContentPane();
				contentPane.setLayout(new BorderLayout());
				JPanel pane = new JPanel();
				contentPane.add(pane, BorderLayout.CENTER);
				pane.setBorder(new EmptyBorder(5, 5, 5, 5));
				pane.setLayout(new BorderLayout());
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setPreferredSize(new Dimension(600, 300));
					pane.add(scrollPane, BorderLayout.CENTER);
					JTextArea textArea = new JTextArea();
					textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
					textArea.setEditable(false);
					textArea.setText(s);
					scrollPane.setViewportView(textArea);
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				contentPane.add(buttonPane, BorderLayout.SOUTH);
				{
					JButton okButton = new JButton(Text.get("button.ok"));
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							dialog.dispose();
						}
					});
					okButton.setActionCommand(Text.get("button.ok"));
					buttonPane.add(okButton);
					dialog.getRootPane().setDefaultButton(okButton);
				}
				dialog.pack();
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(l.frame());
				dialog.show();
				dialog.dispose();
			}
		});
		showTeamBtn.setPreferredSize(new Dimension(100, 23));
		panel.add(openDirBtn);
		
		JButton logsBtn = new JButton(Text.get("button.showlogger"));
		panel.add(logsBtn);
		
		JButton settsBackBtn = new JButton(Text.get("button.back"));
		settsBackBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.set("javapath", javaPathField.getText());
				Config.set("path", libraryPathField.getText());
				Config.saveLater();
			}
		});
		settsBackBtn.addActionListener(frm.settingsListener);
		panel.add(settsBackBtn);
		logsBtn.addActionListener(new ActionListener() {
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
		
		JPanel ramTitlePane = new JPanel();
		FlowLayout fl_ramTitlePanel = (FlowLayout) ramTitlePane.getLayout();
		fl_ramTitlePanel.setVgap(0);
		fl_ramTitlePanel.setHgap(0);
		fl_ramTitlePanel.setAlignment(FlowLayout.LEFT);
		content.add(ramTitlePane, gbc);
		
		JPanel ramTitlePane2 = new JPanel();
		ramTitlePane2.setPreferredSize(new Dimension(610, 32));
		ramTitlePane.add(ramTitlePane2);
		JSlider ramSlider = new JSlider();
		ramTitlePane2.setLayout(new BorderLayout(0, 0));
		
		JPanel ramSpinnerPane = new JPanel();
		FlowLayout fl_ramSpinnerPane = (FlowLayout) ramSpinnerPane.getLayout();
		fl_ramSpinnerPane.setHgap(0);
		fl_ramSpinnerPane.setAlignment(FlowLayout.RIGHT);
		ramTitlePane2.add(ramSpinnerPane);
		
		JSpinner ramSpinner = new JSpinner();
		ramSpinnerPane.add(ramSpinner);
		try {
			ramSpinner.setModel(new SpinnerNumberModel(Config.getInt("xmx"), 1024, max, 512));
		} catch (Exception e) {
			Config.set("xmx", 1024);
			Config.saveLater();
			ramSpinner.setModel(new SpinnerNumberModel(Config.getInt("xmx"), 1024, max, 512));
		}
		
		JPanel ramSpinMargPane = new JPanel();
		ramSpinnerPane.add(ramSpinMargPane);
		
		JPanel ramTitleLabelPane = new JPanel();
		FlowLayout fl_ramTitleLabelPane = (FlowLayout) ramTitleLabelPane.getLayout();
		fl_ramTitleLabelPane.setVgap(10);
		ramTitlePane2.add(ramTitleLabelPane, BorderLayout.WEST);
		
		JLabel ramTitleLabel = new JLabel(Text.get("label.memory") + ": ");
		ramTitleLabelPane.add(ramTitleLabel);
		ramSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if((Integer)ramSpinner.getValue() == 0) return;
				ramSlider.setValue((Integer)ramSpinner.getValue());
				Config.set("xmx", (Integer)ramSpinner.getValue());
				Config.saveLater();
			}
		});

		JPanel ramSliderPane = new JPanel();
		FlowLayout fl_ramSliderPane = (FlowLayout) ramSliderPane.getLayout();
		fl_ramSliderPane.setAlignment(FlowLayout.LEFT);
		content.add(ramSliderPane, gbc.clone());

		ramSlider.setSnapToTicks(true);
		ramSlider.setPaintTicks(true);
		ramSlider.setMinorTickSpacing(512);
		ramSlider.setMajorTickSpacing(1024);
		ramSlider.setPreferredSize(new Dimension(600, 36));
		ramSlider.setMinimum(1024);
		ramSlider.setMaximum(max);
		ramSlider.setValue(Config.getInt("xmx"));
		ramSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(ramSlider.getValue() == 0) return;
				ramSpinner.setValue(ramSlider.getValue());
				Config.set("xmx", ramSlider.getValue());
				Config.saveLater();
			}
		});
		ramSliderPane.add(ramSlider);
		
		JPanel javaPathPane = new JPanel();
		javaPathPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		content.add(javaPathPane, gbc.clone());
		
		JLabel javaPathLabel = new JLabel(Text.get("label.javapath") + ": ");
		javaPathPane.add(javaPathLabel);
		
		javaPathField = new JTextField();
		javaPathField.setCaretColor(new Color(-1));
		javaPathField.addInputMethodListener(new InputMethodListener() {
			public void caretPositionChanged(InputMethodEvent arg0) {
			}
			public void inputMethodTextChanged(InputMethodEvent arg0) {
				Config.set("javapath", javaPathField.getText());
				Config.saveLater();
			}
		});
		javaPathField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.set("javapath", javaPathField.getText());
				Config.saveLater();
			}
		});
		
		JPanel javaPathSpacer = new JPanel();
		javaPathSpacer.setPreferredSize(new Dimension(143, 10));
		javaPathPane.add(javaPathSpacer);
		javaPathField.setText(Config.get("javapath"));
		javaPathPane.add(javaPathField);
		javaPathField.setColumns(40);
		
		JButton javaPathExploreBtn = new JButton("...");
		javaPathExploreBtn.addActionListener(new ActionListener() {
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
						javaPathField.setText(s);
					}
				}
			}
		});
		javaPathPane.add(javaPathExploreBtn);
		JPanel javaPathTipPane = new JPanel();
		javaPathTipPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		content.add(javaPathTipPane, gbc.clone());
		JLabel javaPathTipLabel = new JLabel(Text.get("label.javapathnote"));
		javaPathTipPane.add(javaPathTipLabel);
		JPanel javapathtipspacer = new JPanel();
		javapathtipspacer.setPreferredSize(new Dimension(50, 10));
		javaPathTipPane.add(javapathtipspacer);

		JPanel libraryDirPane = new JPanel();
		libraryDirPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		content.add(libraryDirPane, gbc.clone());
		
		JLabel libraryDirLabel = new JLabel(Text.get("label.savedir") + ": ");
		libraryDirPane.add(libraryDirLabel);
		
		libraryPathField = new JTextField();
		libraryPathField.setCaretColor(new Color(-1));
		libraryPathField.addInputMethodListener(new InputMethodListener() {
			public void caretPositionChanged(InputMethodEvent event) {
			}
			public void inputMethodTextChanged(InputMethodEvent event) {
				Config.set("path", libraryPathField.getText());
				Launcher.inst.pathChanged();
				Config.saveLater();
			}
		});
		libraryPathField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Config.set("path", libraryPathField.getText());
				Launcher.inst.pathChanged();
				Config.saveLater();
			}
		});
		
		JPanel libraryDirSpacer = new JPanel();
		libraryDirSpacer.setPreferredSize(new Dimension(50, 10));
		libraryDirPane.add(libraryDirSpacer);
		libraryPathField.setText(Config.get("path"));
		libraryPathField.setColumns(40);
		libraryDirPane.add(libraryPathField);
		
		JButton libraryDirExploreBtn = new JButton("...");
		libraryDirExploreBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new NativeJFileChooser(Config.get("path"));
				c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(c.showOpenDialog(SettingsPane.this) == JFileChooser.APPROVE_OPTION) {
					File f = c.getSelectedFile();
					if(f != null) {
						String s = f.toString();
						Config.set("path", s);
						Launcher.inst.pathChanged();
						libraryPathField.setText(s);
					}
				}
			}
		});
		libraryDirPane.add(libraryDirExploreBtn);
		
		JPanel titlePanel = new JPanel();
		//titlePanel.setVisible(false);
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		add(titlePanel, BorderLayout.NORTH);
		
		JLabel titleLabel = new JLabel("LGame Launcher " + Launcher.string_version);
		
		titlePanel.add(titleLabel);
		setPreferredSize(new Dimension(1000, 600));
		
		JPanel leftMarginPanel = new JPanel();
		add(leftMarginPanel, BorderLayout.WEST);
		
		JPanel rightMarginPanel = new JPanel();
		add(rightMarginPanel, BorderLayout.EAST);
	}

	private GridBagConstraints gbc() {
		GridBagConstraints gbc_ramTitlePanel = new GridBagConstraints();
		gbc_ramTitlePanel.gridwidth = GridBagConstraints.REMAINDER;
		gbc_ramTitlePanel.weightx = 1;
		gbc_ramTitlePanel.fill = GridBagConstraints.HORIZONTAL;
		return gbc_ramTitlePanel;
	}
}

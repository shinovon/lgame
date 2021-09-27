package ru.lgame.launcher.ui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.ui.Fonts;
import ru.lgame.launcher.ui.frame.LauncherFrm;
import ru.lgame.launcher.update.Modpack;
import ru.lgame.launcher.utils.logging.Log;

/**
 * @author Shinovon
 */
public class LauncherPane extends JPanel {
	
	private static final long serialVersionUID = 208595987287929416L;

	private JTextField usernameField;

	private ButtonGroup bg;

	private JPanel list;

	private JButton startBtn;

	private MiniModpackPane selected;

	private JScrollPane scrollPane;

	private JLabel skinImageLabel;

	private JLabel usernameLabel;

	public LauncherPane(LauncherFrm frm) {
		this.setLayout(new BorderLayout(0, 0));
		
		JPanel modpacksPanel = new JPanel();
		this.add(modpacksPanel, BorderLayout.CENTER);
		modpacksPanel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		/*scrollPane.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int i = e.getUnitsToScroll() * 10;
				int v = scrollPane.getVerticalScrollBar().getValue();
				scrollPane.getVerticalScrollBar().setValue(v + i);
			}
			
		});*/
		modpacksPanel.add(scrollPane);
		
		list = new JPanel();
		scrollPane.setViewportView(list);
		list.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridwidth = GridBagConstraints.REMAINDER;
        gbc2.weightx = 1;
        gbc2.weighty = 1;
        bg = new ButtonGroup();
		
		JPanel panel_2 = new JPanel();
		modpacksPanel.add(panel_2, BorderLayout.NORTH);
		
		JPanel bottomPanel = new JPanel();
		this.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		bottomPanel.add(panel_3_1, BorderLayout.WEST);
		
		JButton refreshBtn = new JButton("Обновить");
		refreshBtn.setActionCommand("Обновить");
		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshModpackList();
			}
		});
		panel_3_1.add(refreshBtn);
		
		JButton settingsBtn = new JButton("Настройки");
		settingsBtn.addActionListener(frm.settingsListener);
		panel_3_1.add(settingsBtn);
		
		JButton accountsBtn = new JButton("Аккаунты");
		accountsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Launcher.inst.showAccountsFrame();
			}
		});
		panel_3_1.add(accountsBtn);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setVgap(0);
		flowLayout_2.setHgap(0);
		panel.setPreferredSize(new Dimension(24, 24));
		panel_3_1.add(panel);
		
		skinImageLabel = new JLabel("");
		Image skin = null;
		try {
			if(Launcher.inst.currentAuth() != null) {
				// ыыы
				if(Launcher.inst.currentAuth().getUsername().equalsIgnoreCase("shinovon")) {
					skin = ImageIO.read(getClass().getResourceAsStream("/Shinovon.png"));
				}
			} else {
				//TODO
			}
		} catch (Exception e1) {
		}
		if(skin == null) {
			try {
				skin = ImageIO.read(getClass().getResourceAsStream("/defaultskin.png"));
			} catch (IOException e1) {
			}
		}
		if(skin != null) skinImageLabel.setIcon(new ImageIcon(skin));
		panel.add(skinImageLabel);
		
		usernameLabel = new JLabel("Добавьте авторизацию");
		panel_3_1.add(usernameLabel);
		if(Launcher.inst.currentAuth() != null) usernameLabel.setText("Добро пожаловать, " + Launcher.inst.currentAuth().getUsername());
		usernameLabel.setFont(Fonts.username);
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		bottomPanel.add(panel_3);
		
		JCheckBox forceUpdateCheck = new JCheckBox("Переустановить");
		panel_3.add(forceUpdateCheck);
		
		//usernameField = new JTextField();
		//panel_3.add(usernameField);
		//usernameField.setToolTipText("Ник");
		//usernameField.setText(Config.get("username"));
		//usernameField.setColumns(15);
		
		startBtn = new JButton("Играть");
		panel_3.add(startBtn);
		startBtn.setEnabled(false);
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Launcher.inst.currentAuth() != null) {
					int i = Launcher.inst.currentAuth().checkAuth();
					if(i == 0) {
						Config.saveLater();
						if(forceUpdateCheck.isSelected()) 
							Launcher.inst.runForceUpdate(Launcher.inst.currentAuth(), selected.getModpack());
						else Launcher.inst.run(Launcher.inst.currentAuth(), selected.getModpack());
					} else {
						JOptionPane.showMessageDialog(frm, "(" + i + ")", "", JOptionPane.WARNING_MESSAGE);
					}
				} else if(usernameField != null) {
					Config.set("username", usernameField.getText());
					Config.saveLater();
					if(usernameField.getText() == null || usernameField.getText().length() <= 4) {
						JOptionPane.showMessageDialog(frm, "Введите никнейм", "", JOptionPane.WARNING_MESSAGE);
						return;
					}
					if(forceUpdateCheck.isSelected()) 
						Launcher.inst.runForceUpdate(Auth.fromUsername(usernameField.getText()), selected.getModpack());
					else Launcher.inst.run(Auth.fromUsername(usernameField.getText()), selected.getModpack());
				} else {
					JOptionPane.showMessageDialog(frm, "Выберите аккаунт для игры", "", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		addModpacks();
		setPreferredSize(new Dimension(1000, 600));
		updateAuth();
	}
	
	public void updateAuth() {
		Image skin = null;
		try {
			if(Launcher.inst.currentAuth() != null) {
				if(Launcher.inst.currentAuth().getUsername().equalsIgnoreCase("shinovon")) {
					skin = ImageIO.read(getClass().getResourceAsStream("/Shinovon.png"));
				}
			} else {
				
			}
		} catch (Exception e1) {
		}
		if(skin == null) {
			try {
				skin = ImageIO.read(getClass().getResourceAsStream("/defaultskin.png"));
			} catch (IOException e1) {
			}
		}
		if(skin != null) skinImageLabel.setIcon(new ImageIcon(skin));
		if(Launcher.inst.currentAuth() != null) usernameLabel.setText("Добро пожаловать, " + Launcher.inst.currentAuth().getUsername());
		else usernameLabel.setText("Добавьте авторизацию");
	}

	public void setSelected(MiniModpackPane modpackPanel) {
		selected = modpackPanel;
		selected();
	}
	
	private void selected() {
		int i = selected.getModpack().getState();
		if(i == 0) startBtn.setText("Установить");
		else if(i == 1) startBtn.setText("Играть");
		else if(i == 2 || i == 3) startBtn.setText("Обновить");
		startBtn.setEnabled(true);
	}
	
	private void unselected() {
		startBtn.setEnabled(false);
		startBtn.setText("Играть");
	}
	
	/**
	 * Удалить все сборки из списка
	 */
	private void removeAllModpacks() {
		selected = null;
		unselected();
		bg.clearSelection();
		list.removeAll();
		scrollPane.revalidate();
		repaint();
	}

	/**
	 * Добавить сборку в список
	 */
	private void addModpack(MiniModpackPane sb) {
		sb.setButtonGroup(bg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        list.add(sb, gbc);
	}
	
	/**
	 * Обновить список сборок
	 */
	public void refreshModpackList() {
		Launcher.inst.queue(new Runnable() {
			public void run() {
				removeAllModpacks();
				Launcher.inst.refreshLauncherJson();
				addModpacks();
			}
		});
	}
	
	/**
	 * Добавить сборки из списка
	 */
	private void addModpacks() {
		Iterator<Modpack> i = Launcher.inst.getModpacks();
		while(i.hasNext()) {
			addModpack(i.next().createPanel());
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Log.error("addModpacks(): interrupted", e);
			}
		}
		scrollPane.revalidate();
		repaint();
	}

}

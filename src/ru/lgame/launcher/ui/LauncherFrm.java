package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import ru.lgame.launcher.Auth;
import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.Modpack;
import ru.lgame.launcher.utils.Log;
import ru.lgame.launcher.utils.StackLayout;

/**
 * Окно лаунчера
 * @author Shinovon
 */
public class LauncherFrm extends JFrame {

	private static final long serialVersionUID = 5754803808367340968L;
	
	private JPanel contentPane;
	private JTextField usernameField;

	private ButtonGroup bg;

	private JPanel list;

	private JButton startBtn;

	private ModpackPanel selected;

	private StackLayout layout;

	protected boolean settingsShowing;

	private JScrollPane scrollPane;

	/**
	 * Создает окно
	 */
	public LauncherFrm() {
		setTitle("Демонстрационный билд. Не готов к распространению");
		setUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(layout = new StackLayout());
		setContentPane(contentPane);
		
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new BorderLayout(0, 0));
		contentPane.add(settingsPanel, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 0));
		contentPane.add(mainPanel, BorderLayout.CENTER);

		ActionListener settsActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settingsShowing = !settingsShowing;
				layout.showComponent(settingsShowing ? settingsPanel : mainPanel, LauncherFrm.this);
			}
		};
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		settingsPanel.add(panel, BorderLayout.SOUTH);
		
		JButton settsBackBtn = new JButton("Назад");
		settsBackBtn.addActionListener(settsActionListener);
		panel.add(settsBackBtn);
		
		JPanel panel_1 = new JPanel();
		settingsPanel.add(panel_1, BorderLayout.CENTER);
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_4.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		settingsPanel.add(panel_4, BorderLayout.NORTH);
		
		JLabel label = new JLabel("Настройки");
		panel_4.add(label);
		
		layout.showComponent(mainPanel, this);
		
		JPanel modpacksPanel = new JPanel();
		mainPanel.add(modpacksPanel, BorderLayout.CENTER);
		modpacksPanel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int i = e.getUnitsToScroll() * 10;
				int v = scrollPane.getVerticalScrollBar().getValue();
				scrollPane.getVerticalScrollBar().setValue(v + i);
			}
			
		});
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
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		bottomPanel.add(panel_3_1, BorderLayout.WEST);
		
		JButton refreshBtn = new JButton("Обновить информацию");
		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshModpackList();
			}
		});
		panel_3_1.add(refreshBtn);
		
		JButton settingsBtn = new JButton("Настройки");
		settingsBtn.addActionListener(settsActionListener);
		panel_3_1.add(settingsBtn);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		bottomPanel.add(panel_3);
		
		JCheckBox forceUpdateCheck = new JCheckBox("Переустановить");
		panel_3.add(forceUpdateCheck);
		
		usernameField = new JTextField();
		panel_3.add(usernameField);
		usernameField.setToolTipText("Ник");
		usernameField.setText(Config.get("username"));
		usernameField.setColumns(15);
		
		startBtn = new JButton("Играть");
		panel_3.add(startBtn);
		startBtn.setEnabled(false);
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.set("username", usernameField.getText());
				Config.saveConfig();
				if(usernameField.getText() == null || usernameField.getText().length() <= 4) {
					JOptionPane.showMessageDialog(LauncherFrm.this, "Введите никнейм", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(forceUpdateCheck.isSelected()) 
					Launcher.inst.runForceUpdate(Auth.fromUsername(usernameField.getText()), selected.getModpack());
				else Launcher.inst.run(Auth.fromUsername(usernameField.getText()), selected.getModpack());
			}
		});
		contentPane.setPreferredSize(new Dimension(1000, 600));
		
		pack();
		setLocationRelativeTo(null);
		addModpacks();
	}

	public void setSelected(ModpackPanel modpackPanel) {
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
	private void addModpack(ModpackPanel sb) {
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
				scrollPane.revalidate();
				repaint();
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
			scrollPane.revalidate();
			repaint();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Log.error("addModpacks(): interrupted", e);
			}
		}
	}

	/**
	 * Установить LAF на системный
	 */
	private static void setUI() {
		try {
			String x = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(x);
		} catch (Exception e1) {
			Log.error("setUI()", e1);
		}

	}

}

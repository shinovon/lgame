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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import ru.lgame.launcher.Auth;
import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.Modpack;

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

	private JButton startButton;

	/**
	 * Создает окно
	 */
	public LauncherFrm() {
		setUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int i = e.getUnitsToScroll() * 10;
				int v = scrollPane.getVerticalScrollBar().getValue();
				scrollPane.getVerticalScrollBar().setValue(v + i);
			}
			
		});
		panel.add(scrollPane);
		
		list = new JPanel();
		scrollPane.setViewportView(list);
		list.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridwidth = GridBagConstraints.REMAINDER;
        gbc2.weightx = 1;
        gbc2.weighty = 1;
        bg = new ButtonGroup();
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.add(panel_3_1, BorderLayout.WEST);
		
		JButton refresh = new JButton("Обновить информацию");
		panel_3_1.add(refresh);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		panel_1.add(panel_3);
		
		JCheckBox forceUpdateCheck = new JCheckBox("Переустановить");
		panel_3.add(forceUpdateCheck);
		
		usernameField = new JTextField();
		panel_3.add(usernameField);
		usernameField.setToolTipText("Ник");
		usernameField.setText(Config.get("username"));
		usernameField.setColumns(15);
		
		startButton = new JButton("Играть");
		panel_3.add(startButton);
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if(forceUpdateCheck.isSelected()) 
					Launcher.inst.runForceUpdate(Auth.fromUsername(usernameField.getText()));
				else Launcher.inst.run(Auth.fromUsername(usernameField.getText()));
			}
			
		});
		contentPane.setPreferredSize(new Dimension(1000, 600));
		pack();
		setLocationRelativeTo(null);
		addModpacks();
	}
	
	/**
	 * Удалить все сборки из списка
	 */
	private void removeAllModpacks() {
		bg.clearSelection();
		list.removeAll();
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
		removeAllModpacks();
		addModpacks();
	}
	
	/**
	 * Добавить сборки из списка
	 */
	private void addModpacks() {
		Iterator<Modpack> i = Launcher.inst.getModpacks();
		while(i.hasNext()) {
			addModpack(i.next().createPanel());
		}
	}

	/**
	 * Установить LAF на системный
	 */
	private static void setUI() {
		try {
			String x = UIManager.getSystemLookAndFeelClassName();
			//String x2 = UIManager.getCrossPlatformLookAndFeelClassName();
			//if (Settings.getBoolean("systemLookAndFeel")) {
				UIManager.setLookAndFeel(x);
			//} else {
			//	UIManager.setLookAndFeel(x2);
			//}
			//if (Settings.getBoolean("customUI")) {
			//UIManager.put("ButtonUI", MyButtonUI.class.getName());
			//}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

	}

}

package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.auth.AuthStore;

public class AccountsFrm extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5110118649257355719L;
	
	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;

	private JList<Auth> list;

	/**
	 * Create the frame.
	 */
	public AccountsFrm() {
		setType(Type.UTILITY);
		setTitle("Аккаунты");
		setResizable(false);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel accountPanel = new JPanel();
		accountPanel.setPreferredSize(new Dimension(144, 10));
		contentPane.add(accountPanel, BorderLayout.EAST);
		accountPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_3.setMaximumSize(new Dimension(140, 32767));
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setVgap(0);
		accountPanel.add(panel_3);
		
		JPanel panel_6 = new JPanel();
		panel_6.setPreferredSize(new Dimension(120, 70));
		panel_3.add(panel_6);
		
		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_7.getLayout();
		flowLayout_2.setHgap(3);
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_7.setPreferredSize(new Dimension(140, 100));
		panel_7.setMaximumSize(new Dimension(140, 32767));
		panel_3.add(panel_7);
		
		JLabel usernameLabel = new JLabel("Ник");
		panel_7.add(usernameLabel);
		
		usernameField = new JTextField();
		panel_7.add(usernameField);
		usernameField.setMaximumSize(new Dimension(300, 2147483647));
		usernameField.setMinimumSize(new Dimension(120, 20));
		usernameField.setPreferredSize(new Dimension(120, 20));
		usernameField.setColumns(16);
		
		JLabel passwordLabel = new JLabel("Пароль");
		panel_7.add(passwordLabel);
		
		passwordField = new JPasswordField();
		panel_7.add(passwordField);
		passwordField.setPreferredSize(new Dimension(120, 20));
		passwordField.setMinimumSize(new Dimension(120, 20));
		passwordField.setMaximumSize(new Dimension(300, 2147483647));
		passwordField.setColumns(16);
		passwordField.setEnabled(false);
		
		JPanel panel_4 = new JPanel();
		accountPanel.add(panel_4, BorderLayout.NORTH);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboBox.getSelectedIndex() == 0) {
					usernameLabel.setText("Ник");
					passwordField.setEnabled(false);
					usernameField.setEnabled(true);
				} else {
					usernameLabel.setText("Email");
					passwordField.setEnabled(true);
					usernameField.setEnabled(true);
				}
				update();
			}
		});
		panel_4.add(comboBox);
		comboBox.setPreferredSize(new Dimension(134, 20));
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"По нику", "Mojang"}));
		
		JPanel panel_5 = new JPanel();
		accountPanel.add(panel_5, BorderLayout.SOUTH);
		
		JButton confirmBtn = new JButton("Подтвердить");
		panel_5.add(confirmBtn);
		confirmBtn.setPreferredSize(new Dimension(134, 20));
		passwordField.setEnabled(false);
		usernameField.setEnabled(false);
		comboBox.setEnabled(false);
		confirmBtn.setEnabled(false);
		confirmBtn.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent event) {
				passwordField.setEnabled(false);
				usernameField.setEnabled(false);
				comboBox.setEnabled(false);
				confirmBtn.setEnabled(false);
				try {
					Auth a = null;
					if(comboBox.getSelectedIndex() == 0) {
						a = Auth.fromUsername(usernameField.getText());
					} else {
						a = Auth.fromMojang(usernameField.getText(), passwordField.getText());
					}
					AuthStore.add(a);
					AuthStore.setSelected(a);
				} catch (Throwable e) {
					Launcher.inst.showError("Аккаунты", "Не удалось добавить аккаунт", e);
				}
				update();
			}
		});
		
		JPanel listPanel = new JPanel();
		listPanel.setPreferredSize(new Dimension(180, 10));
		contentPane.add(listPanel, BorderLayout.WEST);
		listPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		
		list = new JList<Auth>();
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
		    public Component getListCellRendererComponent(
		            JList<?> list,
		            Object value,
		            int index,
		            boolean isSelected,
		            boolean cellHasFocus) {
		    	if(value instanceof Auth) {
		    		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		    		setText(((Auth) value).getUsername() + "(" + ((Auth) value).getType() + ")");
		    	} else {
		    		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		    	}
		    	return this;
		    }
		});
		list.setSelectedValue(Launcher.inst.currentAuth(), true);
		JButton deleteBtn = new JButton("Удалить");
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(list.getSelectedValue() != null) {
					AuthStore.setSelected(list.getSelectedValue());
					deleteBtn.setEnabled(true);
					Launcher.inst.frame().updateAuth();
				}
			}
		});
		scrollPane.setViewportView(list);
		
		JPanel centerPanel = new JPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);
		
		JButton addBtn = new JButton("Добавить");
		addBtn.setPreferredSize(new Dimension(89, 23));
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboBox.getSelectedIndex() == 0) {
					usernameLabel.setText("Ник");
					passwordField.setEnabled(false);
					usernameField.setEnabled(true);
				} else {
					usernameLabel.setText("Email");
					passwordField.setEnabled(true);
					usernameField.setEnabled(true);
				}
				comboBox.setEnabled(true);
				confirmBtn.setEnabled(true);
				update();
			}
		});
		centerPanel.add(addBtn);
		
		deleteBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AuthStore.remove(list.getSelectedValue());
				update();
			}
		});
		deleteBtn.setEnabled(false);
		deleteBtn.setPreferredSize(new Dimension(89, 23));
		centerPanel.add(deleteBtn);
		contentPane.setPreferredSize(new Dimension(450, 300));
		pack();
		setLocationRelativeTo(null);
		update();
	}
	
	public void update() {
		list.removeAll();
		list.setListData(AuthStore.list());
		list.setSelectedValue(Launcher.inst.currentAuth(), true);
		Launcher.inst.frame().updateAuth();
	}

}

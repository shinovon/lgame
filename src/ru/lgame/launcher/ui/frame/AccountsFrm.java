package ru.lgame.launcher.ui.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONObject;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.auth.AuthStore;
import ru.lgame.launcher.auth.AuthType;
import ru.lgame.launcher.ui.ErrorUI;
import ru.lgame.launcher.ui.locale.Text;
import ru.lgame.launcher.utils.HttpUtils;

import java.awt.Color;

/**
 * окно с аккаунтами
 * @author Shinovon
 */
public class AccountsFrm extends JFrame {

	private static final long serialVersionUID = 5110118649257355719L;
	
	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;

	private JList<Auth> list;

	private JButton confirmBtn;

	/**
	 * Create the frame.
	 */
	public AccountsFrm() {
		setType(Type.UTILITY);
		setTitle(Text.get("title.accounts"));
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
		
		JLabel usernameLabel = new JLabel(Text.get("label.username"));
		panel_7.add(usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setCaretColor(new Color(-1));
		panel_7.add(usernameField);
		usernameField.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
		usernameField.setMinimumSize(new Dimension(120, 20));
		usernameField.setPreferredSize(new Dimension(120, 20));
		usernameField.setColumns(16);
		
		JLabel passwordLabel = new JLabel(Text.get("label.password"));
		panel_7.add(passwordLabel);
		
		passwordField = new JPasswordField();
		passwordField.setCaretColor(new Color(-1));
		panel_7.add(passwordField);
		passwordField.setPreferredSize(new Dimension(120, 20));
		passwordField.setMinimumSize(new Dimension(120, 20));
		passwordField.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
		passwordField.setColumns(16);
		passwordField.setEnabled(false);
		
		JPanel panel_4 = new JPanel();
		accountPanel.add(panel_4, BorderLayout.NORTH);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboBox.getSelectedIndex() == 0) {
					usernameLabel.setText(Text.get("label.username"));
					confirmBtn.setText(Text.get("button.account.add"));
					passwordField.setEnabled(false);
					usernameField.setEnabled(true);
				} else {
					usernameLabel.setText(Text.get("label.email"));
					confirmBtn.setText(Text.get("button.account.auth"));
					passwordField.setEnabled(true);
					usernameField.setEnabled(true);
				}
				update();
			}
		});
		panel_4.add(comboBox);
		comboBox.setPreferredSize(new Dimension(134, 20));
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {Text.get("button.account.cracked"), Text.get("button.account.ely")}));
		JPanel panel_5 = new JPanel();
		accountPanel.add(panel_5, BorderLayout.SOUTH);
		
		confirmBtn = new JButton(Text.get("button.account.add"));
		panel_5.add(confirmBtn);
		confirmBtn.setPreferredSize(new Dimension(134, 20));
		passwordField.setEnabled(false);
		usernameField.setEnabled(false);
		comboBox.setEnabled(false);
		confirmBtn.setEnabled(false);
		confirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				passwordField.setEnabled(false);
				usernameField.setEnabled(false);
				comboBox.setEnabled(false);
				confirmBtn.setEnabled(false);
				try {
					if (comboBox.getSelectedIndex() == 1) {
						try {
							AuthStore.addAndSelect(Auth.getEly(usernameField.getText(), passwordField.getText()));
						} catch (Exception e) {
							if ("ForbiddenOperationException".equals(e.getMessage())) {
								String code = JOptionPane.showInputDialog(AccountsFrm.this, "Введите OTP код", "");
								if (code != null)
									AuthStore.addAndSelect(Auth.getEly(usernameField.getText(), passwordField.getText() + ":" + code));
							} else {
								throw e;
							}
						}
					} else {
						String name = usernameField.getText();
						String uuid = null;
						try {
							uuid = AuthStore.getUUID(getName());
						} catch (Exception ignored) {}
						AuthStore.addAndSelect(Auth.get(name, uuid));
					}
				} catch (Throwable e) {
					ErrorUI.showError(Text.get("title.accounts"), Text.get("err.accountadd"), e);
				}
				usernameField.setText("");
				passwordField.setText("");
				update();
			}
		});
		
		JPanel listPanel = new JPanel();
		listPanel.setPreferredSize(new Dimension(220, 10));
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
					setText(((Auth) value).getUsername() + (!((Auth) value).isCracked() ? " (" + localizeType(((Auth) value).getType()) + ")" : ""));
				} else {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
				return this;
			}
		});
		list.setSelectedValue(Launcher.inst.currentAuth(), true);
		JButton deleteBtn = new JButton("-");
		deleteBtn.setPreferredSize(new Dimension(48, 23));
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
		
		JButton addBtn = new JButton("+");
		addBtn.setPreferredSize(new Dimension(48, 23));
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usernameField.setText("");
				passwordField.setText("");
				if(comboBox.getSelectedIndex() == 0) {
					usernameLabel.setText(Text.get("label.username"));
					confirmBtn.setText(Text.get("button.account.add"));
					passwordField.setEnabled(false);
					usernameField.setEnabled(true);
				} else {
					usernameLabel.setText(Text.get("label.email"));
					confirmBtn.setText(Text.get("button.account.auth"));
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
		deleteBtn.setPreferredSize(new Dimension(48, 23));
		centerPanel.add(deleteBtn);
		contentPane.setPreferredSize(new Dimension(450, 300));
		pack();
		setLocationRelativeTo(null);
		update();
	}
	
	protected String localizeType(AuthType type) {
		if(type == AuthType.MOJANG) {
			return "Mojang";
		}
		if(type == AuthType.MICROSOFT) {
			return "Microsoft";
		}
		if(type == AuthType.LGAME) {
			return "LGame";
		}
		if(type == AuthType.ELY) {
			return "Ely.by";
		}
		return "По нику";
	}

	public void update() {
		list.removeAll();
		list.setListData(AuthStore.list());
		list.setSelectedValue(Launcher.inst.currentAuth(), true);
		Launcher.inst.frame().updateAuth();
	}

}

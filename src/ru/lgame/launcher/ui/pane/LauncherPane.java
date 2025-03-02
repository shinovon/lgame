package ru.lgame.launcher.ui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import ru.lgame.launcher.Config;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.auth.AuthStore;
import ru.lgame.launcher.ui.Fonts;
import ru.lgame.launcher.ui.frame.LauncherFrm;
import ru.lgame.launcher.ui.locale.Text;
import ru.lgame.launcher.update.Modpack;
import ru.lgame.launcher.utils.HttpUtils;
import ru.lgame.launcher.utils.logging.Log;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

	protected boolean skinState;

	protected String skinName;

	protected boolean customWarn;

	private LauncherFrm frm;

	private JCheckBox forceUpdateCheck;

	public LauncherPane(LauncherFrm frm) {
		this.frm = frm;
		this.setLayout(new BorderLayout(0, 0));
		
		JPanel warnPane = new JPanel();
		//warnPane.setVisible(!Config.getBoolean("betaWarnShown"));
		warnPane.setVisible(false);
		warnPane.setBackground(Color.ORANGE);
		this.add(warnPane, BorderLayout.NORTH);
		warnPane.setLayout(new BorderLayout(0, 0));
		
		JPanel warnTextPane = new JPanel();
		warnTextPane.setBackground(Color.ORANGE);
		warnPane.add(warnTextPane, BorderLayout.CENTER);
		warnTextPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!customWarn)
				try {
					//Runtime.getRuntime().exec("explorer \"https://vk.com/im?sel=381458425\"");
					Runtime.getRuntime().exec("explorer \"https://t.me/shinovon\"");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JLabel warnTextLabel = new JLabel(Text.get("label.warnbeta"));
		warnTextLabel.setForeground(Color.BLACK);
		warnTextPane.add(warnTextLabel);
		
		JPanel warnClosePane = new JPanel();
		warnClosePane.setBackground(Color.ORANGE);
		warnPane.add(warnClosePane, BorderLayout.EAST);
		
		JLabel warnCloseLabel = new JLabel("x");
		warnCloseLabel.setForeground(Color.BLACK);
		warnCloseLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				warnCloseLabel.setForeground(Color.GRAY);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				warnPane.setVisible(false);
				if(!customWarn) Config.set("betaWarnShown", true);
				Config.saveLater();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				warnCloseLabel.setForeground(Color.BLACK);
			}
		});
		warnClosePane.add(warnCloseLabel);
		
		JPanel contentPane2 = new JPanel();
		contentPane2.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(contentPane2, BorderLayout.CENTER);
		contentPane2.setLayout(new BorderLayout(0, 0));
		
		JPanel modpacksPanel = new JPanel();
		contentPane2.add(modpacksPanel, BorderLayout.CENTER);
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
		bg = new ButtonGroup();
		
		JPanel bottomPanel = new JPanel();
		contentPane2.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_3_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		bottomPanel.add(panel_3_1, BorderLayout.WEST);
		
		JButton refreshBtn = new JButton(Text.get("button.refresh"));
		refreshBtn.setActionCommand(Text.get("button.refresh"));
		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshModpackList();
			}
		});
		//panel_3_1.add(refreshBtn);
		
		JButton settingsBtn = new JButton(Text.get("button.settings"));
		settingsBtn.addActionListener(frm.settingsListener);
		panel_3_1.add(settingsBtn);
		
		JButton accountsBtn = new JButton(Text.get("button.accounts"));
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
		getSkin();
		if(skin == null) {
			try {
				skin = ImageIO.read(getClass().getResourceAsStream("/defaultskin.png"));
			} catch (IOException e1) {
			}
		}
		if(skin != null) skinImageLabel.setIcon(new ImageIcon(skin));
		panel.add(skinImageLabel);
		
		usernameLabel = new JLabel(Text.get("label.account.no"));
		panel_3_1.add(usernameLabel);
		if(Launcher.inst.currentAuth() != null) usernameLabel.setText(Text.get("label.account.welcome") + ", " + Launcher.inst.currentAuth().getUsername());
		usernameLabel.setFont(Fonts.username);
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.TRAILING);
		bottomPanel.add(panel_3);
		
		forceUpdateCheck = new JCheckBox(Text.get("check.forceinstall"));
		panel_3.add(forceUpdateCheck);
		
		//usernameField = new JTextField();
		//panel_3.add(usernameField);
		//usernameField.setToolTipText("Ник");
		//usernameField.setText(Config.get("username"));
		//usernameField.setColumns(15);
		
		startBtn = new JButton(Text.get("button.play"));
		panel_3.add(startBtn);
		startBtn.setEnabled(false);
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});
		addModpacks();
		setPreferredSize(new Dimension(1000, 600));
		updateAuth();
	}
	
	public void start() {
		if(Launcher.inst.currentAuth() != null) {
			int i = Launcher.inst.currentAuth().checkAuth();
			if(i == 0) {
				Config.saveLater();
				if(forceUpdateCheck.isSelected()) 
					Launcher.inst.runForceUpdate(Launcher.inst.currentAuth(), selected.getModpack());
				else Launcher.inst.run(Launcher.inst.currentAuth(), selected.getModpack());
			} else {
				JOptionPane.showMessageDialog(frm, "auth response: " + i, "", JOptionPane.WARNING_MESSAGE);
			}
		} else if(usernameField != null) {
			Config.set("username", usernameField.getText());
			Config.saveLater();
			if(usernameField.getText() == null || usernameField.getText().length() <= 4) {
				JOptionPane.showMessageDialog(frm, Text.get("msg.nousername"), "", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if(forceUpdateCheck.isSelected()) 
				Launcher.inst.runForceUpdate(Auth.get(usernameField.getText()), selected.getModpack());
			else Launcher.inst.run(Auth.get(usernameField.getText()), selected.getModpack());
		} else {
			JOptionPane.showMessageDialog(frm, Text.get("msg.noaccount"), "", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void getSkin() {
		Launcher.inst.queue(new Runnable() {
			public void run() {
				boolean fail = false;
				l: {
				try {
					if(Launcher.inst.currentAuth() != null) {
						if(skinState) return;
						Auth a = Launcher.inst.currentAuth();
						String uuid = null;
						String username = a.getUsername();
						if (a.isEly()) {
							skinImageLabel.setIcon(null);
							skinName = username;
							skinState = true;
							return;
						}
						if(a.isMojang()) uuid = a.getUUID();
						if(uuid == null) {
							uuid = AuthStore.getUUID(username);
						}
						if(uuid == null) {
							skinName = username;
							skinState = true;
							fail = true;
							break l;
						}
						String url = "https://crafatar.com/avatars/" + uuid + "?overlay&size=24";
						Image ci = Launcher.inst.getCachedImage(url);
						if(ci != null) {
							skinImageLabel.setIcon(new ImageIcon(ci));
							skinName = username;
							skinState = true;
							return;
						}
						try {
							byte[] b = HttpUtils.getBytes(url);
							BufferedImage img = (BufferedImage) ImageIO.read(new ByteArrayInputStream(b));
							Launcher.inst.saveImageToCachePng(url, img);
							skinImageLabel.setIcon(new ImageIcon(img));
							skinName = username;
							skinState = true;
						} catch (IOException e) {
							if(ci == null) fail = true;
						}
					} else {
						fail = true;
					}
				} catch (Exception e) {
					Log.error("skin gather failed", e);
					fail = true;
				}
				}
				if(fail) {
					Image skin = null;
					try {
						skin = ImageIO.read(getClass().getResourceAsStream("/defaultskin.png"));
					} catch (IOException e2) {
					}
					if(skin != null) skinImageLabel.setIcon(new ImageIcon(skin));
				}
			}
		});
		
	}

	public void updateAuth() {
		if(skinState && Launcher.inst.currentAuth() != null && !Launcher.inst.currentAuth().getUsername().equalsIgnoreCase(skinName)) {
			skinState = false;
		}
		getSkin();
		if(Launcher.inst.currentAuth() != null) usernameLabel.setText(Text.get("label.account.welcome") + ", " + Launcher.inst.currentAuth().getUsername());
		else usernameLabel.setText(Text.get("label.account.no"));
	}

	public void setSelected(MiniModpackPane modpackPanel) {
		selected = modpackPanel;
		selected();
	}
	
	private void selected() {
		int i = selected.getModpack().getState();
		if(i == 0) startBtn.setText(Text.get("button.install"));
		else if(i == 1) startBtn.setText(Text.get("button.play"));
		else if(i == 2 || i == 3) startBtn.setText(Text.get("button.update"));
		startBtn.setEnabled(true);
	}
	
	private void unselected() {
		startBtn.setEnabled(false);
		startBtn.setText(Text.get("button.play"));
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
	@SuppressWarnings("unused")
	private void addModpack(MiniModpackPane sb) {
		if(sb.getModpack().isHidden() && !Launcher.DEBUG) return;
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
		ArrayList<Modpack> list = new ArrayList<Modpack>();
		while (i.hasNext()) {
			list.add(i.next());
		}
		Collections.sort(list, new Comparator<Modpack>() {
			public int compare(Modpack o1, Modpack o2) {
				return -o1.getReleaseDate().compareTo(o2.getReleaseDate());
			}
		});
		i = list.iterator();
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
	
	public void update() {
		selected();
	}

}

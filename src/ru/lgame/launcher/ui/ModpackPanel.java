package ru.lgame.launcher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.Modpack;
import ru.lgame.launcher.utils.logging.Log;

/**
 * Панелька сборки с описанием и картинкой
 * @author Shinovon
 */
public class ModpackPanel extends JPanel {
	
	private static final long serialVersionUID = 5622859608833406220L;

	private static final int ITEM_HEIGHT = 242;
	private static final int IMAGE_HEIGHT = 240;
	private static final int MAX_WIDTH = 560;

	private static Font nameFont;

	private static Font descFont;

	private Modpack modpack;
	
	private String id;
	private String modpackName;
	private String category;
	private String desc;
	private String version;

	private String[] descArr;

	private Image image;

	private JRadioButton radioButton;

	private JLabel imageLabel;

	private int lastW;

	private String updateText1;
	private String updateText2;
	private int updatePercent = -1;

	public ModpackPanel(String id) {
		this(id, null);
	}

	public ModpackPanel(String i, ButtonGroup bg) {
		this.id = i;
		setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		imageLabel = new JLabel();
		radioButton = new JRadioButton();
		if(bg != null) bg.add(radioButton);
		radioButton.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(radioButton.isSelected())
					selected();
				else unselected();
			}
			
		});
		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				radioButton.setSelected(true);
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
			
		});
		setInformation(id, id);
	}
	
	/**
	 * Задать группу кнопок
	 */
	public void setButtonGroup(ButtonGroup bg) {
		bg.add(radioButton);
	}
	
	protected void unselected() {
		Log.debug("unselected: " + id);
		setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
	}

	protected void selected() {
		Log.debug("selected: " + id);
		setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
		Launcher.inst.frame().mainPane().setSelected(this);
	}

	/**
	 * Задать объект сборки
	 */
	public void setModpack(Modpack mp) {
		this.modpack = mp;
	}

	/**
	 * Получить объект сборки
	 */
	public Modpack getModpack() {
		return modpack;
	}

	/**
	 * Получить идентификатор сборки
	 */
	public String id() {
		return id;
	}
	
	/**
	 * Добавить информацию
	 * @param name Название
	 * @param desc Описание
	 * @return Самого себя
	 */
	public ModpackPanel setInformation(String name, String desc) {
		this.modpackName = name;
		this.desc = desc;
		this.descArr = null;
		updateContents();
		return this;
	}
	
	/**
	 * Добавляет картинку
	 * @param img Изображение
	 * @return Самого себя
	 */
	public ModpackPanel setImage(Image img) {
		this.image = img;
		this.descArr = null;
		imageLabel.setIcon(new ImageIcon(image));
		int iw = image.getWidth(null);
		int ih = image.getHeight(null);
		double r = (double) iw / (double) ih;
		int w = (int) (r * (double)IMAGE_HEIGHT);
		//int factor = greatestCommonFactor(iw, ih);
		//Log.debug(id + " image scaled to: " + w + "x" + ITEM_HEIGHT + " (" + iw / factor + ":" + ih / factor + ")");
		image = image.getScaledInstance(w, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
		updateContents();
		return this;
	}
	
	public boolean isUpdating() {
		if(modpack == null) return false;
		return modpack.isUpdating();
	}
	
	public boolean isStarted() {
		if(modpack == null) return false;
		return modpack.isStarted();
	}
	
	public synchronized void setUpdateInfo(String s1, String s2, int percent) {
		if(s1 != null) this.updateText1 = s1;
		if(s2 != null) this.updateText2 = s2;
		if(percent >= -1) this.updatePercent = percent;
		if(percent > 100) this.updatePercent = 100;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				updateContents();
			}
		});
	}
	
	public void updateContents() {
		//removeAll();
		//add(imageLabel);
		//add(new JLabel(sborkaName));
		//add(new JLabel(desc));
		setPreferredSize(new Dimension(200, ITEM_HEIGHT));
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		setBackground(new Color(21, 22, 24));
		super.paintComponent(g);
		if(g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			//g2d.setRenderingHint(
			//		RenderingHints.KEY_TEXT_ANTIALIASING,
			//		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		g.setColor(UIManager.getColor("Label.foreground"));
		boolean b = true;
		Font f = g.getFont();
		int yyy = 1;
		int w = getWidth();
		int h = getHeight() - 2;
		int rh = getHeight() - 2;
		int th = g.getFontMetrics().getHeight();
		int th2 = th / 2;
		int x = 5;
		if(image != null) {
			g.drawImage(image, b ? w - image.getWidth(null) - 1 : 0, yyy, null);
			if(b) w -= image.getWidth(null);
		}
		int tw = w - x - 12;
		if(tw > MAX_WIDTH) tw = MAX_WIDTH;
		Font of = g.getFont();
		g.setFont(nameFont);
		g.drawString(modpackName, x, g.getFontMetrics(nameFont).getHeight() / 2 + 5);
		int ty = g.getFontMetrics(nameFont).getHeight() + 10 + yyy;
		g.setFont(descFont);
		if((descArr == null || lastW != w) && desc != null) descArr = getStringArray(desc, tw, g.getFontMetrics(descFont));
		if(descArr != null) {
			int dth = g.getFontMetrics(descFont).getHeight() - 6;
			for(int i = 0; i < descArr.length; i++) if(descArr[i] != null) g.drawString(descArr[i], x, ty + (i * (dth + 1)));
		}
		g.setFont(of);
		if(isStarted()) {
			g.setColor(new Color(0, 200, 57));
			g.fillRect(0, 0, w - 1, 3);
			g.fillRect(0, 0, 3, h);
			g.fillRect(0, rh - 2, w - 1, 3);
			g.fillRect(w - 4, 0, 3, rh);
			Font ff = g.getFont().deriveFont(12.0F);
			g.setFont(ff);
			g.drawString("Запущена", x, rh - (g.getFontMetrics(ff).getHeight() / 2));
		} else if(isUpdating()) {
			int percent = this.updatePercent;
			String s = this.updateText1;
			String p = this.updateText2;
			if(s == null) s = "null";
			if(p == null) p = "null"; 
			String s1 = s + (percent >= 0 ? " " + percent + "%" : "");
			String s2 = "" + p;
			
			int ptx = x;
			if(percent != -1) {
				int px = x - 5;
				int pww = w - px;
				g.setColor(new Color(57, 57, 57));
				g.fillRect(px, h - 3, pww - 1, 4);
				g.setColor(new Color(65, 119, 179));
				int pw = (int) ((double)pww*((double)percent/100D));
				g.fillRect(px, h - 3, pw - 1, 4);
			}
			Font ff = g.getFont().deriveFont(12.0F);
			g.setFont(ff);
			g.drawString(s1, ptx, h - th - th2 - 3);
			g.setFont(f);
			g.setColor(UIManager.getColor("Label.foreground"));
			g.drawString(s2, ptx, h - th2 - 1);
		}
		lastW = w;
	}
	
	private static int greatestCommonFactor(int width, int height) {
		return (height == 0) ? width : greatestCommonFactor(height, width % height);
	}
	
	public static String replace(String str, String from, String to) {
		if(from.equals(""))
			return str;
		if(str.indexOf(from) < 0)
			return str;
		if (from.length() == 1 && to.length() == 1)
			return str.replace(from.charAt(0), to.charAt(0));
		int var3 = 0;

		while (var3 >= 0 && var3 < str.length()) {
			var3 = str.indexOf(from, var3);
			if (var3 >= 0) {
				str = str.substring(0, var3) + to + str.substring(var3 + from.length());
				++var3;
			}
		}
		return str;
	}

	/**
	 * Добавляет в текст переносы строки чтобы поместился в окне
	 * @param text Текст
	 * @param width Максимальная ширина
	 * @param font Шрифт
	 */
	public static String[] getStringArray(String text, int maxWidth, FontMetrics font) {
		if (text == null || text.length() == 0 || text.equals(" ") || maxWidth < font.charWidth('A') + 5) {
			return new String[0];
		}
		text = replace(text, "\r", "");
		ArrayList<String> v = new ArrayList<String>();
		char[] chars = text.toCharArray();
		if (text.indexOf('\n') > -1) {
			int j = 0;
			for (int i = 0; i < text.length(); i++) {
				if (chars[i] == '\n') {
					v.add(text.substring(j, i));
					j = i + 1;
				}
			}
			v.add(text.substring(j, text.length()));
		} else {
			v.add(text);
		}
		for (int i = 0; i < v.size(); i++) {
			String s = (String) v.get(i);
			if(font.stringWidth(s) >= maxWidth) {
				int i1 = 0;
				for (int i2 = 0; i2 < s.length(); i2++) {
					if (font.stringWidth(text.substring(i1, i2)) >= maxWidth) {
						boolean space = false;
						for (int j = i2; j > i1; j--) {
							char c = s.charAt(j);
							if (c == ' ' || c == '-') {
								space = true;
								v.set(i, s.substring(i1, j + 1));
								v.add(i + 1, s.substring(j + 1));
								i += 1;
								i2 = i1 = j + 1;
								break;
							}
						}
						if (!space) {
							i2 = i2 - 2;
							v.set(i, s.substring(i1, i2));
							v.add(i + 1, s.substring(i2));
							i2 = i1 = i2 + 1;
							i += 1;
						}
					}
				}
			}
		}
		return v.toArray(new String[0]);
	}
	
	static {
		try {
			descFont = Font.createFont(Font.TRUETYPE_FONT, ModpackPanel.class.getResourceAsStream("/font/Montserrat-Regular.ttf")).deriveFont(0, 15);
		} catch (Exception e) {
			Log.warn("Font load failed: " + e.toString());
			descFont = Font.getFont(Font.SANS_SERIF).deriveFont(0, 15);
		}
		try {
			nameFont = Font.createFont(Font.TRUETYPE_FONT, ModpackPanel.class.getResourceAsStream("/font/Montserrat-Bold.ttf")).deriveFont(Font.BOLD, 18);
		} catch (Exception e) {
			Log.warn("Font load failed: " + e.toString());
			nameFont = Font.getFont(Font.SANS_SERIF).deriveFont(Font.BOLD, 18);
		}
	}

}

package ru.lgame.launcher.ui;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.Modpack;
import ru.lgame.launcher.utils.Log;

public class ModpackPanel extends JPanel {
	
	private static final long serialVersionUID = 5622859608833406220L;

	private static final int ITEM_HEIGHT = 240;

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
	private int updatePercent;

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
		Launcher.inst.frame().setSelected(this);
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
		int w = (int) (r * (double)ITEM_HEIGHT);
		//int factor = greatestCommonFactor(iw, ih);
		//Log.debug(id + " image scaled to: " + w + "x" + ITEM_HEIGHT + " (" + iw / factor + ":" + ih / factor + ")");
		image = image.getScaledInstance(w, ITEM_HEIGHT, Image.SCALE_SMOOTH);
		updateContents();
		return this;
	}
	
	public boolean isUpdating() {
		if(modpack == null) return false;
		return modpack.isUpdating();
	}
	
	public void setUpdateInfo(String s1, String s2, int percent) {
		if(s1 != null) this.updateText1 = s1;
		if(s2 != null) this.updateText2 = s2;
		if(percent >= 0) this.updatePercent = percent;
		Launcher.inst.queue(new Runnable() {
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
		super.paintComponent(g);
		if(g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setColor(Color.BLACK);
		Font f = g.getFont();
		int w = getWidth();
		int h = getHeight();
		int th = g.getFontMetrics().getHeight();
		int th2 = th / 2;
		int x = 5;
		if(image != null) {
			g.drawImage(image, 0, 0, null);
			x += image.getWidth(null);
		}
		int tw = w - x - 5;
		if((descArr == null || lastW != w) && desc != null) descArr = getStringArray(desc, tw, g.getFontMetrics());
		g.drawString(modpackName, x, 12);
		if(descArr != null) {
			for(int i = 0; i < descArr.length; i++) g.drawString(descArr[i], x, 16 + th + (i * (th + 1)));
		}
		if(isUpdating()) {
			int percent = this.updatePercent;
			String s = this.updateText1;
			String p = this.updateText2;
			
			String s1 = s + " " + percent + "%";
			String s2 = p;
			
			int px = x - 5;
			int ptx = x;
			int pww = w - px;
			g.setColor(new Color(57, 57, 57));
			g.fillRect(px, h - 4, pww, 4);
			g.setColor(new Color(65, 119, 179));
			int pw = (int) ((double)pww*((double)percent/100D));
			g.fillRect(px, h - 4, pw, 4);
			Font f2 = g.getFont().deriveFont(12.0F);
			g.setFont(f2);
			g.drawString(s1, ptx, h - th - th2 - 3);
			g.setFont(f);
			g.setColor(Color.BLACK);
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

}

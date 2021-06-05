package ru.lgame.launcher.ui;

import java.awt.Color;
import java.awt.Dimension;
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

	private Modpack modpack;

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
		int factor = greatestCommonFactor(iw, ih);
		Log.debug(id + " image scaled to: " + w + "x" + ITEM_HEIGHT + " (" + iw / factor + ":" + ih / factor + ")");
		image = image.getScaledInstance(w, ITEM_HEIGHT, Image.SCALE_SMOOTH);
		updateContents();
		return this;
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
		int w = getWidth();
		int th = g.getFontMetrics().getHeight();
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
		lastW = w;
	}
	
	private static int greatestCommonFactor(int width, int height) {
	    return (height == 0) ? width : greatestCommonFactor(height, width % height);
	}
	
	/**
	 * Добавляет в текст переносы строки чтобы поместился в окне
	 * @param text Текст
	 * @param width Максимальная ирина
	 * @param font Шрифт
	 */
	private static String[] getStringArray(String text, int width, FontMetrics font) {
		if (text == null || text.length() == 0 || (text.length() == 1 && text.charAt(0) == ' ')) {
			return new String[0];
		}
		text = text.replace('\r', '\0');
		ArrayList<String> v = new ArrayList<String>(5);
		char[] chars = text.toCharArray();
		if (font.stringWidth(text) > width) {
			int i1 = 0;
			for (int i2 = 0; i2 < text.length(); i2++) {
				if (chars[i2] == '\n') {
					v.add(text.substring(i1, i2));
					i2 = i1 = i2 + 1;
				} else {
					if (text.length() - i2 <= 1) {
						v.add(text.substring(i1, text.length()));
						break;
					} else if (font.stringWidth(text.substring(i1, i2)) >= width) {
						boolean f = false;
						for (int j = i2; j > i1; j--) {
							char c = text.charAt(j);
							if (c == ' ' || c == '-' || c == '.') {
								f = true;
								v.add(text.substring(i1, j + 1));
								i2 = i1 = j + 1;
								break;
							}
						}
						if (!f) {
							i2 = i2 - 2;
							v.add(text.substring(i1, i2));
							i2 = i1 = i2 + 1;
						}
					}
				}
			}
		} else {
			v.add(text);
		}
		return v.toArray(new String[0]);
	}

}

package ru.lgame.launcher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

import ru.lgame.launcher.ui.pane.ModpackPanel;
import ru.lgame.launcher.utils.logging.Log;

public class ButtonUI extends BasicButtonUI {

	private static Font font;

	static {
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, ModpackPanel.class.getResourceAsStream("/font/Montserrat-Bold.ttf"))
					.deriveFont(Font.BOLD, 14);
		} catch (Exception e) {
			Log.warn("Font load failed: " + e.toString());
			font = Font.getFont(Font.SANS_SERIF)
					.deriveFont(Font.BOLD, 15);
		}
	}

	public static ComponentUI createUI(JComponent b) {
		return new ButtonUI();
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		AbstractButton button = (AbstractButton) c;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		Color c2 = new Color(0x591896);
		Color c1 = new Color(135, 44, 221);
		if (button.isOpaque()) {
			final int buttonWidth = button.getWidth();
			if (button.getModel().isRollover()) {
				g2d.setColor(c2);
			} else if (button.isEnabled()) {
				g2d.setColor(c1);
			} else {
				g2d.setColor(Color.GRAY);
			}
			g2d.fillRoundRect(0, 0, buttonWidth, button.getHeight(), 10, 10);
		}
		Font f = g.getFont();
		g.setFont(font);
		super.paint(g, button);
		g.setFont(f);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		AbstractButton button = (AbstractButton) c;
		String s = button.getText();
		if(s != null) {
			if(s.equals("...")) return new Dimension(48, 24);
			if(s.length() == 1) return new Dimension(48, 24);
			if(s.equals("Играть")) return new Dimension(100, 24);
			if(s.equals("Обновить")) return new Dimension(110, 24);
			if(s.equals("Аккаунты")) return new Dimension(110, 24);
		}
		int width = Math.max(button.getWidth(), 135);
		int height = Math.max(button.getHeight(), 24);
		return new Dimension(width, height);
	}
}
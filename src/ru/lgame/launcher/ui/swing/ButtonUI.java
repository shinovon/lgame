package ru.lgame.launcher.ui.swing;

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

import static ru.lgame.launcher.ui.Fonts.buttonFont;

/**
 * @author Shinovon
 */
public class ButtonUI extends BasicButtonUI {

	public static ComponentUI createUI(JComponent b) {
		return new ButtonUI();
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		AbstractButton btn = (AbstractButton) c;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		Color c2 = new Color(0x591896);
		Color c1 = new Color(135, 44, 221);
		Font f = buttonFont;
		if (btn.isOpaque()) {
			int bw = btn.getWidth();
			int bh = btn.getHeight();
			if (btn.getModel().isRollover())
				g.setColor(c2);
			else if (btn.isEnabled())
				g.setColor(c1);
			else g.setColor(Color.GRAY);
			g.fillRoundRect(0, 0, bw, bh, 10, 10);
			g.setFont(f);
			int w = g.getFontMetrics().stringWidth(btn.getText());
			int h = g.getFontMetrics().getHeight();
			int x = (bw - w) / 2;
			int y = (bh + h) / 2 - h / 4;
			if (btn.isEnabled())
				g.setColor(Color.WHITE);
			else g.setColor(Color.DARK_GRAY);
			g.drawString(btn.getText(), x, y);
		}
		/*
		Font f = g.getFont();
		g.setFont(buttonFont);
		super.paint(g, btn);
		g.setFont(f);
		*/
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		AbstractButton btn = (AbstractButton) c;
		String s = btn.getText();
		if(s == null || c == null || c.getGraphics() == null) return new Dimension(24, 24);
		if(s.equals("Играть") || s.equals("Обновить") || s.equals("Установить")) return new Dimension(100, 24);
		int width = c.getGraphics().getFontMetrics(buttonFont).stringWidth(s) + 24;
		int height = 24;
		return new Dimension(width, height);
		/*
		if(s != null) {
			if(s.equals("...")) return new Dimension(48, 24);
			if(s.length() == 1) return new Dimension(48, 24);
			if(s.length() == 2) return new Dimension(64, 24);
			if(s.equals("Играть")) return new Dimension(100, 24);
			if(s.equals("Назад")) return new Dimension(80, 24);
			if(s.equals("Обновить")) return new Dimension(110, 24);
			if(s.equals("Аккаунты")) return new Dimension(110, 24);
		}
		int width = Math.max(btn.getWidth(), 135);
		int height = Math.max(btn.getHeight(), 24);
		return new Dimension(width, height);
		*/
	}
}
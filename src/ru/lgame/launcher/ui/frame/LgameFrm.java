package ru.lgame.launcher.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class LgameFrm extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Thread t;
	private int frame;
	private JPanel panel;
	private static final String[] launcherNames = new String[] {
			"Арман Джусупгалиев",
			"Вадим Дробот",
			"Hollow Horizon",
			"Иван Жуков",
			"Александр Кирпичев",
			"Remuru Tempest",
			"Тимур Сагитов",
			"Максим Барахтанов",
			"Никита Косенко"
	};
	private static final String[] lgameNames = new String[] {
			"Вадим Дробот",
			"Hollow Horizon",
			"Тима Постоенко",
			"Арсений Ивлев",
			"Максим Барахтанов",
			"Иван Жуков",
			"Николай Петкун",
			"Арман Джусупгалиев",
			"Антон Григорьев",
			"Артём Косенков",
			"Remuru Tempest",
			"Александр Ясаков",
			"Семен Фокин",
			"Данил Чебукин",
			"Александр Кирпичев",
			"Филипп Немытых",
			"Виталий Морозов",
			"Женя Николаев",
			"Дана Холод",
			"Мария Власова",
			"Михаил Шекера",
			"Шелдон Докучаев",
			"Тимур Сагитов",
			"Александр Мерц",
			"Виктор Франчук",
			"Артур Сырлыбаев",
			"Тимофей Буйневич",
			"Олег Ждамаров",
			"Ягор Лапцеў"
	};
	
	private float[][] s = new float[lgameNames.length][4];
	
	private Random rnd = new Random();

	/**
	 * Create the frame.
	 */
	private float alpha = 1;
	protected int lastNameFrame;
	protected int index;
	protected int part;
	protected float[][] s2;
	
	public LgameFrm() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		final int nt = 20;
		final int p2 = (70 + (nt * lgameNames.length)) + 80;
		final int p3 = (70 + (nt * lgameNames.length) + (nt * launcherNames.length)) + 80 + 80 + 70 + 40;
		//System.out.println(p2);
		//System.out.println(p3);
		panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				int w = getWidth();
				int h = getHeight();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, w, h);
				g.setColor(Color.WHITE);
				//g.drawString("" + frame, 0, 20);
				if(part == 0) {
					if(frame > 50) {
						if(alpha > 0) alpha-=8;
					} else {
						if(alpha < 255) alpha+=8;
					}
					if(alpha > 255) alpha = 255;
					if(alpha < 0) alpha = 0;
					g.setColor(new Color(255, 255, 255, (int)alpha));
					g.drawString("LGame Studio Team", 155, 140);
				} else if(part == 1) {
					if(frame > p2) {
						if(frame > p2 + 50) {
							if(alpha > 0) alpha-=8;
						} else {
							if(alpha < 255) alpha+=8;
						}
						if(alpha > 255) alpha = 255;
						if(alpha < 0) alpha = 0;
						g.setColor(new Color(255, 255, 255, (int)alpha));
						g.drawString("LGame Launcher Team", 120, 140);
					}
				}
				if(part == 1) {
					if(frame > p2 + 80 && frame - lastNameFrame > nt ) {
						lastNameFrame = frame;
						float[] f = randomPos();
						s2[index] = f;
						index++;
					}
				} else if(part == 0)
				if(frame > 80 && frame - lastNameFrame > nt ) {
					lastNameFrame = frame;
					float[] f = randomPos();
					s[index] = f;
					index++;
				} else if(part == 2) {
					if(frame > p3) {
						setVisible(false);
					}
				}
				for (int i = 0; i < s.length; i++) {
					float[] ff = s[i];

					float dir = ff[3] > 0 ? -1 : 1;
					if(s[i][2] > 0.02f) {
						g.setColor(new Color(255, 255, 255, (int)(ff[2]*255)));
						g.drawString(lgameNames[i], (int)ff[0], (int)ff[1]);
						s[i][0] += dir * 2f;
						s[i][2] *= 0.971f;
					}
				}
				if(s2 != null)
				for (int i = 0; i < s2.length; i++) {
					float[] ff = s2[i];

					float dir = ff[3] > 0 ? -1 : 1;
					if(s2[i][2] > 0.02f) {
						g.setColor(new Color(255, 255, 255, (int)(ff[2]*255)));
						g.drawString(launcherNames[i], (int)ff[0], (int)ff[1]);
						s2[i][0] += dir * 2f;
						s2[i][2] *= 0.971f;
					}
				}
				if(part == 0 && index == lgameNames.length) {
					part = 1;
					s2 = new float[launcherNames.length][4];
					index = 0;
				}
				if(part == 1 && index == launcherNames.length) {
					part = 2;
					index = 0;
				}
				frame++;
			}
		};
		t = new Thread() {
			public void run() {
				try {
					while(isVisible()) {
						panel.repaint();
						Thread.sleep(1000 / 30);
					}
				} catch (Exception e) {
				}
			}
		};
		contentPane.add(panel, BorderLayout.CENTER);
		contentPane.setPreferredSize(new Dimension(400, 300));
		pack();
		setLocationRelativeTo(null);
	}
	
	private float[] randomPos() {
		int x = 50 + (rnd.nextInt(100) * 2);
		int y = 40 + (rnd.nextInt(120) * 2);
		return new float[] { x, y, 1, x > 180 ? 1 : 0 };
	}
	
	public void setVisible(boolean b) {
		super.setVisible(b);
		if(b == true) start();
		else stop();
	}
	
	public void start() {
		if(t.isAlive()) return;
		t.start();
	}
	
	public void stop() {
		t.interrupt();
	}

}

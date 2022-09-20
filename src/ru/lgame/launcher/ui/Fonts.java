package ru.lgame.launcher.ui;

import java.awt.Font;

import ru.lgame.launcher.ui.pane.MiniModpackPane;
import ru.lgame.launcher.utils.logging.Log;

public class Fonts {

	public static Font buttonFont;
	public static Font modpackDesc;
	public static Font modpackCategory;
	public static Font modpackTitle;
	public static Font modpackState;
	public static Font modpackUpdateInfo;
	public static Font label;
	public static Font username;
	public static Font loading;

	static {
		buttonFont = createFontBold("Montserrat-Bold", 13.5F);
		modpackDesc = createFont("Montserrat-Regular", 15);
		modpackCategory = createFont("Montserrat-Regular", 14);
		modpackTitle = createFontBold("Montserrat-Bold", 18);
		modpackState = createFontBold("Montserrat-Bold", 14);
		modpackUpdateInfo = createFont("Montserrat-Regular", 12);
		username = createFont("Montserrat-Regular", 11.5f);
		loading = createFont("Montserrat-Regular", 12);
		//label = createFont(11);
	}

	private static Font createFontBold(String font, float f) {
		return createFont(font, 1, f);
	}

	private static Font createFont(String font, float f) {
		return createFont(font, 0, f);
	}

	private static Font createFont(String font, int style, float size) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, MiniModpackPane.class.getResourceAsStream("/font/" + font + ".ttf"))
					.deriveFont(style, size);
		} catch (Exception e) {
			Log.warn("Font load failed: " + e.toString());
		}
		return new Font(Font.SANS_SERIF, 0, (int) size);
	}
}

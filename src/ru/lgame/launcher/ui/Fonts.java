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
		buttonFont = createFontBold(13.5F);
		modpackDesc = createFont(15);
		modpackCategory = createFont(14);
		modpackTitle = createFontBold(18);
		modpackState = createFontBold(14);
		modpackUpdateInfo = createFont(12);
		username = createFont(11.5f);
		loading = createFont(12);
		//label = createFont(11);
	}

	private static Font createFontBold(float f) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, MiniModpackPane.class.getResourceAsStream("/font/Montserrat-Bold.ttf"))
					.deriveFont(Font.BOLD, f);
		} catch (Exception e) {
			Log.warn("Font load failed: " + e.toString());
		}
		return new Font(Font.SANS_SERIF, Font.BOLD, (int) f);
	}

	private static Font createFont(float f) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, MiniModpackPane.class.getResourceAsStream("/font/Montserrat-Regular.ttf"))
					.deriveFont(0, f);
		} catch (Exception e) {
			Log.warn("Font load failed: " + e.toString());
		}
		return new Font(Font.SANS_SERIF, 0, (int) f);
	}
}

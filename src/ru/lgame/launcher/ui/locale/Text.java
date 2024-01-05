package ru.lgame.launcher.ui.locale;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import ru.lgame.launcher.utils.logging.Log;

/**
 * Локализация
 * @author Shinovon
 */
public class Text {
	
	private static Properties props;
	
	static {
		try {
			props = new Properties();
			props.load(new InputStreamReader(Text.class.getResourceAsStream("/lang/ru.lang"), "UTF-8"));
		} catch (IOException e) {
			Log.error("Locale load failed", e);
		}
	}

	public static String get(String key, String def) {
		try {
			if(props.containsKey(key)) {
				return (String) props.get(key);
			}
		} catch (Exception e) {
		}
		return def;
	}

	public static String get(String key) {
		return get(key, key);
	}

}

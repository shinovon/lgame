package ru.lgame.launcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import ru.lgame.launcher.utils.Log;

/**
 * Конфиг
 * @author Shinovon
 */
public class Config {

	private static Properties properties = new Properties();
	
	public static void init() {
		loadDefaults();
		loadConfig();
		saveConfig();
	}

	public static void loadDefaults() {
		set("path", Launcher.getModpacksDefaultDir());
		set("username", "");
	}

	public static void loadConfig() {
		try {
			properties.load(new FileInputStream(Launcher.getConfigPath()));
		} catch (IOException e) {
			Log.error("Failed to load properties", e);
		}
	}

	public static void set(String key, boolean value) {
		set(key, "" + value);
	}

	public static void set(String key, int value) {
		set(key, "" + value);
	}

	public static void set(String key, long value) {
		set(key, "" + value);
	}

	public static void set(String key, String value) {
		properties.setProperty(key, value);
	}

	public static boolean getBoolean(String key) {
		if(properties.containsKey(key)) return Boolean.parseBoolean((String) properties.get(key));
		else return false;
	}

	public static int getInt(String key) {
		if(properties.containsKey(key)) return Integer.parseInt((String) properties.get(key));
		else return -1;
	}

	public static long getLong(String key) {
		if(properties.containsKey(key)) return Long.parseLong((String) properties.get(key));
		else return 0;
	}

	public static String get(String key) {
		if(properties.containsKey(key)) return (String) properties.get(key);
		else return null;
	}

	public static boolean contains(String key) {
		return properties.containsKey(key);
	}

	@SuppressWarnings("deprecation")
	public static void saveConfig() {
		try {
			properties.save(new FileOutputStream(Launcher.getConfigPath()), "LGame Launcher Configuration file");
		} catch (IOException e) {
			Log.error("Failed to save properties", e);
		}
	}

}

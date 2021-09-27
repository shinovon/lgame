package ru.lgame.launcher.locale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;

/**
 * Локализация (будет)
 * @author Shinovon
 */
public class Text {
	
	private static Hashtable<String, String> table;
	
	static {
		table = new Hashtable<String, String>();
		try {
			InputStream is = Text.class.getResourceAsStream("/lang/ru.lang");
			Reader r = new InputStreamReader(is, "UTF-8");
			char[] chars = new char[65535];
			int slen = r.read(chars);
			r.close();
			is.close();
			String x = "";
			boolean iscomment = false;
			for (int i = 0; i < slen; i++) {
				final char c = chars[i];
	
				if (c == 0) {
					break;
				}
	
				if (x.length() == 0 && c == '#') {
					iscomment = true;
				}
	
				if (c == '\n') {
					if (!iscomment && x != null && x.length() > 2) {
						int splitLoc = x.indexOf("=");
						int len = x.length();
						String key = x.substring(0, splitLoc);
						String val = x.substring(splitLoc + 1, len).replace("\r", "").replace("|", "\n");
						table.put(key, val);
					}
					iscomment = false;
					x = "";
				} else {
					x += String.valueOf(c);
				}
			}
			x = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String get(String key, String def) {
		try {
			if(table.containsKey(key)) {
				return (String) table.get(key);
			}
		} catch (Exception e) {
		}
		return def;
	}

	public static String get(String key) {
		try {
			if(table.containsKey(key)) {
				return (String) table.get(key);
			}
		} catch (Exception e) {
		}
		return "null";
	}

}

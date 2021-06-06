package ru.lgame.launcher;

import java.io.File;

public class Updater {

	/**
	 * 
	 * @param m Объект сборки
	 * @return 0 - не установлена, 1 - можно играть, 2 - есть обновление, 3 - требуется обновление, отрицательное значение - ошибка
	 */
	public static int getModpackState(Modpack m) {
		if(!checkInstalled(m)) return 0;
		if(!checkModpackIntegrity(m)) return 3;
		if(checkUpdatesAvailable(m)) return 2;
		return 1;
	}

	/**
	 * Проверить установку сборки
	 * @param m Объект сборки
	 */
	public static boolean checkInstalled(Modpack m) {
		String s = Config.get("path") + m.id() + File.separator;
		File dir = new File(s);
		if(!dir.exists()) return false;
		File f = new File(s + "build");
		if(!f.exists()) return false;
		return true;
	}
	
	/**
	 * Проверить целостность сборки
	 * @param m Объект сборки
	 */
	public static boolean checkModpackIntegrity(Modpack m) {
		return false;
	}

	/**
	 * Проверить наличие обновлений
	 * @param m Объект сборки
	 */
	public static boolean checkUpdatesAvailable(Modpack m) {
		return false;
	}

	public static Modpack getNowUpdatingModpack() {
		return null;
	}

}

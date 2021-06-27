package ru.lgame.launcher;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.Log;

public final class Updater implements Runnable {
	
	private static Updater currentInst;

	private static Thread currentThread;

	private boolean running;
	
	private Modpack modpack;
	private Auth auth;
	
	private JSONObject json;
	private JSONObject clientJson;

	private boolean forceUpdate;

	private boolean updating;

	private boolean failed;

	 
	private Updater(Modpack m, Auth a, boolean b) {
		this.modpack = m;
		this.auth = a;
		this.forceUpdate = b;
	}
	
	static void start(Modpack m, Auth a) {
		if(currentThread != null && currentThread.isAlive()) return;
		currentThread = new Thread(new Updater(m, a, false));
		currentThread.start();
	}
	
	static void startForceUpdate(Modpack m, Auth a) {
		if(currentThread != null && currentThread.isAlive()) return;
		currentThread = new Thread(new Updater(m, a, true));
		currentThread.start();
	}

	/**
	 * 
	 * @param m Объект сборки
	 * @return 0 - не установлена, 1 - можно играть, 2 - есть обновление, 3 - требуется обновление, отрицательное значение - ошибка
	 */
	public static int getModpackState(Modpack m) {
		if(!checkInstalled(m)) return 0;
		try {
			if(!checkModpackIntegrity(m)) return 3;
			if(checkUpdatesAvailable(m)) return 2;
		} catch (FileNotFoundException e) {
			return -2;
		} catch (IOException e) {
			return -1;
		} catch (Exception e) {
			return -3;
		}
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
		File f = new File(s + "version");
		if(!f.exists()) return false;
		return true;
	}
	
	/**
	 * Проверить целостность сборки
	 * @param m Объект сборки
	 * @throws Exception 
	 */
	public static boolean checkModpackIntegrity(Modpack m) throws Exception {
		return checkModpackIntegrity(m, m.getUpdateJson());
	}
	
	/**
	 * Проверить целостность сборки
	 * @param m Объект сборки
	 * @throws Exception 
	 */
	public static boolean checkModpackIntegrity(Modpack m, JSONObject updateJson) throws Exception {
		String p = Config.get("path") + m.id() + File.separator;
		File vvf = new File(p + "version");
		if(!vvf.exists()) return false;
		JSONObject j = updateJson.getJSONObject("integrity_check");
		JSONArray a = j.getJSONArray("dirs");
		Iterator<Object> i = a.iterator();
		while(i.hasNext()) {
			JSONObject o = (JSONObject) i.next();
			if(o.getString("type").equals("exists")) {
				String d = path(o.getString("p"));
				if(!new File(p + d).exists()) return false;
			}
		}
		if(updateJson.getBoolean("has_mods")) {
			JSONObject md = updateJson.getJSONObject("mods");
			boolean ignoreExcessMods = md.getBoolean("ignore_excess_mods");
			boolean removeAll = md.getBoolean("remove_all_if_check_failed");
			boolean deleteBlacklisted = md.getBoolean("delete_blacklist_mods");
			String digest = updateJson.getString("checksum_algorithm");
			ArrayList<String> names = new ArrayList<String>();
			JSONObject ck = md.getJSONObject("checksums");
			for (String s : ck.keySet()) {
				names.add(s);
				String hash = ck.getString(s);
				String filep = p + "mods" + File.separator + s;
				File file = new File(filep);
				if (!file.exists()) {
					Log.info("not exists: " + s);
					if(removeAll) deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
					return false;
				}
				if (hash(filep, digest).equalsIgnoreCase(hash)) {
					if(removeAll) deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
					Log.info("wrong checksum: " + s);
					return false;
				}
			}
			String[] blacklist = updateJson.getJSONArray("words_blacklist").toList().toArray(new String[0]);
			FileFilter filter = (file) -> !file.isDirectory() && file.getName().toLowerCase().endsWith(".jar");
			File[] mods = new File(p + "mods" + File.separator).listFiles(filter);
			if(mods != null)
			for (File f : mods) {
				String n = f.getName().toLowerCase();
				for(String s: blacklist) if(n.contains(s.toLowerCase())) {
					Log.info("blacklisted mod: " + n);
					if(deleteBlacklisted) f.delete();
					else {
						if(removeAll) deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
						return false;
					}
				}
				if(!ignoreExcessMods && !names.contains(n)) {
					Log.info("Excess mod: " + n);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Проверить наличие обновлений
	 * @param m Объект сборки
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public static boolean checkUpdatesAvailable(Modpack m) throws JSONException, IOException {
		return checkUpdatesAvailable(m, m.getUpdateJson());
	}

	private boolean checkClientNeedUpdate() throws Exception {
		String p = Config.get("path") + modpack.client() + File.separator;
		File f = new File(p + "client.jar");
		if(!f.exists()) return true;
		f = new File(p + "version");
		if(!f.exists()) return true;
		try {
			if(!FileUtils.getString(f).equalsIgnoreCase("" + clientJson.getInt("update_build"))) return true;
		} catch (Exception e) {
		}
		return !checkClientIntegrity();
	}

	private boolean checkClientIntegrity() throws Exception {
		String p = Config.get("path") + modpack.client() + File.separator;
		File vvf = new File(p + "client.jar");
		if(!vvf.exists()) return true;
		vvf = new File(p + "version");
		if(!vvf.exists()) return false;
		JSONObject j = clientJson.getJSONObject("integrity_check");
		JSONArray a = j.getJSONArray("dirs");
		Iterator<Object> i = a.iterator();
		while(i.hasNext()) {
			JSONObject o = (JSONObject) i.next();
			if(o.getString("type").equals("exists")) {
				String d = path(o.getString("p"));
				if(!new File(p + d).exists()) return false;
			}
		}

		if(clientJson.has("libraries")) {
			JSONObject md = clientJson.getJSONObject("libraries");
			String digest = clientJson.getString("checksum_algorithm");
			ArrayList<String> names = new ArrayList<String>();
			JSONObject ck = md.getJSONObject("checksums");
			for (String s : ck.keySet()) {
				names.add(s);
				String hash = ck.getString(s);
				String filep = p + "libraries" + File.separator + s;
				File file = new File(filep);
				if (!file.exists()) {
					Log.info("not exists: " + s);
					deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
					return false;
				}
				if (hash(filep, digest).equalsIgnoreCase(hash)) {
					deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
					Log.info("wrong checksum: " + s);
					return false;
				}
			}
			FileFilter filter = (file) -> !file.isDirectory() && file.getName().toLowerCase().endsWith(".jar");
			File[] mods = new File(p + "libraries" + File.separator).listFiles(filter);
			if(mods != null)
			for (File f : mods) {
				String n = f.getName().toLowerCase();
				if(!names.contains(n)) {
					Log.info("Excess mod: " + n);
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkClientInstalled() {
		String p = Config.get("path") + modpack.client() + File.separator;
		File f = new File(p + "client.jar");
		if(!f.exists()) return false;
		f = new File(p + "version");
		if(!f.exists()) return false;
		return true;
	}


	/**
	 * Проверить наличие обновлений
	 * @param m Объект сборки
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public static boolean checkUpdatesAvailable(Modpack m, JSONObject updateJson) throws JSONException, IOException {
		String p = Config.get("path") + m.id() + File.separator;
		File f = new File(p + "version");
		if(!f.exists()) return true;
		if(!FileUtils.getString(f).equalsIgnoreCase("" + updateJson.getInt("update_build"))) return true;
		return false;
	}

	public static Modpack getNowUpdatingModpack() {
		if(currentInst == null) return null;
		if(currentInst.running && currentInst.updating) return currentInst.modpack;
		return null;
	}

	public void run() {
		if(running) throw new IllegalStateException("Already running!");
		running = true;
		int modpackState = forceUpdate ? 3 : checkInstalled(modpack) ? -100 : 0;
		try {
			json = modpack.getUpdateJson(true);
			clientJson = modpack.getClientUpdateJson(true);
		} catch (IOException e) {
			e.printStackTrace();
			if(modpackState == 0 || forceUpdate) {
				fatalError("Нет подключения к интернету или сервер не отвечает!", e, Errors.UPDATER_RUN_GETUPDATEJSON_IOEXCEPTION);
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			if(modpackState == 0 || forceUpdate) {
				fatalError("Ошибка парса", e, Errors.UPDATER_RUN_GETUPDATEJSON_JSONEXCEPTION);
				return;
			}
		}
		//boolean packInstalled = modpackState != 0;
		boolean clientInstalled = checkClientInstalled();
		boolean clientNeedsUpdate = false;
		try {
			clientNeedsUpdate = checkClientNeedUpdate();
		} catch (Exception e1) {
			fatalError("Ошибка проверки клиента", e1, Errors.UPDATER_RUN_CHECKCLIENT_EXCEPTION);
		}
		// Сборка установлена, нужно проверить обновления
		if(modpackState == -100) {
			modpackState = 1;
			if(!clientInstalled) modpackState = 4;
			else if(clientNeedsUpdate) modpackState = 5;
			try {
				if(!checkModpackIntegrity(modpack, json)) modpackState = 3;
				else if(checkUpdatesAvailable(modpack)) modpackState = 2;
			} catch (Exception e) {
				fatalError("Ошибка проверки сборки", e, Errors.UPDATER_RUN_CHECKMODPACK_EXCEPTION);
				return;
			}
		}
		// обнова либо старт сборки
		boolean ret = true;
		switch(modpackState) {
		case 0:
		{
			if(!clientInstalled) installClient();
			else if(clientNeedsUpdate) updateClient();
			install();
			break;
		}
		case 1:
		{
			startClient();
			ret = false;
			break;
		}
		case 2:
		case 3:
		{
			if(!clientInstalled) installClient();
			else if(clientNeedsUpdate) updateClient();
			update();
			break;
		}
		case 4: {
			installClient();
			break;
		}
		case 5: {
			updateClient();
			break;
		}
		default:
		{
			fatalError("default", modpackState, Errors.UPDATER_GETMODPACKSTATE_ILLEGAL_VALUE);
			ret = false;
			break;
		}
		}
		if(ret && !failed) {
			Log.info("Updater repeat");
		}
	}

	private void updateClient() {
		updating = true;
	}

	private void installClient() {
		updating = true;
		
	}

	private void update() {
		updating = true;
		String p = Config.get("path") + modpack.id() + File.separator;
		
	}
	
	private void install() {
		updating = true;
		String p = Config.get("path") + modpack.id() + File.separator;
		
	}

	private void startClient() {
		
	}

	private void reset() {
		forceUpdate = false;
		running = false;
		failed = false;
	}

	private void fatalError(String s, Throwable t, int i) {
		String e = Log.exceptionToString(t);
		Launcher.inst.showError("Ошибка обновления", s + " (Код ошибки: "  + Errors.toHexString(i) + ")", s + " (Код ошибки: " + Errors.toHexString(i) + ")\n" + e);	
		fatalError();
	}

	private void fatalError(String s, String s2, int i) {
		Launcher.inst.showError("Ошибка обновления", s, s + ": " + s2 + " (Код ошибки: " +  Errors.toHexString(i) + ")");
		fatalError();
	}

	private void fatalError(String s, int i1, int i2) {
		String e = Log.exceptionToString(new Exception());
		Launcher.inst.showError("Ошибка обновления", s, s + ": " + Errors.toHexString(i1) + " (Код ошибки: " + Errors.toHexString(i2) + ")\n" + e);
		fatalError();
	}

	private void fatalError(String s, String s2) {
		Launcher.inst.showError("Ошибка обновления", s, s2);
		fatalError();
	}

	private void fatalError(String s) {
		Log.error(s);
		String e = Log.exceptionToString(new Exception());
		Launcher.inst.showError("Ошибка обновления", s, e);
		fatalError();
	}
	
	private void fatalError() {
		updating = false;
		forceUpdate = false;
		running = false;
		failed = true;
	}

	private static String path(String s) {
		return s.replace("/", File.separator);
	}

	public static String hash(String f, String digest) throws IOException, NoSuchAlgorithmException {
		File file = new File(f);
		MessageDigest shaDigest = MessageDigest.getInstance(digest);
		String shaChecksum = getFileChecksum(shaDigest, file);
		return shaChecksum;
	}
	
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}

		fis.close();
		byte[] bytes = digest.digest();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	private static void deleteDirectoryRecursion(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursion(entry);
				}
			}
		}
		Files.delete(path);
	}

}

package ru.lgame.launcher;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.ZipException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.ui.ErrorUI;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.LauncherOfflineException;
import ru.lgame.launcher.utils.StartUtil;
import ru.lgame.launcher.utils.ZipUtils;
import ru.lgame.launcher.utils.logging.ClientLog;
import ru.lgame.launcher.utils.logging.InputStreamCopier;
import ru.lgame.launcher.utils.logging.Log;
import ru.lgame.launcher.utils.WebUtils;

public final class Updater implements Runnable, ZipUtils.ProgressListener, WebUtils.ProgressListener {
	
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

	private String clientMainClass;
	private String[] clientTweakClasses;
	private String clientAssetIndex;

	private String currentUnzipFile;

	private boolean repeated;

	public boolean clientStarted;

	private int tasksDone;

	private int totalTasks;

	private boolean offline;

	private JSONObject clientStartJson;

	private float avgspeed;

	private int downloadFailCount;
	
	private static Process clientProcess;

	 
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
			//if(!checkModpackIntegrity(m)) return 3;
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
		if(updateJson == null) throw new LauncherOfflineException();
		JSONObject j = updateJson.getJSONObject("integrity_check");
		if(j.has("dirs")) {
			JSONArray a = j.getJSONArray("dirs");
			Iterator<Object> i = a.iterator();
			while(i.hasNext()) {
				JSONObject o = (JSONObject) i.next();
				if(o.getString("type").equals("exists")) {
					String d = path(o.getString("path"));
					if(!new File(p + d).exists()) return false;
				}
			}
		}
		if(updateJson.getBoolean("has_mods")) {
			return checkMods(m, p, j);
		}
		return true;
	}
	
	/**
	 * Проверка модов
	 */
	public static boolean checkMods(Modpack m, String p, JSONObject integrityCheck) throws Exception {
		JSONObject md = integrityCheck.getJSONObject("mods");
		boolean ignoreExcessMods = md.getBoolean("ignore_excess_mods");
		boolean removeAll = md.getBoolean("remove_all_if_check_failed");
		boolean deleteBlacklisted = md.getBoolean("delete_blacklist_mods");
		String digest = md.getString("checksum_algorithm");
		ArrayList<String> names = new ArrayList<String>();
		JSONObject ck = md.getJSONObject("checksums");
		for (String s : ck.keySet()) {
			names.add(s);
			String hash = ck.getString(s);
			String filep = p + "mods" + File.separator + s;
			File file = new File(filep);
			if (!file.exists()) {
				Log.info("not exists: " + file);
				if(removeAll) {
					try {
						deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
					} catch (Exception e) {
					}
				}
				return false;
			}
			try {
				if (!hash(filep, digest).equalsIgnoreCase(hash)) {
					Log.info("wrong checksum: " + s);
					if(removeAll) deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
					else {
						try {
							new File(filep).delete();
						} catch (Exception e) {
						}
					}
					return false;
				}
			} catch (IOException e) {
				Log.error("file error", e);
				return false;
			}
		}
		String[] blacklist = md.getJSONArray("words_blacklist").toList().toArray(new String[0]);
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
				if(removeAll) deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
				else try {
					new File(p + n).delete();
				} catch (Exception e) {
				}
				return false;
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
		if(clientJson == null) throw new LauncherOfflineException();
		try {
			if(!FileUtils.getString(f).equalsIgnoreCase("" + clientJson.getInt("update_build"))) return true;
		} catch (Exception e) {
		}
		return !checkClientIntegrity();
	}


	/**
	 * Проверка целостности клиента
	 */
	private boolean checkClientIntegrity() throws Exception {
		String p = Config.get("path") + modpack.client() + File.separator;
		File vvf = new File(p + "client.jar");
		if(!vvf.exists()) return true;
		vvf = new File(p + "version");
		if(!vvf.exists()) return false;
		JSONObject j = clientJson.getJSONObject("integrity_check");
		if(j.has("dirs")) {
			JSONArray a = j.getJSONArray("dirs");
			Iterator<Object> i = a.iterator();
			while(i.hasNext()) {
				JSONObject o = (JSONObject) i.next();
				if(o.getString("type").equals("exists")) {
					String d = path(o.getString("path"));
					if(!new File(p + d).exists()) return false;
				}
			}
		}
		
		if(clientJson.has("libraries")) {
			if(!checkClientLibraries(p)) return false;
		}

		if(clientJson.has("natives")) {
			return checkClientNatives(p);
		}
		return true;
	}

	/**
	 * Проверка библиотек клиента
	 */
	private boolean checkClientLibraries(String p) throws Exception {
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
			if (!hash(filep, digest).equalsIgnoreCase(hash)) {
				deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
				Log.info("wrong checksum: " + s);
				return false;
			}
		}
		FileFilter filter = (file) -> file.isDirectory() || file.getName().toLowerCase().endsWith(".jar");
		File[] mods = new File(p + "libraries" + File.separator).listFiles(filter);
		if(mods != null)
		for (File f : mods) {
			if(f.isDirectory()) continue;
			String n = f.getName().toLowerCase();
			if(!names.contains(n)) {
				Log.info("Excess jar libary: " + n);
				return false;
			}
		}
		return true;
	}

	/**
	 * Проверка нативных библиотек клиента
	 */
	private boolean checkClientNatives(String p) throws Exception {
		JSONObject md = clientJson.getJSONObject("natives");
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
				deleteDirectoryRecursion(Paths.get(p + "natives" + File.separator));
				return false;
			}
			if (!hash(filep, digest).equalsIgnoreCase(hash)) {
				deleteDirectoryRecursion(Paths.get(p + "natives" + File.separator));
				Log.info("wrong checksum: " + s);
				return false;
			}
		}
		FileFilter filter = (file) -> file.isDirectory() || (file.getName().toLowerCase().endsWith(".dll") || file.getName().toLowerCase().endsWith(".so"));
		File[] mods = new File(p + "natives" + File.separator).listFiles(filter);
		if(mods != null)
		for (File f : mods) {
			if(f.isDirectory()) continue;
			String n = f.getName().toLowerCase();
			if(!names.contains(n)) {
				Log.info("Excess dynamic libary: " + n);
				return false;
			}
		}
		return false;
	}

	private boolean checkClientInstalled() {
		String p = getClientDir();
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

	public static Modpack getNowRunningModpack() {
		if(currentInst == null) return null;
		if(currentInst.running && currentInst.clientStarted) return currentInst.modpack;
		return null;
	}

	public void run() {
		if(running) throw new IllegalStateException("Already running!");
		currentInst = this;
		running = true;
		updating = true;
		modpack.setUpdateInfo(repeated ? "Обновление" : "Сбор информации", repeated ? "Проверка правильности установки" : "Проверка установки", 0);
		int modpackState = forceUpdate ? 3 : checkInstalled(modpack) ? -100 : 0;
		try {
			modpack.setUpdateInfo(null, "Получение дескриптора запуска клиента", 15);
			clientStartJson = modpack.getClientStartJson(modpackState == 0 || forceUpdate);
			if(clientStartJson == null) {
				if(modpackState == 0 || forceUpdate)
					updateFatalError("Нет подключения к интернету или сервер не отвечает!", 0, Errors.UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION);
				else
					updateFatalError("Файл дескриптора запуска клиента отсутсвует! Без него игра в оффлайн режиме невозможна!", 0, Errors.UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION);
				return;
			}
			clientAssetIndex = clientStartJson.getString("asset_index");
			clientMainClass = clientStartJson.getString("mainclass");
			clientTweakClasses = clientStartJson.getJSONArray("tweak_classes").toList().toArray(new String[0]);
			modpack.setUpdateInfo(null, "Скачивание конфигурации сборки", 33);
			json = modpack.getUpdateJson(true);
			modpack.setUpdateInfo(null, "Скачивание конфигурации клиента", 67);
			clientJson = modpack.getClientUpdateJson(true);
		} catch (LauncherOfflineException e) {
			if(modpackState == 0 || forceUpdate) {
				updateFatalError("Нет подключения к интернету или сервер не отвечает!", e.getCause(), Errors.UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION);
				return;
			}
		} catch (IOException e) {
			if(modpackState == 0 || forceUpdate) {
				updateFatalError("Нет подключения к интернету или сервер не отвечает!", e, Errors.UPDATER_RUN_GETUPDATEJSON_IOEXCEPTION);
				return;
			}
		} catch (JSONException e) {
			if(modpackState == 0 || forceUpdate) {
				updateFatalError("Ошибка парса", e, Errors.UPDATER_RUN_GETUPDATEJSON_JSONEXCEPTION);
				return;
			}
		}
		modpack.setUpdateInfo(null, null, 100);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e2) {
			interrupted();
			return;
		}
		modpack.setUpdateInfo("Проверка установки", "Проверка целостности клиента", 0);
		//boolean packInstalled = modpackState != 0;
		boolean clientInstalled = checkClientInstalled();
		modpack.setUpdateInfo(null, "Проверка наличия обновлений клиента", 25);
		boolean clientNeedsUpdate = false;
		try {
			clientNeedsUpdate = checkClientNeedUpdate();
		} catch (LauncherOfflineException e) {
			offline = true;
		} catch (Exception e1) {
			updateFatalError("Ошибка проверки клиента", e1, Errors.UPDATER_RUN_CHECKCLIENT_EXCEPTION);
			return;
		}
		modpack.setUpdateInfo(null, "Проверка целостности сборки", 50);
		//сбросить кэшированное состояние сборки
		modpack.getStateRst();
		if(modpackState == -100) {
			// Сборка установлена, нужно проверить обновления
			modpackState = 1;
			if(!clientInstalled) modpackState = 4;
			else if(clientNeedsUpdate) modpackState = 5;
			try {
				if(!checkModpackIntegrity(modpack, json)) modpackState = 3;
				else {
					modpack.setUpdateInfo(null, "Проверка наличия обновлений сборки", 75);
					if(checkUpdatesAvailable(modpack)) modpackState = 2;
				}
			} catch (LauncherOfflineException e) {
				offline = true;
			} catch (Exception e) {
				updateFatalError("Ошибка проверки сборки", e, Errors.UPDATER_RUN_CHECKMODPACK_EXCEPTION);
				return;
			}
		}
		modpack.setUpdateInfo(null, null, 100);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e2) {
			interrupted();
			return;
		}
		Log.debug("install state: " + modpackState);
		modpack.setUpdateInfo("Обновление", "Инициализация системы обновления", 0);
		// обнова либо старт сборки
		boolean ret = !repeated;
		switch(modpackState) {
		case 0:
		{
			modpack.setUpdateInfo("Обновление", "Обновление клиента", 0);
			if(!clientInstalled) installClient();
			else if(clientNeedsUpdate) updateClient();
			modpack.setUpdateInfo("Обновление", "Установка сборки", -2);
			install();
			break;
		}
		case 1:
		{
			modpack.setUpdateInfo("", "Запуск клиента", 100);
			try {
				startClient();
			} catch (Exception e) {
				clientError("Запуск не удался", e);
				return;
			}
			ret = false;
			break;
		}
		case 2:
		case 3:
		{
			modpack.setUpdateInfo("Обновление", "Обновление клиента", 0);
			if(!clientInstalled) installClient();
			else if(clientNeedsUpdate) updateClient();
			modpack.setUpdateInfo("Обновление", "Обновление сборки", -2);
			update();
			break;
		}
		case 4: {
			modpack.setUpdateInfo("Обновление", "Установка клиента", 0);
			installClient();
			break;
		}
		case 5: {
			modpack.setUpdateInfo("Обновление", "Обновление клиента", 0);
			updateClient();
			break;
		}
		default:
		{
			modpack.setUpdateInfo("", "Обработка ошибки", -2);
			updateFatalError("default", modpackState, Errors.UPDATER_GETMODPACKSTATE_ILLEGAL_VALUE);
			ret = false;
			break;
		}
		}
		if(ret && !failed) {
			modpack.setUpdateInfo("Обновление", "Повторная инициализация системы обновления", 100);
			Log.debug("Updater repeat");
			reset();
			repeated = true;
			run();
			return;
		}
		reset();
	}

	private void updateClient() {
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getClientDir();
		String t = Launcher.getTempDir();
		JSONObject script = clientJson.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("update_script");
		JSONArray download = script.getJSONArray("download");
		JSONArray unzip = script.getJSONArray("unzip");
		JSONArray preremove = script.optJSONArray("pre_remove_dirs");
		JSONArray post = script.optJSONArray("post_update");
		totalTasks += download.length();
		totalTasks += unzip.length();
		if(preremove == null || scriptedRemoveDirs(preremove, p, t)) {
			if(download == null || scriptedDownload(download, p, t)) {
				if(unzip == null || scriptedUnzip(unzip, p, t)) {
					if(post != null) scriptedPost(post, p, t);
					clientVersionFile();
					return;
				} else {
					updateFatalError();
					return;
				}
			} else {
				updateFatalError();
				return;
			}
		}
	}

	private void installClient() {
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getClientDir();
		File pf = new File(p);
		if(!pf.exists()) pf.mkdirs();
		String t = Launcher.getTempDir();
		JSONObject script = clientJson.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("install_script");
		JSONArray download = script.getJSONArray("download");
		JSONArray unzip = script.getJSONArray("unzip");
		JSONArray post = script.optJSONArray("post_install");
		totalTasks += download.length();
		totalTasks += unzip.length();
		if(download == null || scriptedDownload(download, p, t)) {
			if(unzip == null || scriptedUnzip(unzip, p, t)) {
				if(post != null) scriptedPost(post, p, t);
				clientVersionFile();
			} else updateFatalError();
		} else updateFatalError();
	}

	private void update() {
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getModpackDir();
		String t = Launcher.getTempDir();
		JSONObject script = json.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("update_script");
		JSONArray download = script.getJSONArray("download");
		JSONArray unzip = script.getJSONArray("unzip");
		JSONArray preremove = script.optJSONArray("pre_remove_dirs");
		JSONArray post = script.optJSONArray("post_update");
		totalTasks += download.length();
		totalTasks += unzip.length();
		if(preremove == null || scriptedRemoveDirs(preremove, p, t)) {
			if(download == null || scriptedDownload(download, p, t)) {
				if(unzip == null || scriptedUnzip(unzip, p, t)) {
					if(post != null) scriptedPost(post, p, t);
					modpackVersionFile();
				} else updateFatalError();
			} else updateFatalError();
		}
	}
	
	private void install() {
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getModpackDir();
		File pf = new File(p);
		if(!pf.exists()) pf.mkdirs();
		String t = Launcher.getTempDir();
		JSONObject script = json.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("install_script");
		JSONArray download = script.optJSONArray("download");
		JSONArray unzip = script.optJSONArray("unzip");
		JSONArray post = script.optJSONArray("post_install");
		totalTasks += download.length();
		totalTasks += unzip.length();
		if(download == null || scriptedDownload(download, p, t)) {
			if(unzip == null || scriptedUnzip(unzip, p, t)) {
				if(post != null) scriptedPost(post, p, t);
				modpackVersionFile();
				return;
			} else {
				updateFatalError();
				return;
			}
		} else {
			updateFatalError();
			return;
		}
	}

	private void clientVersionFile() {
		String s = Config.get("path") + modpack.client() + File.separator;
		File f2 = new File(s + "start.json");
		if(f2.exists()) f2.delete();
		try {
			FileUtils.writeString(f2, clientStartJson.toString());
		} catch (Exception e) {
			Log.error("client start.json write failed", e);
		}
		File f = new File(s + "version");
		if(f.exists()) f.delete();
		try {
			FileUtils.writeString(f, "" + clientJson.getInt("update_build"));
		} catch (Exception e) {
			Log.error("client version write failed", e);
		}
	}
	
	private void modpackVersionFile() {
		String s = Config.get("path") + modpack.id() + File.separator;
		File f = new File(s + "version");
		if(f.exists()) f.delete();
		try {
			FileUtils.writeString(f, "" + json.getInt("update_build"));
		} catch (Exception e) {
			Log.error("modpack version write failed", e);
		}
	}

	private void scriptedPost(JSONArray j, String root, String temp) {
		for(Iterator<Object> it = j.iterator(); it.hasNext(); ) {
			try {
				JSONObject o = (JSONObject) it.next();
				if(o.has("remove")) {
					String s = o.getString("remove");
					if(s.equals("temp")) {
						deleteDirectoryContents(Paths.get(temp));
					} else deleteDirectoryRecursion(Paths.get(root, s));
				}
			} catch (Exception e) {
			}
		}
	}
	
	private boolean scriptedRemoveDirs(JSONArray j, String root, String temp) {
		for(Iterator<Object> it = j.iterator(); it.hasNext(); ) {
			try {
				String s = (String) it.next();
				if(s.equals("temp")) {
					deleteDirectoryContents(Paths.get(temp));
				} else deleteDirectoryRecursion(Paths.get(root, s));
			} catch (Exception e) {
				break;
			}
		}
		return true;
	}

	private boolean scriptedDownload(JSONArray j, String root, String temp) {
		modpack.setUpdateInfo(null, "Скачивание", -2);
		WebUtils.setListener(this);
		String p = root;
		String tp = temp;
		if(p.endsWith(File.separator)) p.substring(0, p.length() - 1);
		if(tp.endsWith(File.separator)) tp.substring(0, tp.length() - 1);
		int mods = -1;
		for(Iterator<Object> it = j.iterator(); it.hasNext(); ) {
			JSONObject o = (JSONObject) it.next();
			String name = o.getString("name");
			modpack.setUpdateInfo(null, "Скачивание: " + name + " (0%)", percentI());
			if(o.has("check")) {
				JSONObject check = o.getJSONObject("check");
				String type = check.optString("type");
				if(type != null && !type.equals("force")) {
					try {
						boolean b = false;
						if(type.equals("mods")) {
							if(mods != -1) {
								if(mods == 1) b = true;
							} else if((mods = checkMods(modpack, root, json.getJSONObject("integrity_check")) ? 1 : 0) == 1) b = true;
						} else if(type.equals("libraries")) {
							if(checkClientLibraries(root)) b = true;
						} else if(type.equals("natives")) {
							if(checkClientNatives(root)) b = true;
						} else if(type.equals("exists")) {
							if(new File(p + path(check.getString("path"))).exists()) b = true;
						}
						if(b) {
							tasksDone++;
							continue;
						}
					} catch (Exception e) {
						updateFatalError("scriptedDownload(): check", e, Errors.UPDATER_SCRIPTEDDOWNLOAD_CHECK);
					}
				}
			}
			String url = o.getString("url");
			String dir = path(o.getString("dir"));
			if(dir.equals(File.separator)) dir = root + name;
			else if(dir.equals("temp")) dir = temp + name;
			else dir = p + dir + name;
			try {
				boolean b = true;
				while(b) {
					try {
						WebUtils.download(url, dir);
						b = false;
					} catch(IOException e) {
						if(downloadFailCount > 2) throw e;
						Log.warn("download io err: " + e.toString() + ", retrying..");
						downloadFailCount++;
						Thread.sleep(500L);
					}
				}
			} catch (Exception e) {
				updateFatalError("scriptedDownload(): download", e, Errors.UPDATER_SCRIPTEDDOWNLOAD_DOWNLOAD);
				return false;
			}
			tasksDone++;
		}
		return true;
	}
	
	private boolean scriptedUnzip(JSONArray j, String root, String temp) {
		modpack.setUpdateInfo(null, "Распаковка архивов", -2);
		ZipUtils.setListener(this);
		String p = root;
		String tp = temp;
		if(p.endsWith(File.separator)) p.substring(0, p.length() - 1);
		if(tp.endsWith(File.separator)) tp.substring(0, tp.length() - 1);
		for(Iterator<Object> it = j.iterator(); it.hasNext(); ) {
			JSONObject o = (JSONObject) it.next();
			String name = o.optString("name");
			String path = o.optString("path");
			String from = o.optString("from");
			path = path(path);
			from = path(from);
			if(path.equals(File.separator)) path = root;
			else if(path.equals("temp")) path = temp;
			else {
				if(path.startsWith(File.separator))
					path = p + path.substring(1);
				else path = p + path;
			}
			if(from.equals("temp")) from = temp + name;
			else from = tp + from + name;
			currentUnzipFile = name;
			modpack.setUpdateInfo(null, "Распаковка: " + name + " (0%)", percentI());
			if(!new File(from).exists()) {
				tasksDone++;
				continue;
			}
			try {
				ZipUtils.unzip(from, path);
			} catch (ZipException e) {
				if(!e.toString().equalsIgnoreCase("zip file is empty")) {
					updateFatalError("scriptedUnzip(): unzip", e, Errors.UPDATER_SCRIPTEDUNZIP_UNZIP);
					return false;
				}
			} catch (Exception e) {
				updateFatalError("scriptedUnzip(): unzip", e, Errors.UPDATER_SCRIPTEDUNZIP_UNZIP);
				return false;
			}
			tasksDone++;
		}
		return true;
	}

	private int percentI(double i) {
		return (int) (percentD() + (i * (1D / (double) totalTasks) * 100D));
	}
	
	private double percentD() {
		return ((double) tasksDone / (double) totalTasks) * 100D;
	}
	
	private int percentI() {
		return (int) percentD();
	}

	private void startClient() throws Exception {
		if (clientProcess != null)
			throw new IllegalStateException("Client still running!");
		try {
			File[] libraries = getClientLibariesFiles();
			HashSet<File> d = new HashSet<File>();
			d.add(new File(getClientJarPath()));
			for (File f: libraries) d.add(f);
			ArrayList<String> jvmArgs = new ArrayList<>();
			jvmArgs.add("-Xms" + Config.getInt("xms") + "M");
			jvmArgs.add("-Xmx" + Config.getInt("xmx") + "M");
			jvmArgs.add("-Djava.library.path=" + getNativesDir());
			jvmArgs.add("-Dfile.encoding=UTF-8");

			ArrayList<String> appArgs = new ArrayList<>();
			if(auth.isCracked()) {
				appArgs.add("--username");
				appArgs.add(auth.getUsername());
				appArgs.add("--accessToken");
				appArgs.add("null");
				appArgs.add("--userProperties");
				appArgs.add("{}");
			} else if(auth.isMojang()) {
				appArgs.add("--username");
				appArgs.add(auth.getUsername());
				appArgs.add("--uuid");
				appArgs.add(auth.getMojangUUID());
				appArgs.add("--accessToken");
				appArgs.add(auth.getMojangAuthToken());
				appArgs.add("--userProperties");
				appArgs.add("{}");
			}
			appArgs.add("--gameDir");
			appArgs.add(getModpackDir());
			appArgs.add("--assetsDir");
			appArgs.add(getAssetsDir());
			appArgs.add("--assetIndex");
			appArgs.add(clientAssetIndex);
			for (String tw : clientTweakClasses) {
				appArgs.add("--tweakClass");
				appArgs.add(tw);
			}
			appArgs.add("--modpackid");
			appArgs.add(modpack.id());
			appArgs.add("--modpackname");
			appArgs.add(modpack.getName());
			clientProcess = StartUtil.startJarProcess(new File(getModpackDir()), d, clientMainClass, jvmArgs,
					appArgs);
			PrintStream ps = ClientLog.getInstance();
			final InputStreamCopier input = new InputStreamCopier(clientProcess.getInputStream(), ps);
			final InputStreamCopier error = new InputStreamCopier(clientProcess.getErrorStream(), ps);
			error.start();
			input.start();
			clientStarted();
			final int exitCode = clientProcess.waitFor();
			ps.append("\nLOG END\n");
			Log.info("Client exit code: " + exitCode);
			if (exitCode == 1 || exitCode == -1) {
				clientError("Клиент аварийно завершился! Код: " + exitCode);
			}
			input.interrupt();
			error.interrupt();
			clientProcess = null;
			clientStopped();
		} catch (NullPointerException e) {
			Log.error("startClient()", e);
			clientProcess = null;
		}
	}

	private void clientStarted() {
		forceUpdate = false;
		running = true;
		failed = false;
		updating = false;
		clientStarted = true;
	}

	private void clientStopped() {
		forceUpdate = false;
		running = false;
		failed = false;
		updating = false;
		clientStarted = false;
	}

	private void reset() {
		forceUpdate = false;
		running = false;
		failed = false;
	}
	
	public String getClientDir() {
		return Config.get("path") + modpack.client() + File.separator;
	}
	
	public String getModpackDir() {
		return Config.get("path") + modpack.id() + File.separator;
	}

	public String getAssetsDir() {
		return getClientDir() + "assets" + File.separator;
	}

	public String getLibrariesDir() {
		return getClientDir() + "libraries" + File.separator;
	}

	public String getNativesDir() {
		return getClientDir() + "natives" + File.separator;
	}

	public File[] getClientLibariesFiles() {
		return new File(getClientDir() + "libraries" + File.separator).listFiles((file) -> file.isDirectory() || file.getName().toLowerCase().endsWith(".jar"));
	}

	public String getClientJarPath() {
		return getClientDir() + "client.jar";
	}

	private void clientError(String s) {
		String x = ClientLog.getInstance().getLastException();
		if(x == null) x = "No Error";
		if(x.contains("java.lang.NoClassDefFoundError: java/util/jar/Pack200") || 
				x.contains("java.lang.ClassNotFoundException: java.util.jar.Pack200")) {
		x = "Возможное решение:\nОшибка несовместимости Forge с версией Java!!\nИспользуйте версию Java меньше 14 \n"
				+ "(8 - самый оптимальный вариант)\n" + x;
		} else if(x.contains("AppClassLoader cannot be cast to class java.net.URLClassLoader")) {
			x = "Возможное решение:\nОшибка несовместимости Forge с версией Java!!\nИспользуйте версию Java 8\n" + x;
			}
		ErrorUI.clientError("Ошибка клиента", s, x);	
	}

	private void clientError(String s, Throwable t) {
		String e = Log.exceptionToString(t);
		ErrorUI.clientError("Ошибка клиента", s, s + "\n" + e);	
	}

	private void updateFatalError(String s, Throwable t, int i) {
		String e = Log.exceptionToString(t);
		ErrorUI.showError("Ошибка обновления", s + " (Код ошибки: "  + Errors.toHexString(i) + ")", s + " (Код ошибки: " + Errors.toHexString(i) + ")\n" + e);	
		updateFatalError();
	}

	private void updateFatalError(String s, String s2, int i) {
		ErrorUI.showError("Ошибка обновления", s, s + ": " + s2 + " (Код ошибки: " +  Errors.toHexString(i) + ")");
		updateFatalError();
	}

	private void updateFatalError(String s, int i1, int i2) {
		String e = Log.getTraceString(2);
		ErrorUI.showError("Ошибка обновления", s, s + ": " + Errors.toHexString(i1) + " (Код ошибки: " + Errors.toHexString(i2) + ")\n" + e);
		updateFatalError();
	}

	private void updateFatalError(String s, String s2) {
		ErrorUI.showError("Ошибка обновления", s, s2);
		updateFatalError();
	}

	private void fatalError(String s) {
		Log.error(s);
		String e = Log.exceptionToString(new Exception());
		ErrorUI.showError("Ошибка обновления", s, e);
		updateFatalError();
	}
	
	private void updateFatalError() {
		currentInst = null;
		updating = false;
		forceUpdate = false;
		running = false;
		failed = true;
	}
	
	private void interrupted() {
		currentInst = null;
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
		deleteDirectoryContents(path);
		Files.delete(path);
	}
	private static void deleteDirectoryContents(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursion(entry);
				}
			}
		}
	}

	@Override
	public void startZip(String zipFile) {
		
	}

	@Override
	public void doneZip(String zipFile) {
		
	}

	@Override
	public void startUnzip(String zipFile) {
		
	}

	@Override
	public void unzipProgress(String currentFile, int totalPercent, int currentFilePercent) {
		uiInfo(null, "Распаковка: " + currentUnzipFile + ": " + currentFile + " (" + totalPercent + "%)", percentI(totalPercent / 100D));
	}

	@Override
	public void doneUnzip(String zipFile) {
		
	}

	@Override
	public void startDownload(String filename) {
		
	}
	
	int avgcounter;
	float avgsum;
	@Override
	public void downloadProgress(String filename, double speed, int percent, int bytesLeft) {
		avgsum += speed;
		avgcounter++;
		if(avgcounter == 15) {
			avgspeed = avgsum / 15;
			avgsum = avgspeed;
			avgcounter = 1;
		}
		float s = avgspeed;
		s = Math.round(s * 100) / 100F;
		// секунды
		int left = (int)((bytesLeft/1024F/1024F) / s);
		if(s == 0) left = 0;
		//Log.debug(s + "mbs left: " + timeStr(left));
		uiInfo(null, "Скачивание: " +  filename 
				+ " (" + speed + "Mb/s)"
				+ " (" + percent + "%)"
				+ " Осталость приблизительно: " + timeStr(left) + ""
				, percentI(percent / 100D));
	}

	private static String timeStrHM(long sec) {
		sec = sec % 86400 / 60;
		return timeStr((int) sec);
	}

	private static String timeStr(int sec) {
		if (sec <= 0)
			return "0:00";
		String s = "" + sec % 60;
		if (s.length() < 2)
			s = "0" + s;
		s = (int) (sec / 60D) + ":" + s;
		return s;
	}
	
	private void uiInfo(String s1, String s2, int p) {
		modpack.setUpdateInfo(s1, s2, p);
	}

	@Override
	public void doneDownload(String zipFile) {
		
	}

}

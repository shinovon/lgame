package ru.lgame.launcher.update;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sf.jazzlib.ZipException;
import ru.lgame.launcher.Config;
import ru.lgame.launcher.Errors;
import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.ui.ErrorUI;
import ru.lgame.launcher.ui.locale.Text;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.HashUtils;
import ru.lgame.launcher.utils.LauncherOfflineException;
import ru.lgame.launcher.utils.ZipUtils;
import ru.lgame.launcher.utils.logging.ClientLog;
import ru.lgame.launcher.utils.logging.InputStreamCopier;
import ru.lgame.launcher.utils.logging.Log;
import ru.lgame.launcher.utils.HttpUtils;

/**
 * Система обновления
 * @author Shinovon
 */
public final class Updater implements Runnable, ZipUtils.ProgressListener, HttpUtils.ProgressListener {
	
	private static Updater currentInst;
	private static Thread currentThread;

	private boolean running;
	
	private Modpack modpack;
	private Auth auth;
	
	private JSONObject json;
	private JSONObject clientJson;

	private boolean clientNewLibraries;
	private boolean clientNewAssets;

	private JSONObject clientLibrariesJson;
	private JSONObject clientAssetsJson;

	private boolean forceUpdate;
	private boolean updating;
	private boolean failed;
	private boolean repeated;
	private boolean offline;

	protected int tasksDone;
	private int totalTasks;
	
	private int downloadFailCount;

	public boolean clientStarted;

	private JSONObject clientStartJson;
	private String clientMainClass;
	private String clientAssetIndex;
	private String[] clientTweakClasses;
	private String[] clientJvmArgs;
	private String[] clientExtraArgs;

	private String currentUnzipFile;

	private int totalAssets;
	private int downloadedAssets;

	private boolean hasMojangJre;
	private String mojang_jre;

	private boolean hideDownloadStatus;

	public static Process clientProcess;

	private static int count;
	
	// счетчик скорости
	private float avgspeed;
	int avgcounter;
	float avgsum;

	private Updater(Modpack m, Auth a, boolean b) {
		this.modpack = m;
		this.auth = a;
		this.forceUpdate = b;
	}
	
	public static void start(Modpack m, Auth a) {
		if(currentThread != null && currentThread.isAlive()) return;
		currentThread = new Thread(new Updater(m, a, false), "Updater-" + (++count));
		currentThread.setPriority(9);
		currentThread.start();
	}
	
	public static void startForceUpdate(Modpack m, Auth a) {
		if(currentThread != null && currentThread.isAlive()) return;
		currentThread = new Thread(new Updater(m, a, true), "Updater-" + (++count));
		currentThread.setPriority(9);
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
			Log.warn("Get modpack state failed", e);
			return -2;
		} catch (IOException e) {
			Log.warn("Get modpack state failed", e);
			return -1;
		} catch (Exception e) {
			Log.warn("Get modpack state failed", e);
			return -3;
		}
		return 1;
	}

	/**
	 * Проверить установку сборки
	 * @param m Объект сборки
	 */
	public static boolean checkInstalled(Modpack m) {
		String s = Launcher.getLibraryDir() + m.id() + File.separator;
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
		String p = Launcher.getLibraryDir() + m.id() + File.separator;
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
					if(!new File(p + d).exists()) {
						Log.warn("not exists " + p + d);
						return false;
					}
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
		JSONArray ignoreMods = md.optJSONArray("ignore");
		boolean ignoreExcessMods = md.getBoolean("ignore_excess_mods");
		boolean removeAll = md.getBoolean("remove_all_if_check_failed");
		boolean deleteBlacklisted = md.getBoolean("delete_blacklist_mods");
		String digest = md.getString("checksum_algorithm");
		ArrayList<String> names = new ArrayList<String>();
		JSONObject ck = md.getJSONObject("checksums");
		for (String s : ck.keySet()) {
			names.add(s.toLowerCase());
			String hash = ck.getString(s);
			String filep = p + "mods" + File.separator + s;
			File file = new File(filep);
			if (!file.exists()) {
				Log.warn("mod not exists: " + file);
				if(removeAll) {
					try {
						FileUtils.deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
					} catch (Exception e) {
					}
				}
				return false;
			}
			try {
				String hash2 = HashUtils.getFileChecksum(filep, digest);
				if (!hash2.equalsIgnoreCase(hash)) {
					Log.debug("mod has wrong checksum: " + s + " " + hash2);
					if(removeAll) FileUtils.deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
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
			for(String s: blacklist) {
				if(n.contains(s.toLowerCase())) {
					Log.warn("blacklisted mod: " + n);
					if(deleteBlacklisted) f.delete();
					else {
						if(removeAll) {
							FileUtils.deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
							return false;
						}
					}
				}
			}
			if(!ignoreExcessMods && !names.contains(n)) {
				boolean b = false;
				if(ignoreMods != null) {
					for(Object s: ignoreMods.toList()) {
						if(n.startsWith((String)s)) {
							b = true;
							break;
						}
					}
				}
				if(b) continue;
				Log.warn("Excess mod: " + n);
				if(removeAll) FileUtils.deleteDirectoryRecursion(Paths.get(p + "mods" + File.separator));
				else try {
					f.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(removeAll) return false;
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
		String p = Launcher.getLibraryDir() + modpack.client() + File.separator;
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
	
	private boolean checkClientJar(String p) throws Exception {
		File f = new File(p + "client.jar");
		if(!f.exists()) return false;
		if(!HashUtils.getFileChecksum(f.toString(), "SHA-1").equalsIgnoreCase(clientJson.getJSONObject("integrity_check").getString("client_sha1"))) {
			Log.warn("Client has wrong hash");
			return false;
		}
		return true;
	}


	/**
	 * Проверка целостности клиента
	 */
	private boolean checkClientIntegrity() throws Exception {
		Log.info("Checking client integrity");
		String p = Launcher.getLibraryDir() + modpack.client() + File.separator;
		File f = new File(p + "version");
		if(!f.exists()) return false;
		if(!checkClientJar(p)) return false;
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
		
		if(j.has("libraries")) {
			Log.info("Checking client libraries");
			if(!checkClientLibraries(p)) return false;
		}

		if(j.has("natives")) {
			Log.info("Checking client natives");
			if(!checkClientNatives(p)) return false;
		}
		if(j.has("assets")) {
			Log.info("Checking client assets");
			if(!checkClientAssets(p)) return false;
		}
		if(j.has("mojang_jre")) {
			Log.info("Checking mojang jre");
			if(!checkClientMojangJRE()) return false;
		}
		return true;
	}
	
	private boolean checkClientAssets(String p) {
		return new File(p + "assets" + File.separator + "objects" + File.separator).exists()
				&& new File(p + "assets" + File.separator + "indexes" + File.separator + clientJson.getString("asset_index") + ".json").exists()
				&& (clientNewAssets ? checkClientNewAssets(p) : true);
	}
	
	private boolean checkClientNewAssets(String p) {
		JSONObject objects = clientAssetsJson.getJSONObject("objects");
		for(String key: objects.keySet()) {
			JSONObject object = objects.getJSONObject(key);
			String hash = object.getString("hash");
			File file = new File(p + "assets" + File.separator + "objects" + File.separator + hash.substring(0, 2) + File.separator + hash);
			if(!file.exists()) {
				Log.warn("Asset not exists: " + hash + " (" + key + ")");
				return false;
			}
			long l = org.apache.commons.io.FileUtils.sizeOf(file);
			if(l != object.getLong("size")) {
				Log.warn("Asset has wrong size: " + hash + " (" + key + ")");
				return false;
			}
		}
		return true;
	}

	private boolean checkClientLibrary(String p, JSONObject j) throws Exception {
		String s = j.getString("name");
		String sha1 = j.getString("sha1");
		long size = j.getLong("size");
		String path = p + "libraries" + File.separator + path(j.getString("path"));
		File file = new File(path);
		if (!file.exists()) {
			Log.warn("Library does not exists: " + s);
			//FileUtils.deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
			return false;
		}
		long l = org.apache.commons.io.FileUtils.sizeOf(file);
		if(l != size) {
			Log.warn("Library has wrong size: " + s + " " + l + " " + size + " " + path);
			file.delete();
			//FileUtils.deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
			return false;
		}
		String hash2 = HashUtils.getFileChecksum(path, "SHA-1");
		if (!hash2.equalsIgnoreCase(sha1)) {
			file.delete();
			//FileUtils.deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
			Log.warn("Library has wrong checksum: " + s + " " + hash2);
			return false;
		}
		return true;
	}

	/**
	 * Проверка библиотек клиента
	 */
	private boolean checkClientLibraries(String p) throws Exception {
		JSONObject md = clientJson.getJSONObject("integrity_check").getJSONObject("libraries");
		if(md.optBoolean("new_libraries")) {
			clientNewLibraries = true;
			if(clientLibrariesJson == null) {
				modpack.setClientLibrariesURL(md.getString("url"));
				clientLibrariesJson = modpack.getClientLibrariesJson();
				totalTasks += clientLibrariesJson.getJSONArray("libraries").length();
			}
			if(!new File(p + "libraries" + File.separator).exists()) return false;
			JSONArray libraries = clientLibrariesJson.getJSONArray("libraries");
			for (Object o: libraries) {
				if(!checkClientLibrary(p, (JSONObject) o))
					return false;
			}
			return true;
		}
		if(!new File(p + "libraries" + File.separator).exists()) return false;
		String digest = md.optString("checksum_algorithm", "SHA-1");
		ArrayList<String> names = new ArrayList<String>();
		JSONObject ck = md.getJSONObject("checksums");
		for (String s : ck.keySet()) {
			names.add(s.toLowerCase());
			String hash = ck.getString(s);
			String filep = p + "libraries" + File.separator + s;
			File file = new File(filep);
			if (!file.exists()) {
				Log.warn("not exists: " + s);
				FileUtils.deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
				return false;
			}
			if (!HashUtils.getFileChecksum(filep, digest).equalsIgnoreCase(hash)
					/*&& !(s.contains("-srg.jar") && s.contains("client-1.16.5")) */ // костыль
					) {
				FileUtils.deleteDirectoryRecursion(Paths.get(p + "libraries" + File.separator));
				Log.warn("wrong checksum: " + s);
				return false;
			}
		}
		FileFilter filter = (file) -> file.isDirectory() || file.getName().toLowerCase().endsWith(".jar");
		File[] libs = new File(p + "libraries" + File.separator).listFiles(filter);
		if(libs != null)
		for (File f : libs) {
			if(f.isDirectory()) continue;
			String n = f.getName().toLowerCase();
			if(!names.contains(n)) {
				Log.warn("Excess jar library: " + n);
				return false;
			}
		}
		return true;
	}

	/**
	 * Проверка нативных библиотек клиента
	 */
	private boolean checkClientNatives(String p) throws Exception {
		JSONObject md = clientJson.getJSONObject("integrity_check").getJSONObject("natives");
		String digest = md.getString("checksum_algorithm");
		ArrayList<String> names = new ArrayList<String>();
		JSONObject ck = md.getJSONObject("checksums");
		for (String s : ck.keySet()) {
			names.add(s.toLowerCase());
			String hash = ck.getString(s);
			String filep = p + "natives" + File.separator + s;
			File file = new File(filep);
			if (!file.exists()) {
				Log.warn("Native does not exists: " + s);
				FileUtils.deleteDirectoryRecursion(Paths.get(p + "natives" + File.separator));
				return false;
			}
			if (!HashUtils.getFileChecksum(filep, digest).equalsIgnoreCase(hash)) {
				FileUtils.deleteDirectoryRecursion(Paths.get(p + "natives" + File.separator));
				Log.warn("Native has wrong checksum: " + s);
				return false;
			}
		}
		FileFilter filter = (file) -> file.isDirectory() || (file.getName().toLowerCase().endsWith(".dll") || file.getName().toLowerCase().endsWith(".so"));
		File[] natives = new File(p + "natives" + File.separator).listFiles(filter);
		if(natives != null)
		for (File f : natives) {
			if(f.isDirectory()) continue;
			String n = f.getName().toLowerCase();
			if(!names.contains(n)) {
				FileUtils.deleteDirectoryRecursion(Paths.get(p + "natives" + File.separator));
				Log.warn("Excess native: " + n);
				return false;
			}
		}
		return true;
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
		String p = Launcher.getLibraryDir() + m.id() + File.separator;
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
		modpack.setUpdateInfo(repeated ? Text.get("state.updating") : Text.get("state.updatestart"), repeated ? Text.get("state.updaterepeatcheck") : Text.get("state.installationcheck"), 0);
		int modpackState = forceUpdate ? 3 : checkInstalled(modpack) ? -100 : 0;
		stat("run");
		try {
			modpack.setUpdateInfo(null, Text.get("state.clientstartjson"), 15);
			clientStartJson = modpack.getClientStartJson(modpackState == 0 || forceUpdate);
			if(clientStartJson == null) {
				if(modpackState == 0 || forceUpdate)
					updateFatalError(Text.get("err.offline"), 0, Errors.UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION);
				else
					updateFatalError(Text.get("err.nostartjson"), 0, Errors.UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION);
				return;
			}
			clientAssetIndex = clientStartJson.getString("asset_index");
			clientMainClass = clientStartJson.getString("mainclass");
			clientTweakClasses = clientStartJson.getJSONArray("tweak_classes").toList().toArray(new String[0]);
			if(clientStartJson.has("extra_args")) {
				clientExtraArgs = clientStartJson.getJSONArray("extra_args").toList().toArray(new String[0]);
			}
			if(clientStartJson.has("jvm_args")) {
				clientJvmArgs = clientStartJson.getJSONArray("jvm_args").toList().toArray(new String[0]);
			}
			modpack.setUpdateInfo(null, Text.get("state.modpackupdatejson"), 33);
			json = modpack.getUpdateJson(true);
			modpack.setUpdateInfo(null, Text.get("state.clientupdatejson"), 67);
			clientJson = modpack.getClientUpdateJson(true);
			try {
				if(clientJson.getJSONObject("integrity_check").getJSONObject("libraries").optBoolean("new_libraries")) {
					clientNewLibraries = true;
					Log.info("client uses new libraries");
					if(clientLibrariesJson == null) {
						modpack.setClientLibrariesURL(clientJson.getJSONObject("integrity_check").getJSONObject("libraries").getString("url"));
						clientLibrariesJson = modpack.getClientLibrariesJson();
						//totalTasks += clientLibrariesJson.getJSONArray("libraries").length();
					}
				}
			} catch (Exception e) {
			}
			try {
				if(clientJson.getJSONObject("integrity_check").getJSONObject("assets").optBoolean("new_assets")) {
					clientNewAssets = true;
					Log.info("client uses new assets");
					if(clientAssetsJson == null) {
						clientAssetsJson = new JSONObject(HttpUtils.get((clientJson.getJSONObject("integrity_check").getJSONObject("assets").getString("url"))));
						//totalTasks += clientAssetsJson.getJSONObject("objects").length();
					}
				}
			} catch (Exception e) {
			}
			try {
				if(clientJson.optBoolean("has_mojang_jre")) {
					hasMojangJre = true;
					Log.info("client uses mojang jre");
					mojang_jre = clientJson.getString("mojang_jre");
				}
			} catch (Exception e) {
			}
		} catch (LauncherOfflineException e) {
			if(modpackState == 0 || forceUpdate) {
				updateFatalError(Text.get("err.offline"), e.getCause(), Errors.UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION);
				return;
			}
		} catch (IOException e) {
			if(modpackState == 0 || forceUpdate) {
				updateFatalError(Text.get("err.offline"), e, Errors.UPDATER_RUN_GETUPDATEJSON_IOEXCEPTION);
				return;
			}
		} catch (JSONException e) {
			if(modpackState == 0 || forceUpdate) {
				updateFatalError(Text.get("err.parse"), e, Errors.UPDATER_RUN_GETUPDATEJSON_JSONEXCEPTION);
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
		modpack.setUpdateInfo(Text.get("state.installationcheck"), Text.get("state.clientintegritycheck"), 0);
		//boolean packInstalled = modpackState != 0;
		boolean clientInstalled = checkClientInstalled();
		modpack.setUpdateInfo(null, Text.get("state.clientupdatecheck"), 25);
		boolean clientNeedsUpdate = false;
		try {
			clientNeedsUpdate = checkClientNeedUpdate();
		} catch (LauncherOfflineException e) {
			Log.warn("can't check client in offline mode");
			offline = true;
		} catch (Exception e1) {
			updateFatalError(Text.get("err.clientupdatecheck"), e1, Errors.UPDATER_RUN_CHECKCLIENT_EXCEPTION);
			return;
		}
		modpack.setUpdateInfo(null, Text.get("state.modpackintegritycheck"), 50);
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
					modpack.setUpdateInfo(null, Text.get("state.updatesavailablecheck"), 75);
					if(checkUpdatesAvailable(modpack)) modpackState = 2;
				}
			} catch (LauncherOfflineException e) {
				Log.warn("can't check modpack in offline mode");
				offline = true;
			} catch (Exception e) {
				updateFatalError(Text.get("err.modpackcheck"), e, Errors.UPDATER_RUN_CHECKMODPACK_EXCEPTION);
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
		Log.info("install state: " + modpackState);
		modpack.setUpdateInfo(Text.get("state.updating"), Text.get("stater.updaterinit"), 0);
		// обнова либо старт сборки
		boolean ret = !repeated;
		switch(modpackState) {
		case 0:
		{
			modpack.setUpdateInfo(Text.get("state.updatingclient"), Text.get("state.updatingclient"), 0);
			boolean client = true;
			if(!clientInstalled) client = installClient();
			else if(clientNeedsUpdate) client = updateClient();
			if(!client) return;
			modpack.setUpdateInfo(Text.get("state.updatingmodpack"), Text.get("state.updatingmodpack"), -2);
			install();
			break;
		}
		case 1:
		{
			stat("launch");
			modpack.setUpdateInfo("", Text.get("state.startingclient"), 100);
			try {
				startClient();
			} catch (Exception e) {
				clientError(Text.get("err.clientstartfail"), e);
				return;
			}
			ret = false;
			break;
		}
		case 2:
		case 3:
		{
			modpack.setUpdateInfo(Text.get("state.updatingclient"), Text.get("state.updatingclient"), 0);
			if(!clientInstalled) installClient();
			else if(clientNeedsUpdate) updateClient();
			modpack.setUpdateInfo(Text.get("state.updatingmodpack"), Text.get("state.updatingmodpack"), -2);
			update();
			break;
		}
		case 4: {
			modpack.setUpdateInfo(Text.get("state.updatingclient"), Text.get("state.updatingclient"), 0);
			installClient();
			break;
		}
		case 5: {
			modpack.setUpdateInfo(Text.get("state.updatingclient"), Text.get("state.updatingclient"), 0);
			updateClient();
			break;
		}
		default:
		{
			modpack.setUpdateInfo("", Text.get("state.error"), -2);
			updateFatalError("default", modpackState, Errors.UPDATER_GETMODPACKSTATE_ILLEGAL_VALUE);
			ret = false;
			break;
		}
		}
		if(ret && !failed) {
			modpack.setUpdateInfo(Text.get("state.updating"), Text.get("state.updaterinitrepeat"), 100);
			Log.info("Updater repeat");
			reset();
			repeated = true;
			run();
			return;
		}
		Log.info("updater finished");
		reset();
		Launcher.inst.frame().mainPane().update();
	}

	private void stat(String type) {
		JSONObject o = new JSONObject();
		o.put("type", "updater_" + type);
		o.put("modpack", modpack.id());
		try {
			o.put("modpack_build", FileUtils.getString(Launcher.getLibraryDir() + modpack.client() + File.separator + "version"));
		} catch (Exception e) {
		}
		Launcher.stat(o);
	}

	private boolean updateClient() {
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getClientDir();
		String t = Launcher.getTempDir(modpack.client());
		JSONObject script = clientJson.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("update_script");
		JSONArray download = script.getJSONArray("download");
		JSONArray unzip = script.getJSONArray("unzip");
		JSONArray preremove = script.optJSONArray("pre_remove_dirs");
		JSONArray post = script.optJSONArray("post_update");
		totalTasks += download.length();
		if(clientNewLibraries) {
			totalTasks += clientLibrariesJson.getJSONArray("libraries").length();
		}
		if(clientNewAssets) {
			totalAssets = clientAssetsJson.getJSONObject("objects").length();
			totalTasks += totalAssets;
		}
		totalTasks += unzip.length();
		if(preremove == null || scriptedRemoveDirs(preremove, p, t)) {
			if(download == null || scriptedDownload(download, p, t)) {
				if(unzip == null || scriptedUnzip(unzip, p, t)) {
					if(post != null) scriptedPost(post, p, t);
					writeClientInfo();
					return true;
				} else {
					updateFatalError();
					return false;
				}
			} else {
				updateFatalError();
				return false;
			}
		}
		return false;
	}

	private boolean installClient() {
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getClientDir();
		File pf = new File(p);
		if(!pf.exists()) pf.mkdirs();
		String t = Launcher.getTempDir(modpack.client());
		JSONObject script = clientJson.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("install_script");
		JSONArray download = script.getJSONArray("download");
		JSONArray unzip = script.getJSONArray("unzip");
		JSONArray post = script.optJSONArray("post_install");
		totalTasks += download.length();
		totalTasks += unzip.length();
		if(clientNewLibraries) {
			totalTasks += clientLibrariesJson.getJSONArray("libraries").length();
		}
		if(clientNewAssets) {
			totalAssets = clientAssetsJson.getJSONObject("objects").length();
			totalTasks += totalAssets;
		}
		boolean fail = false;
		if(download == null || scriptedDownload(download, p, t)) {
			if(unzip == null || scriptedUnzip(unzip, p, t)) {
				if(post != null) scriptedPost(post, p, t);
				writeClientInfo();
			} else {
				fail = true;
				updateFatalError();
			}
		} else {
			fail = true;
			updateFatalError();
		}
		return !fail;
	}

	private void update() {
		stat("update");
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getModpackDir();
		String t = Launcher.getTempDir(modpack.id());
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
					writeModpackInfo();
				} else updateFatalError();
			} else updateFatalError();
		}
	}
	
	private void install() {
		stat("install");
		totalTasks = 0;
		tasksDone = 0;
		updating = true;
		String p = getModpackDir();
		File pf = new File(p);
		if(!pf.exists()) pf.mkdirs();
		String t = Launcher.getTempDir(modpack.id());
		JSONObject script = json.getJSONObject("update").getJSONObject("update_scripts").getJSONObject("install_script");
		JSONArray download = script.optJSONArray("download");
		JSONArray unzip = script.optJSONArray("unzip");
		JSONArray post = script.optJSONArray("post_install");
		totalTasks += download.length();
		totalTasks += unzip.length();
		if(download == null || scriptedDownload(download, p, t)) {
			if(unzip == null || scriptedUnzip(unzip, p, t)) {
				if(post != null) scriptedPost(post, p, t);
				writeModpackInfo();
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

	private void writeClientInfo() {
		Log.info("Writing client version file");
		String s = Launcher.getLibraryDir() + modpack.client() + File.separator;
		File f2 = new File(s + "start.json");
		if(f2.exists()) f2.delete();
		try {
			FileUtils.writeString(f2, clientStartJson.toString());
		} catch (Exception e) {
			Log.error("client start.json write failed", e);
		}
		if(clientNewLibraries) {
			File f3 = new File(s + "libraries.json");
			if(f3.exists()) f3.delete();
			try {
				FileUtils.writeString(f3, clientLibrariesJson.toString());
			} catch (Exception e) {
				Log.error("client libraries.json write failed", e);
			}
		}
		File f = new File(s + "version");
		if(f.exists()) f.delete();
		try {
			FileUtils.writeString(f, "" + clientJson.getInt("update_build"));
		} catch (Exception e) {
			Log.error("client version write failed", e);
		}
	}
	
	private void writeModpackInfo() {
		File f = new File(Launcher.getLibraryDir() + modpack.id() + File.separator + "version");
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
						FileUtils.deleteDirectoryContents(Paths.get(temp));
					} else FileUtils.deleteDirectoryRecursion(Paths.get(root, s));
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
					FileUtils.deleteDirectoryContents(Paths.get(temp));
				} else FileUtils.deleteDirectoryRecursion(Paths.get(root, s));
			} catch (Exception e) {
				break;
			}
		}
		return true;
	}

	private boolean scriptedDownload(JSONArray j, String root, String temp) {
		modpack.setUpdateInfo(null, Text.get("state.downloading"), -2);
		HttpUtils httpClient = new HttpUtils();
		httpClient.setListener(this);
		String p = root;
		//String tp = temp;
		//if(p.endsWith(File.separator)) p = p.substring(0, p.length() - 1);
		//if(tp.endsWith(File.separator)) tp = tp.substring(0, tp.length() - 1);
		int mods = -1;
		for(Iterator<Object> it = j.iterator(); it.hasNext(); ) {
			JSONObject o = (JSONObject) it.next();
			String name = o.getString("name");
			modpack.setUpdateInfo(null, Text.get("state.downloading") + ": " + name + " (0%)", percentI());
			check: {
				if(o.has("check")) {
					JSONObject check = o.getJSONObject("check");
					String type = check.optString("type");
					if(type != null && !type.equals("force")) {
						try {
							boolean skip = false;
							if(type.equals("mods")) {
								if(forceUpdate) {
									skip = false;
									break check;
								}
								if(mods != -1) {
									if(mods == 1) skip = true;
								} else if((mods = checkMods(modpack, root, json.getJSONObject("integrity_check")) ? 1 : 0) == 1) skip = true;
							} else if(type.equals("libraries")) {
								if(checkClientLibraries(root)) skip = true;

								if(skip && clientNewLibraries) {
									tasksDone+=clientLibrariesJson.getJSONArray("libraries").length();
								}
							}  else if(type.equals("assets")) {
								if(checkClientAssets(p)) skip = true;

								if(skip && clientNewAssets && clientAssetsJson != null) {
									tasksDone+=clientAssetsJson.getJSONObject("objects").length();
								}
							} else if(type.equals("natives")) {
								if(checkClientNatives(root)) skip = true;
							} else if(type.equals("exists")) {
								if(new File(p + path(check.getString("path"))).exists()) skip = true;
							} else if(type.equals("notexists")) {
								if(!new File(p + path(check.getString("path"))).exists()) skip = true;
							} else if(type.equals("client")) {
								if(checkClientJar(p)) skip = true;
							} else if(type.equals("mojang_jre")) {
								if(checkClientMojangJRE()) skip = true;
							}
							if(skip) {
								tasksDone++;
								continue;
							}
						} catch (Exception e) {
							updateFatalError("scriptedDownload(): check", e, Errors.UPDATER_SCRIPTEDDOWNLOAD_CHECK);
						}
					}
				}
			}
			try {
				if(o.optBoolean("new_libraries")) {
					clientNewLibraries = true;
					if(clientLibrariesJson == null) {
						modpack.setClientLibrariesURL(o.getString("url"));
						clientLibrariesJson = modpack.getClientLibrariesJson();
						totalTasks += clientLibrariesJson.getJSONArray("libraries").length();
					}
					hideDownloadStatus = true;
					JSONArray libraries = clientLibrariesJson.getJSONArray("libraries");
					uiInfo("Скачивание библиотек");
					MultiThreadedDownloader downloader = new MultiThreadedDownloader(3, libraries.length(), this, "Скачивание библиотек", true);
					for (Object i: libraries) {
						JSONObject k = (JSONObject) i;
						String path = k.getString("path");
						String url = k.getString("url");
						String dir = p + "libraries" + File.separator + path(path);
						if(!checkClientLibrary(p, k)) {
							//modpack.setUpdateInfo(null, Text.get("state.downloading") + ": " + path.substring(path.lastIndexOf("/") + 1) + " (0%)", percentI());
							downloader.add(url, dir);
							/*
							boolean b = true;
							while(b) {
								try {
									WebUtils.download(url, dir);
									b = false;
								} catch(IOException e) {
									if(downloadFailCount > Config.getInt("downloadMaxAttempts")) throw e;
									Log.warn("download io err: " + e.toString() + ", retrying..");
									downloadFailCount++;
									Thread.sleep(500L);
								}
							}
							*/
						}
						//tasksDone++;
					}
					downloader.lock();
					downloader.stop();
					hideDownloadStatus = false;
					tasksDone++;
					continue;
				}
				if(o.optBoolean("new_assets")) {
					clientNewAssets = true;
					String indexUrl = o.getString("url");
					if(clientAssetsJson == null) {
						clientAssetsJson = new JSONObject(HttpUtils.get(indexUrl));
						totalTasks += clientAssetsJson.getJSONObject("objects").length();
					}
					hideDownloadStatus = true;
					uiInfo("Скачивание ассетов");
					httpClient.download(indexUrl, p + "assets" + File.separator + "indexes" + File.separator + o.getString("name"));
					JSONObject objects = clientAssetsJson.getJSONObject("objects");
					/*
					boolean b = true;
					boolean repeat = false;
					while(b) {
						try {
							for (String s: objects.keySet()) {
								JSONObject k = objects.getJSONObject(s);
								String hash = k.getString("hash");
								String sh = hash.substring(0, 2);
								String dir = p + "assets" + File.separator + "objects" + File.separator + sh + File.separator + hash;
								File file = new File(dir);
								if(!file.exists() || org.apache.commons.io.FileUtils.sizeOf(file) != k.getLong("size")) {
									String url = "https://resources.download.minecraft.net/" + sh + "/" + hash;
									uiInfo(null, "Скачивание ассетов (" + downloadedAssets + "/" + totalAssets + ")", percentD(0), null);
									WebUtils.download(url, dir);
									tasksDone++;
									downloadedAssets++;
								} else if(!repeat) {
									tasksDone++;
									downloadedAssets++;
								}
							}
							b = false;
						} catch(IOException e) {
							repeat = true;
							if(downloadFailCount > Config.getInt("downloadMaxAttempts")) throw e;
							Log.warn("download io err: " + e.toString() + ", retrying..");
							downloadFailCount++;
							Thread.sleep(500L);
						}
					}
					*/
					MultiThreadedDownloader downloader = new MultiThreadedDownloader(8, objects.length(), this, "Скачивание ассетов", false);
					for (String s: objects.keySet()) {
						JSONObject k = objects.getJSONObject(s);
						String hash = k.getString("hash");
						String sh = hash.substring(0, 2);
						String dir = p + "assets" + File.separator + "objects" + File.separator + sh + File.separator + hash;
						File file = new File(dir);
						if(!file.exists() || org.apache.commons.io.FileUtils.sizeOf(file) != k.getLong("size")) {
							downloader.add("https://resources.download.minecraft.net/" + sh + "/" + hash, dir);
						}
					}
					downloader.lock();
					downloader.stop();
					hideDownloadStatus = false;
					tasksDone++;
					continue;
				}
				if(o.optBoolean("mojang_jre")) {
					hideDownloadStatus = true;
					uiInfo("Скачивание Java");
					String allUrl = o.getString("url");
					JSONObject allJson = new JSONObject(HttpUtils.get(allUrl));
					JSONObject platform = allJson.getJSONObject(getMojangJREPlatform());
					JSONObject jre = platform.getJSONArray(mojang_jre).optJSONObject(0);
					if(jre != null) {
						String manifestUrl = jre.getJSONObject("manifest").getString("url");
						JSONObject manifestJson = new JSONObject(HttpUtils.get(manifestUrl));
						JSONObject files = manifestJson.getJSONObject("files");
						totalTasks += files.length();
						//int downloadedFiles = 0;
						//int totalFiles = files.length();
						MultiThreadedDownloader downloader = new MultiThreadedDownloader(3, files.length(), this, "Скачивание Java", true);
						for(String key: files.keySet()) {
							//downloadedFiles++;
							File file = new File(Launcher.getLibraryDir()+"mojang_jre"+File.separator+mojang_jre+File.separator+getMojangJREPlatform()+File.separator + key);
							JSONObject json = files.getJSONObject(key);
							if(json.getString("type").equals("directory")) {
								file.mkdirs();
								file.mkdir();
								continue;
							}
							JSONObject raw = json.getJSONObject("downloads").getJSONObject("raw");
							if(file.exists() && org.apache.commons.io.FileUtils.sizeOf(file) == raw.getLong("size")) continue;
							downloader.add(raw.getString("url"), file.getCanonicalPath());
							//uiInfo(null, "Скачивание Java: " + key + "("+downloadedFiles+"/"+totalFiles+")", percentD((double)downloadedFiles / (double)totalFiles), null);
							//WebUtils.download(raw.getString("url"), file.getCanonicalPath());
						}
						downloader.lock();
						downloader.stop();
					}
					hideDownloadStatus = false;
					tasksDone++;
					continue;
				}
				String url = o.getString("url");
				String dir = path(o.getString("dir"));
				if(dir.equals(File.separator)) dir = root + name;
				else if(dir.equals("temp")) dir = temp + name;
				else dir = p + dir + name;
				boolean b = true;
				while(b) {
					try {
						httpClient.download(url, dir);
						b = false;
					} catch(IOException e) {
						if(downloadFailCount > Config.getInt("downloadMaxAttempts")) throw e;
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
	
	private boolean checkClientMojangJRE() {
		String p = Launcher.getLibraryDir()+"mojang_jre"+File.separator+mojang_jre+File.separator+getMojangJREPlatform()+File.separator;
		if(!new File(p).exists()) {
			Log.info("jre folder does not exist");
			return false;
		}
		String allUrl = clientJson.getJSONObject("integrity_check").getJSONObject("mojang_jre").getString("url");
		try {
			JSONObject allJson = new JSONObject(HttpUtils.get(allUrl));
			JSONObject platform = allJson.getJSONObject(getMojangJREPlatform());
			JSONObject jre = platform.getJSONArray(mojang_jre).optJSONObject(0);
			if(jre != null) {
				String manifestUrl = jre.getJSONObject("manifest").getString("url");
				JSONObject manifestJson = new JSONObject(HttpUtils.get(manifestUrl));
				JSONObject files = manifestJson.getJSONObject("files");
				for(String key: files.keySet()) {
					File file = new File(p + key);
					JSONObject json = files.getJSONObject(key);
					if(!file.exists()) {
						Log.info("jre file " + key + " does not exist");
						return false;
					}
					if(json.getString("type").equals("directory")) {
						continue;
					}
					if(org.apache.commons.io.FileUtils.sizeOf(file) != json.getJSONObject("downloads").getJSONObject("raw").getLong("size")) {
						Log.info("jre file " + key + " has wrong size");
						return false;
					}
				}
			}
		} catch (Exception e) {
			Log.error("Failed to check jre integrity", e);
			return true;
		}
		return true;
	}

	private boolean scriptedUnzip(JSONArray j, String root, String temp) {
		modpack.setUpdateInfo(null, Text.get("state.unzipstart"), -2);
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
			modpack.setUpdateInfo(null, Text.get("state.unzipping") + ": " + name + " (0%)", percentI());
			if(!new File(from).exists()) {
				tasksDone++;
				continue;
			}
			try {
				ZipUtils.unzip(from, path);
			} catch (ZipException e) {
				if(!e.toString().contains("zip file is empty") && !e.toString().contains("probably not a zip file")) {
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

	private double percentD(double i) {
		//return percentD() + (i * (1D / (double) totalTasks) * 100D);
		//return ((double) (tasksDone+i) / (double) totalTasks) * 100D;
		return percentD();
	}

	private int percentI(double i) {
		return (int) (percentD(i));
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
			ArrayList<File> d = new ArrayList<File>();
			for (File f: libraries) d.add(f);
			d.add(new File(getClientJarPath()));
			ArrayList<String> jvmArgs = new ArrayList<>();
			jvmArgs.add("-Xms" + Config.getInt("xms") + "M");
			jvmArgs.add("-Xmx" + Config.getInt("xmx") + "M");
			jvmArgs.add("-Djava.library.path=" + getNativesDir());
			jvmArgs.add("-Dfile.encoding=UTF-8");
			if(clientJvmArgs != null) {
				for (String s : clientJvmArgs) {
					jvmArgs.add(s
							.replace("${library_directory}", getClientDir() + "libraries")
							.replace("${classpath_separator}", "" + File.pathSeparatorChar)
							);
				}
			}
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
				//appArgs.add("\"" + auth.getMojangUserProperties().replace("\"", "\\\"") + "\"");
			}
			appArgs.add("--gameDir");
			appArgs.add(getModpackDir());
			appArgs.add("--assetsDir");
			appArgs.add(getAssetsDir());
			appArgs.add("--assetIndex");
			appArgs.add(clientAssetIndex);
			if(clientTweakClasses != null) {
				for (String tw : clientTweakClasses) {
					appArgs.add("--tweakClass");
					appArgs.add(tw);
				}
			}
			if(clientExtraArgs != null) {
				for (String s : clientExtraArgs) {
					appArgs.add(s);
				}
			}
			appArgs.add("--modpackid");
			appArgs.add(modpack.id());
			appArgs.add("--modpackname");
			appArgs.add(modpack.getName());
			clientProcess = startJarProcess(new File(getModpackDir()), d, clientMainClass, jvmArgs, appArgs);
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
				clientError(Text.get("err.clientexitcode") + " " + exitCode);
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
		Launcher.inst.notifyClientStart(this);
	}

	private void clientStopped() {
		forceUpdate = false;
		running = false;
		failed = false;
		updating = false;
		clientStarted = false;
		Launcher.inst.notifyClientStop(this);
	}

	private void reset() {
		forceUpdate = false;
		running = false;
		failed = false;
		downloadFailCount = 0;
	}
	
	public String getClientDir() {
		return Launcher.getLibraryDir() + modpack.client() + File.separator;
	}
	
	public String getModpackDir() {
		return Launcher.getLibraryDir() + modpack.id() + File.separator;
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
		if(clientNewLibraries || new File(getClientDir() + "libraries.json").exists()) {
			if(clientLibrariesJson == null) {
				clientLibrariesJson = modpack.getClientLibrariesJson();
			}
			JSONArray libraries = clientLibrariesJson.getJSONArray("libraries");
			ArrayList<File> list = new ArrayList<File>();
			for (Object i: libraries) {
				JSONObject k = (JSONObject) i;
				if(k.optBoolean("downloadOnly")) continue;
				list.add(new File(getClientDir() + "libraries" + File.separator + path(k.getString("path"))));
			}
			return list.toArray(new File[0]);
		}
		Log.warn("Client libraries.json was not found! Trying to list libraries");
		return new File(getClientDir() + "libraries" + File.separator).listFiles((file) -> file.isDirectory() || file.getName().toLowerCase().endsWith(".jar"));
	}

	public String getClientJarPath() {
		return getClientDir() + "client.jar";
	}

	private void clientError(String s) {
		String x = ClientLog.getInstance().getLastException();
		if(x == null) x = "No Error";
		/*
		if(x.contains("java.lang.NoClassDefFoundError: java/util/jar/Pack200") || 
				x.contains("java.lang.ClassNotFoundException: java.util.jar.Pack200")) {
		x = "Возможное решение:\nОшибка несовместимости Forge с версией Java!!\nИспользуйте версию Java меньше 14 \n"
				+ "(8 - самый оптимальный вариант)\n" + x;
		} else if(x.contains("AppClassLoader cannot be cast to class java.net.URLClassLoader")) {
			x = "Возможное решение:\nОшибка несовместимости Forge с версией Java!!\nИспользуйте версию Java 8\n" + x;
		}
		*/
		ErrorUI.clientError(Text.get("title.clienterror"), s, x);	
	}

	private void clientError(String s, Throwable t) {
		String e = Log.exceptionToString(t);
		ErrorUI.clientError(Text.get("title.clienterror"), s, s + "\n" + e);	
	}

	private void updateFatalError(String s, Throwable t, int i) {
		String e = Log.exceptionToString(t);
		ErrorUI.showError(Text.get("title.updateerror"), s + " (" + Text.get("err.code") + ": "  + Errors.toString(i) + ")", s + " (" + Text.get("err.code") + ": " + Errors.toString(i) + ")\n" + e);	
		updateFatalError();
	}

	private void updateFatalError(String s, String s2, int i) {
		ErrorUI.showError(Text.get("title.updateerror"), s, s + ": " + s2 + " (" + Text.get("err.code") + ": " +  Errors.toString(i) + ")");
		updateFatalError();
	}

	private void updateFatalError(String s, int i1, int i2) {
		String e = Log.getTraceString(2);
		ErrorUI.showError(Text.get("title.updateerror"), s, s + ": " + Errors.toString(i1) + " (" + Text.get("err.code") + ": " + Errors.toString(i2) + ")\n" + e);
		updateFatalError();
	}

	private void updateFatalError(String s, String s2) {
		ErrorUI.showError(Text.get("title.updateerror"), s, s2);
		updateFatalError();
	}

	private void fatalError(String s) {
		Log.error(s);
		String e = Log.exceptionToString(new Exception());
		ErrorUI.showError(Text.get("title.updateerror"), s, e);
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
	
	@Override
	public void startZip(String zipFile) {
		uiInfo(null, Text.get("state.zipping") + ": " + currentUnzipFile + " (%)", percentD(0), null);
	}

	@Override
	public void doneZip(String zipFile) {
		uiInfo(null, Text.get("state.zipping") + ": " + currentUnzipFile + " (100%)", percentD(1), null);
	}

	@Override
	public void startUnzip(String zipFile) {
		uiInfo(null, Text.get("state.unzipping") + ": " + currentUnzipFile + " (0%)", percentD(0), null);
	}

	@Override
	public void unzipProgress(String currentFile, int totalPercent, int currentFilePercent) {
		uiInfo(null, Text.get("state.unzipping") + ": " + currentUnzipFile + ": " + currentFile + " (" + totalPercent + "%)", percentD(totalPercent / 100D), null);
	}

	@Override
	public void doneUnzip(String zipFile) {
		uiInfo(null, Text.get("state.unzipping") + ": " + currentUnzipFile + " (100%)", percentD(1), null);
	}

	@Override
	public void startDownload(String filename) {
		if(hideDownloadStatus) {
			return;
		}
		uiInfo(null, Text.get("state.downloading") + ": " +  filename + " (0%)", percentD(0), null);
	}
	
	@Override
	public void downloadProgress(String filename, double speed, int percent, int bytesLeft) {
		avgsum += speed;
		avgcounter++;
		if(avgcounter == 15) {
			avgspeed = avgsum / 15;
			avgsum = avgspeed;
			avgcounter = 1;
		}
		if(hideDownloadStatus) {
			return;
		}
		float s = avgspeed;
		s = Math.round(s * 100) / 100F;
		// секунды
		int left = (int)((bytesLeft/1024F/1024F) / s);
		if(s == 0) left = 0;
		//Log.debug(s + "mbs left: " + timeStr(left));
		String leftStr = "Ост.: " + timeStr(left);
		if(!Config.getBoolean("downloadLeftTime")) leftStr = "";
		uiInfo(null, Text.get("state.downloading") + ": " +  filename 
				+ " (" + speed + "Mb/s)"
				+ " (" + percent + "%)"
				, percentD(percent / 100D),
				leftStr);
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
	
	private void uiInfo(String s1, String s2, double p, String time) {
		modpack.setUpdateInfo(s1, s2, p, time);
	}
	
	protected void uiInfo(String s) {
		modpack.setUpdateInfo(null, s, percentD());
	}

	@Override
	public void doneDownload(String zipFile) {
		
	}
	
	public Process startJarProcess(File dir, List<File> classpathList, String mainClass, List<String> jvmArgs, List<String> appArgs) throws IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add(getJavaExec());
		cmd.addAll(jvmArgs);
		cmd.add("-cp");
		cmd.add(constructClassPath(classpathList));
		cmd.add(mainClass);
		cmd.addAll(appArgs);
		Log.info(cmd.toString());
		return startProcess(dir, cmd);
	}

	private Process startProcess(File dir, List<String> cmd) throws IOException {
		return new ProcessBuilder(new String[0]).command(cmd).directory(dir).start();
	}

	private String getJavaExec() {
		String p = Config.get("javapath");
		String exec = "java" + (System.getProperty("os.name").toLowerCase().contains("win") ? "w.exe" : "");
		if(p != null && p.length() > 3) {
			p = p.replace("\\", File.separator);
			p = p.replace("/", File.separator);
			if(p.endsWith("java.exe")) return p;
			if(p.endsWith("bin" + File.separator)) return p + exec;
			if(p.endsWith("bin")) return p + File.separator + exec;
			if(p.endsWith(File.separator)) return p + "bin" + File.separator + exec;
			Log.debug("1: " + p);
			return p + File.separator + "bin" + File.separator + exec;
		}
		String mojangjre = clientStartJson != null && clientStartJson.has("mojang_jre") ? clientStartJson.getString("mojang_jre") : mojang_jre;
		if(mojangjre != null) {
			String platform = getMojangJREPlatform();
			String jre = Launcher.getLibraryDir() + "mojang_jre" + File.separator + mojangjre + File.separator + platform + File.separator;
			if(new File(jre).exists()) {
				return jre + "bin" + File.separator + exec;
			}
		}
		String home = System.getProperty("java.home");
		if(home == null || home == "" || home == " " || home.length() < 2)
			throw new RuntimeException("invalid java.home value");
		Log.debug("2: " + home);
		return home + File.separator + "bin" + File.separator + exec;
	}
	
	private static String getJavaPathDir() {
		String[] paths = System.getenv("path").split(";");
		String w = null;
		for (int i = 0; i < paths.length; i++) {
			String o = paths[i];
			String s = o.toLowerCase();
			if ((s.contains("java") || s.contains("jre")) && s.endsWith("bin")) {
				if(s.contains("jdk") && w.contains("jdk")) {
					if(!o.contains("jre") && w.contains("jre")) {
						continue;
					}
				}
				w = o;
			}
		}
		return w;
	}

	private static String constructClassPath(List<File> classpathList) throws IOException {
		StringBuilder classpathBuilder = new StringBuilder();
		for (File classpathEntry : classpathList) {
			if (!classpathEntry.exists()) {
				throw new FileNotFoundException("classpath not found: " + classpathEntry.getAbsolutePath());
			}
			if (classpathBuilder.length() > 0) {
				classpathBuilder.append(File.pathSeparatorChar);
			}
			classpathBuilder.append(classpathEntry.getAbsolutePath());
		}
		return classpathBuilder.toString();
	}
	
	public static String getMojangJREPlatform() {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();
		String s = "";
		if(os.startsWith("win")) {
			if(arch.contains("64")) {
				s = "windows-x64";
			} else {
				s = "windows-x86";
			}
		} else if(os.startsWith("linux") || os.contains("nux") || os.startsWith("nix") || os.startsWith("aix")) {
			if(arch.contains("64")) {
				s = "linux";
			} else {
				s = "linux-i386";
			}
		} else if(os.contains("darwin") || os.contains("mac")) {
			if(arch.contains("arm")) {
				s = "mac-os-arm64";
			} else {
				s = "mac-os";
			}
		}
		return s;
	}

}

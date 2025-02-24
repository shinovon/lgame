package ru.lgame.launcher;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.lgame.launcher.auth.Auth;
import ru.lgame.launcher.auth.AuthStore;
import ru.lgame.launcher.ui.ErrorUI;
import ru.lgame.launcher.ui.frame.AccountsFrm;
import ru.lgame.launcher.ui.frame.LauncherFrm;
import ru.lgame.launcher.ui.frame.LoadingFrm;
import ru.lgame.launcher.ui.frame.LoggerFrm;
import ru.lgame.launcher.ui.locale.Text;
import ru.lgame.launcher.update.Modpack;
import ru.lgame.launcher.update.Updater;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.HttpUtils;
import ru.lgame.launcher.utils.logging.Log;

import static ru.lgame.launcher.utils.HashUtils.getMD5String;

/**
 * @author Shinovon
 */
public class Launcher {
	
	public static final String version = "1.4.1";
	public static final boolean DEBUG = true;
	
	public static final String string_version = "v" + version;
	
	private static final String LAUNCHER_JSON_URL = "https://nnp.nnchan.ru/lgame/launcher.php";

	public static Launcher inst;
	
	private static String launcherPath;
	public static boolean running;
	public static boolean starter;

	private ArrayList<Runnable> queuedTasks;

	protected LoadingFrm loadingFrame;
	protected AccountsFrm accountsFrame;
	protected LoggerFrm loggerFrame;
	protected LauncherFrm frame;
	
	private ArrayList<String> modpackIds;
	private ArrayList<Modpack> modpacks;
	
	private JSONObject launcherJson;

	private boolean offline;

	private Thread eventThread = new Thread("Event thread") {
		public void run() {
			while(true) {
				if(queuedTasks.size() > 0) {
					queuedTasks.get(0).run();
					queuedTasks.remove(0);
				}
				try {
					Thread.sleep(20);
				} catch (Exception e) {
					return;
				}
				Thread.yield();
			}
		}
	};
	
	public Launcher() {
		inst = this;
		queuedTasks = new ArrayList<Runnable>();
		modpackIds = new ArrayList<String>();
		modpacks = new ArrayList<Modpack>();
	}

	public void startLauncher() {
		running = true;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Log.close();
			}
		});
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					try {
						loadingFrame = new LoadingFrm();
						loadingFrame.setVisible(true);
						loadingFrame.setText(Text.get("loading.initializing"));
						loggerFrame = new LoggerFrm();
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}
		eventThread.setPriority(2); 
		eventThread.start();
		getLauncherDir();
		Log.init();
		Log.info("Launcher path set: " + getLauncherDir());
		Config.init();
		createDirsIfNecessary();
		Config.saveConfig();
		AuthStore.init();
		loadingFrame.setText(Text.get("loading.fetchingmodpacks"));
		try {
			try {
				loadModpacksFromServer();
			} catch (IOException e) {
				e.printStackTrace();
				if(loadCachedLauncherJson()) {
					offline = true;
				} else {
					if(e.getCause() != null && e.getCause() instanceof SSLHandshakeException) {
						ErrorUI.showError(Text.get("title.launchererror"), Text.get("err.ssl") + "\nhttps://java.com/ru/download", e);			
					} else {
						JOptionPane.showMessageDialog(loadingFrame, Text.get("msg.firststart"), "", JOptionPane.ERROR_MESSAGE, null);
					}
					System.exit(0);
					return;
				}
			}
		} catch (Exception e) {
			ErrorUI.showError(Text.get("title.launchererror"), Text.get("loading.fetchingmodpacks"), e);
			return;
		}
		loadingFrame.setText(Text.get("loading.initializingui"));
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new LauncherFrm();
					loadingFrame.setVisible(false);
					loadingFrame.disableExitOnClose();
					frame.setVisible(true);
					accountsFrame = new AccountsFrm();
				} catch (Throwable e) {
					ErrorUI.showError(Text.get("title.launchererror"), Text.get("loading.initializingui"), e);
					System.exit(1);
				}
			}
		});
	}

	/**
	 * Создать недостающие директории
	 */
	private void createDirsIfNecessary() {
		try {
			File f = new File(getLauncherDir());
			if(!f.exists()) f.mkdirs();
			f = new File(getCacheDir());
			if(!f.exists()) f.mkdirs();
			if(Config.get("path") != null) {
				f = new File(Config.get("path"));
				if(!f.exists()) f.mkdirs();
			}
		} catch (Exception e) {
			ErrorUI.showError(Text.get("title.error"), Text.get("err.dirs"), Log.exceptionToString(e));
		}
	}

	/**
	 * Загрузить кешированный launcher.json
	 * @return Получилось ли
	 */
	private boolean loadCachedLauncherJson() {
		File f = new File(getCacheDir() + "launcher.json");
		if(!f.exists()) return false;
		try {
			parseLauncherJson(FileUtils.getString(f));
			return true;
		} catch (IOException e) {
			Log.error("failed to load cached launcher.json", e);
			return false;
		}
	}

	/**
	 * Попытаться загрузить launcher.json с сервера
	 */
	private void loadModpacksFromServer() throws IOException {
		String s = HttpUtils.get(LAUNCHER_JSON_URL);
		FileUtils.writeString(new File(getCacheDir() + "launcher.json"), s);
		parseLauncherJson(s);
	}

	/**
	 * Поиск сборки по идентификатору
	 * @param id
	 * @return Сборка или null
	 */
	public Modpack getModpackById(String id) {
		Iterator<Modpack> i = modpacks.iterator();
		while(i.hasNext()) {
			Modpack m = i.next();
			if(m.id().equals(id)) return m;
		}
		return null;
	}
	
	/**
	 * Возвращает список сборок
	 * @return Итератор сборок
	 */
	public Iterator<Modpack> getModpacks() {
		return modpacks.iterator();
	}
	
	/**
	 * Парс launcher.json
	 * @param json Содержание файла
	 */
	public void parseLauncherJson(String json) {
		launcherJson = new JSONObject(json);
		JSONArray arr = launcherJson.getJSONArray("modpacks");
		Iterator<Object> i = arr.iterator();
		Modpack m;
		while(i.hasNext()) {
			String id = (String) i.next();
			if(!modpackIds.contains(id)) {
				modpackIds.add(id);
				modpacks.add(m = new Modpack(id).parse(launcherJson.getJSONObject(id)));
			} else m = getModpackById(id).parse(launcherJson.getJSONObject(id));
			m.getStateRst();
		}
	}

	public void refreshLauncherJson() {
		launcherJson = null;
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					try {
						loadingFrame.setText(Text.get("loading.fetchingmodpacks"));
						loadingFrame.setVisible(true);
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}
		try {
			loadModpacksFromServer();
		} catch (IOException e) {
			loadCachedLauncherJson();
		}
		loadingFrame.setVisible(false);
	}
	
	/**
	 * Проверка на оффлайн
	 */
	public boolean isOffline() {
		return offline;
	}

	/**
	 * Запустить сборку
	 */
	public void run(Auth a, Modpack m) {
		Updater.start(m, a);
	}

	/**
	 * Запустить сборку с принудительным обновлением
	 */
	public void runForceUpdate(Auth a, Modpack m) {
		Updater.startForceUpdate(m, a);
	}
	
	public void interruptEventThread() {
		eventThread.interrupt();
	}

	/**
	 * Запланировать действие
	 */
	public void queue(Runnable runnable) {
		queuedTasks.add(runnable);
	}
	
	public LauncherFrm frame() {
		return frame;
	}

	/**
	 * Получить папку лаунчера
	 * @return Путь к папке .lgame
	 */
	public static String getLauncherDir() {
		if(launcherPath != null) return launcherPath;
		String s = System.getenv("APPDATA");
		if(s == null) s = System.getProperty("user.home");
		if(s.endsWith("/") || s.endsWith("\\"))
			s = s.substring(0, s.length()-1);
		s += File.separator + ".lgame" + File.separator;
		return launcherPath = s;
	}

	/**
	 * @return Путь к файлу с настройками
	 */
	public static String getConfigPath() {
		return getLauncherDir() + "launcher.properties";
	}

	/**
	 * @return Путь к файлу с кэшем
	 */
	public static String getCacheDir() {
		return getLauncherDir() + "cache" + File.separator;
	}

	public static String getLibraryDefaultDir() {
		String s = System.getenv("APPDATA");
		if(s == null) s = System.getProperty("user.home");
		if(s.endsWith("/") || s.endsWith("\\"))
			s = s.substring(0, s.length()-1);
		s += File.separator + ".lgame" + File.separator;
		return s;
	}

	public static String getLibraryDir() {
		String p = Config.get("path");
		String s = File.separator;
		p = p.replace("/", s);
		p = p.replace("\\", s);
		if(!p.endsWith(s)) p = p + s;
		return p;
	}


	/**
	 * Получить папку с временными файлами
	 * @return Путь к временным файлам
	 */
	public static String getTempDir() {
		String s = System.getProperty("java.io.tmpdir");
		if(s.endsWith("/") || s.endsWith("\\"))
			s = s.substring(0, s.length()-1);
		s += File.separator;
		return s + "lgametemp" + File.separator;
	}
	

	public static String getTempDir(String str) {
		return getTempDir() + str + File.separator;
	}

	/**
	 * Сохранить картинку в кэш
	 * @param url Адрес
	 * @param img Изображение
	 */
	public void saveImageToCache(String url, BufferedImage img) {
		Log.debug("saveImage:" + url);
		File f = new File(getCacheDir() + "i" + getMD5String(url));
		if(f.exists()) f.delete();
		try {
			ImageIO.write(img, "jpeg", f);
		} catch (IOException e) {
		}
	}

	public void saveImageToCachePng(String url, BufferedImage img) {
		Log.debug("saveImagePng:" + url);
		File f = new File(getCacheDir() + "i" + getMD5String(url));
		if(f.exists()) f.delete();
		try {
			ImageIO.write(img, "png", f);
		} catch (IOException e) {
		}
	}

	/**
	 * Получить картинку из кэша
	 * @param url Адрес
	 * @return Кэшированное изображение
	 */
	public Image getCachedImage(String url) {
		File f = new File(getCacheDir() + "i" + getMD5String(url));
		if(!f.exists()) {
			Log.debug("getImage:" + url + "=null");
			return null;
		}
		try {
			Image i = ImageIO.read(f);
			Log.debug("getImage:" + url + "=" + i.getWidth(null) + "x" + i.getHeight(null));
			return i;
		} catch (IOException e) {
			Log.error("getCachedImage()", e);
		}
		Log.debug("getImage:" + url + "=null");
		return null;
	}
	
	public void saveValueToCache(String name, String value) {
		Log.debug("saveValue " + name + "=" + value);
		File f = new File(getCacheDir() + "v" + getMD5String(name));
		if(f.exists()) f.delete();
		try {
			FileUtils.writeString(f, value);
		} catch (IOException e) {
			Log.error("saveValueToCache()", e);
		}
	}
	
	public String getValueFromCache(String name) {
		File f = new File(getCacheDir() + "v" + getMD5String(name));
		if(!f.exists()) {
			Log.debug("getValue:" + name + "=null");
			return null;
		}
		try {
			String s = FileUtils.getString(f);
			Log.debug("getValue:" + name + "=" + s);
			return s;
		} catch (IOException e) {
			Log.error("getValueFromCache()", e);
		}
		Log.debug("getValue:" + name + "=null");
		return null;
	}

	public Auth currentAuth() {
		return AuthStore.getSelected();
	}

	public void showAccountsFrame() {
		accountsFrame.setVisible(true);
	}

	public void showLoggerFrame() {
		loggerFrame.setVisible(true);
	}

	public static String getFrmTitle() {
		if(DEBUG) {
			return "LGame Launcher debug";
		}
		return "LGame Launcher";
	}

	public LoggerFrm loggerFrame() {
		return loggerFrame;
	}

	public void pathChanged() {
		// TODO
	}

	public static void setFrameIcon(JFrame frm) {
		try {
			List<Image> images = new Vector<Image>();
			Class<?> c = Launcher.class;
			images.add(ImageIO.read(c.getResourceAsStream("/icon128.png")));
			images.add(ImageIO.read(c.getResourceAsStream("/icon64.png")));
			images.add(ImageIO.read(c.getResourceAsStream("/icon48.png")));
			images.add(ImageIO.read(c.getResourceAsStream("/icon32.png")));
			images.add(ImageIO.read(c.getResourceAsStream("/icon24.png")));
			images.add(ImageIO.read(c.getResourceAsStream("/icon16.png")));
			frm.setIconImages(images);
		} catch (Exception e) {
			Log.warn("icon load failed", e);
		}
	}

	public void notifyClientStart(Updater client) {
		frame.setVisible(false);
	}

	public void notifyClientStop(Updater client) {
		frame.setVisible(true);
	}
	
	public static void stat(JSONObject o) {
		o.put("launcher_version", version);
		if(DEBUG) {
			o.put("debug", "1");
		}
		new Thread() {
			public void run() {
				try {
					HttpUtils.postReq("https://nnp.nnchan.ru/lgame/api.php", o.toString());
				} catch (IOException e) {
				}
			}
		}.start();
	}

}

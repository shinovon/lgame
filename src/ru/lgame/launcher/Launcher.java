package ru.lgame.launcher;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.lgame.launcher.ui.LauncherFrm;
import ru.lgame.launcher.ui.LoadingFrm;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.Log;
import ru.lgame.launcher.utils.WebUtils;

public class Launcher {

	
	public static final String version = "0.1";
	public static final String string_version = "concept demo";
	
	public static final boolean DEBUG = true;
	
	private static final String LAUNCHER_JSON_URL = "http://dl.nnproject.cc/lgame/launcher.json";

	public static Launcher inst;
	
	private static String launcherPath;

	private ArrayList<Runnable> queuedTasks;

	protected LoadingFrm loadingFrame;
	protected LauncherFrm frame;
	
	private ArrayList<String> modpackIds;
	private ArrayList<Modpack> modpacks;
	
	private JSONObject launcherJson;

	private boolean offline;

	private Thread eventThread = new Thread() {
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
	
	private Launcher() {
		inst = this;
		queuedTasks = new ArrayList<Runnable>();
		modpackIds = new ArrayList<String>();
		modpacks = new ArrayList<Modpack>();
	}

	public static void main(String[] args) {
		new Launcher().startLauncher();
	}

	private void startLauncher() {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					try {
						loadingFrame = new LoadingFrm();
						loadingFrame.setVisible(true);
						loadingFrame.setText("Инициализация");
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}
		eventThread.setPriority(2);
		eventThread.start();
		getLauncherDir();
		Config.init();
		createDirsIfNecessary();
		Config.saveConfig();
		loadingFrame.setText("Получение данных о сборках");
		if(tryLoadModpacksFromServer()) {
		} else if(loadCachedLauncherJson()) {
			offline = true;
		} else {
			JOptionPane.showMessageDialog(new JPanel(), "Для первого запуска нужно подключение к интернету!");
			System.exit(0);
			return;
		}
		loadingFrame.setText("Инициализация интерфейса");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new LauncherFrm();
					loadingFrame.setVisible(false);
					frame.setVisible(true);
				} catch (Exception e) {
					showError("Ошибка лаунчера", "Инициализация интерфейса", e);
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
			showError("Ошибка", "Ошибка создания директорий", Log.exceptionToString(e));
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
	 * @return Получилось ли
	 */
	private boolean tryLoadModpacksFromServer() {
		try {
			String s = WebUtils.get(LAUNCHER_JSON_URL);
			FileUtils.writeString(new File(getCacheDir() + "launcher.json"), s);
			parseLauncherJson(s);
			return true;
		} catch (IOException e) {
			Log.error("failed to load launcher.json", e);
			return false;
		}
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
		while(i.hasNext()) {
			String id = (String) i.next();
			if(!modpackIds.contains(id)) {
				modpackIds.add(id);
				modpacks.add(new Modpack(id).parse(launcherJson.getJSONObject(id)));
			} else getModpackById(id).parse(launcherJson.getJSONObject(id));
		}
	}

	public void refreshLauncherJson() {
		launcherJson = null;
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					try {
						loadingFrame.setText("Получение данных о сборках");
						loadingFrame.setVisible(true);
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}
		if(!tryLoadModpacksFromServer()) loadCachedLauncherJson();
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
		Log.info("Launcher path set: " + s);
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

	public static String getModpacksDefaultDir() {
		String s = System.getProperty("user.home");
		if(s.endsWith("/") || s.endsWith("\\"))
			s = s.substring(0, s.length()-1);
		s += File.separator + ".lgame" + File.separator;
		return s;
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

	/**
	 * Сохранить картинку в кэш
	 * @param url Адрес
	 * @param img Изображение
	 */
	public void saveImageToCache(String url, BufferedImage img) {
		File f = new File(getCacheDir() + getMD5String(url));
		if(f.exists()) f.delete();
		try {
			ImageIO.write(img, "jpeg", f);
		} catch (IOException e) {
		}
	}

	/**
	 * Получить картинку из кэша
	 * @param url Адрес
	 * @return Кэшированное изображение
	 */
	public Image getCachedImage(String url) {
		File f = new File(getCacheDir() + getMD5String(url));
		if(!f.exists()) return null;
		try {
			return ImageIO.read(f);
		} catch (IOException e) {
			Log.error("getCachedImage()", e);
		}
		return null;
	}

	/**
	 * MD5 хэш
	 */
	public static String getMD5String(String x) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(x.getBytes());
			byte[] digest = md.digest();
			return DatatypeConverter.printHexBinary(digest).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public void showError(String title, String text, String trace) {
		Log.error("showError(): " + trace);
		JDialog dialog = new JDialog(frame, title, true);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel pane = new JPanel();
        contentPane.add(pane, BorderLayout.CENTER);
		pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		pane.setLayout(new BorderLayout());
		if(text != null && text.length() > 0) {
			JTextPane infopane = new JTextPane();
			infopane.setBackground(SystemColor.menu);
			infopane.setEditable(false);
			infopane.setText(text);
			pane.add(infopane, BorderLayout.NORTH);
		}
		if(trace != null) {
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(new Dimension(600, 200));
			pane.add(scrollPane, BorderLayout.CENTER);
			JTextArea textArea = new JTextArea();
			textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
			textArea.setEditable(false);
			textArea.setText(trace);
			scrollPane.setViewportView(textArea);
		}
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPane, BorderLayout.SOUTH);
		{
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			dialog.getRootPane().setDefaultButton(okButton);
		}
		dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        dialog.show();
        dialog.dispose();
	}

	public void showError(String title, String text) {
		showError(title, text, (String) null);
	}
	
	public void showError(String title, String text, Throwable e) {
		showError(title, text, Log.exceptionToString(e));
	}

}

package ru.lgame.launcher;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.lgame.launcher.ui.LauncherFrm;
import ru.lgame.launcher.ui.LoadingFrm;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.Log;
import ru.lgame.launcher.utils.WebUtils;

public class Launcher {
	
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
						loadingFrame.setText("�������������");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		eventThread.setPriority(2);
		eventThread.start();
		getLauncherDir();
		createDirsIfNecessary();
		Config.init();
		loadingFrame.setText("��������� ������ � �������");
		if(tryLoadModpacksFromServer()) {
		} else if(loadCachedModpacks()) {
			offline = true;
		} else {
			JOptionPane.showMessageDialog(new JPanel(), "��� ������� ������� ����� ����������� � ���������!");
			System.exit(0);
			return;
		}
		loadingFrame.setText("������������� ����������");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new LauncherFrm();
					loadingFrame.setVisible(false);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void createDirsIfNecessary() {
		File f = new File(getLauncherDir());
		if(!f.exists()) f.mkdirs();
		f = new File(getCacheDir());
		if(!f.exists()) f.mkdirs();
	}

	private boolean loadCachedModpacks() {
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

	public Modpack getModpackById(String id) {
		Iterator<Modpack> i = modpacks.iterator();
		while(i.hasNext()) {
			Modpack m = i.next();
			if(m.id().equals(id)) return m;
		}
		return null;
	}
	
	public Iterator<Modpack> getModpacks() {
		return modpacks.iterator();
	}
	
	public void parseLauncherJson(String json) {
		launcherJson = new JSONObject(json);
		JSONArray arr = launcherJson.getJSONArray("modpacks");
		Iterator<Object> i = arr.iterator();
		while(i.hasNext()) {
			String id = (String) i.next();
			modpackIds.add(id);
			modpacks.add(new Modpack(id).parse(launcherJson.getJSONObject(id)));
		}
	}
	
	public boolean isOffline() {
		return offline;
	}

	public void run(Auth auth) {
		
	}
	
	public void wakeEventThread() {
		eventThread.interrupt();
	}

	public void runForceUpdate(Auth auth) {
		
	}

	public void queue(Runnable runnable) {
		queuedTasks.add(runnable);
	}

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

	public static String getConfigPath() {
		return getLauncherDir() + "launcher.properties";
	}

	public static String getCacheDir() {
		return getLauncherDir() + "cache" + File.separator;
	}

	public static String getTempDir() {
		String s = System.getProperty("java.io.tmpdir");
		if(s.endsWith("/") || s.endsWith("\\"))
			s = s.substring(0, s.length()-1);
		s += File.separator;
		return s + "lgametemp" + File.separator;
	}

	public void saveImageToCache(String url, BufferedImage img) {
		File f = new File(getCacheDir() + getMD5String(url));
		if(f.exists()) f.delete();
		try {
			ImageIO.write(img, "jpeg", f);
		} catch (IOException e) {
		}
	}

	public Image getCachedImage(String url) {
		File f = new File(getCacheDir() + getMD5String(url));
		if(!f.exists()) return null;
		try {
			return ImageIO.read(f);
		} catch (IOException e) {
			Log.error("getCachedImage failed: " + e);
		}
		return null;
	}

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

}

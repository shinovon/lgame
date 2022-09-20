package ru.lgame.launcher.update;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.ui.pane.MiniModpackPane;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.LauncherOfflineException;
import ru.lgame.launcher.utils.WebUtils;
import ru.lgame.launcher.utils.logging.Log;

/**
 * Объект сборки
 * @author Shinovon
 */
public class Modpack {
	
	private String id;
	private String name;
	private String category;
	private String description;
	private String last_version;
	private String image;
	private String client_id;
	private String update_data;
	private String client_update_data;
	private String client_start_data;
	private String client_libraries_data;
	private String type;
	private Date releaseDate;
	private MiniModpackPane ui;
	
	private JSONObject updateJson;
	private JSONObject clientUpdateJson;
	private JSONObject clientStartJson;
	private JSONObject clientLibrariesJson;
	
	private int cachedState;
	private String client_assets_data;
	
	public Modpack(String id) {
		this.id = id;
	}
	
	public Modpack parse(JSONObject o) {
		cachedState = -999;
		updateJson = null;
		clientUpdateJson = null;
		name = o.optString("name", id);
		category = o.optString("category");
		image = o.optString("img", null);
		description = o.optString("description");
		last_version = o.optString("last_version");
		client_id = o.optString("client", null);
		update_data = o.optString("update_data", null);
		releaseDate = parseDate(o.optString("release_date", null));
		client_update_data = o.optString("client_update_data", null);
		client_start_data = client_update_data.replace("/update.json", "/start.json");
		client_libraries_data = client_update_data.replace("/update.json", "/libraries.json");
		type = o.optString("type");
		return this;
	}

	public String id() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategories() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public String getImageUrl() {
		return image;
	}

	public String getLastVersion() {
		return last_version;
	}

	public String client() {
		return client_id;
	}

	public String getType() {
		return type;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Создает панель, грузит картинку
	 */
	public MiniModpackPane createPanel() {
		if(ui != null) {
			ui.setInformation(name, description, category);
			ui.setModpack(this);
			return ui;
		}
		MiniModpackPane mp = new MiniModpackPane(id);
		mp.setInformation(name, description, category);
		mp.setModpack(this);
		//Launcher.inst.queue(new Runnable() {
		//	public void run() {
		l: {
			String url = getImageUrl();
			if(url != null) {
				final boolean cache;
				try {
					Image img = Launcher.inst.getCachedImage(url);
					if(img == null) {
						img = ImageIO.read(new URL(url));
						cache = false;
					} else cache = true;
					if(img != null) {
						mp.setImage(img);
						final Image cimg = img;
						Launcher.inst.queue(new Runnable() {
							public void run() {
								if(!cache) Launcher.inst.saveImageToCache(url, (BufferedImage) cimg);
								try {
									if(cache) {
										Image img = ImageIO.read(new URL(url));
										Launcher.inst.saveImageToCache(url, (BufferedImage) img);
										mp.setImage(img);
									}
								} catch (Exception e) {
									Log.error("Failed to load modpack image: " + e);
								}
							}
						});
						break l;
					}
				} catch (Exception e) {
					Log.error("Failed to load modpack image: " + e);
				}
			}
			/*
			try {
				mp.setImage(ImageIO.read(getClass().getResourceAsStream("/noimg.png")));
			} catch (IOException e) {
				Log.error("Failed to load modpack default image:" + e);
			}
			*/
		}
		//	}
		//});
		return ui = mp;
	}
	
	public boolean isUpdating() {
		return this.equals(Updater.getNowUpdatingModpack());
	}

	public boolean isStarted() {
		return this.equals(Updater.getNowRunningModpack());
	}

	public int getStateRst() {
		if(isUpdating() || isStarted()) return cachedState = 1;
		return cachedState = Updater.getModpackState(this);
	}

	public int getState() {
		if(cachedState != -999) return cachedState;
		if(isUpdating() || isStarted()) return cachedState = 1;
		return cachedState = Updater.getModpackState(this);
	}
	
	public void setUpdateInfo(String s1, String s2, double percent) {
		if(ui != null) ui.setUpdateInfo(s1, s2, percent, null);
	}

	public void setUpdateInfo(String s1, String s2, double p, String time) {
		if(ui != null) ui.setUpdateInfo(s1, s2, p, time);
		
	}
	
	public JSONObject getUpdateJson() throws IOException {
		if(updateJson == null) updateJson = new JSONObject(WebUtils.get(update_data));
		return updateJson;
	}
	
	public JSONObject getClientUpdateJson() throws IOException {
		if(clientUpdateJson == null) clientUpdateJson = new JSONObject(WebUtils.get(client_update_data));
		return clientUpdateJson;
	}
	
	public JSONObject getClientStartJson() {
		if(clientStartJson == null) {
			try {
				clientStartJson = new JSONObject(WebUtils.get(client_start_data));
			} catch (IOException e) {
				try {
					clientStartJson = new JSONObject(FileUtils.getString(getClientDir() + "start.json"));
				} catch (IOException e2) {
				}
			}
		}
		return clientStartJson;
	}

	public JSONObject getUpdateJson(boolean b) throws IOException {
		if(b == false) return getUpdateJson();
		return updateJson = new JSONObject(WebUtils.get(update_data));
	}
	
	public JSONObject getClientStartJson(boolean b) {
		if(b == false) return getClientStartJson();
		try {
			return clientStartJson = new JSONObject(WebUtils.get(client_start_data));
		} catch (IOException e) {
			throw new LauncherOfflineException(e);
		}
	}
	
	public JSONObject getClientUpdateJson(boolean b) throws IOException {
		if(b == false) return getClientUpdateJson();
		return clientUpdateJson = new JSONObject(WebUtils.get(client_update_data));
	}
	
	public String getClientDir() {
		return Launcher.getLibraryDir() + client() + File.separator;
	}
	
	public String getModpackDir() {
		return Launcher.getLibraryDir() + id() + File.separator;
	}

	public JSONObject getClientLibrariesJson() {
		if(clientLibrariesJson == null) {
			try {
				clientLibrariesJson = new JSONObject(WebUtils.get(client_libraries_data));
			} catch (IOException e) {
				try {
					clientLibrariesJson = new JSONObject(FileUtils.getString(getClientDir() + "libraries.json"));
				} catch (IOException e2) {
				}
			}
		}
		return clientLibrariesJson;
	}

	public void setClientLibrariesURL(String url) {
		client_libraries_data = url;
	}

	private static Date parseDate(String str) {
		if(str == null) {
			str = "2.2.2022";
		}
		String[] s = str.split("\\.", 3);
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(s[2]), Integer.parseInt(s[1]), Integer.parseInt(s[0]));
		return cal.getTime();
	}

}

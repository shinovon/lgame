package ru.lgame.launcher;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import ru.lgame.launcher.ui.ModpackPanel;
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
	private ModpackPanel ui;
	
	private JSONObject updateJson;
	private JSONObject clientUpdateJson;
	private int cachedState;
	private JSONObject clientStartJson;
	private String client_start_data;
	
	public Modpack(String id) {
		this.id = id;
	}
	
	public Modpack parse(JSONObject o) {
		cachedState = -999;
		updateJson = null;
		clientUpdateJson = null;
		name = o.optString("name", id);
		category = o.optString("category");
		description = o.optString("category");
		image = o.optString("img", null);
		description = o.optString("description");
		last_version = o.optString("last_version");
		client_id = o.optString("client", null);
		update_data = o.optString("update_data", null);
		client_update_data = o.optString("client_update_data", null);
		client_start_data = client_update_data.replace("/update.json", "/start.json");
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

	/**
	 * Создает панель, грузит картинку
	 */
	public ModpackPanel createPanel() {
		if(ui != null) {
			ui.setInformation(name, description);
			ui.setModpack(this);
			return ui;
		}
		ModpackPanel mp = new ModpackPanel(id);
		mp.setInformation(name, description);
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
			try {
				mp.setImage(ImageIO.read(getClass().getResourceAsStream("/noimg.png")));
			} catch (IOException e) {
				Log.error("Failed to load modpack default image:" + e);
			}
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
	
	public void setUpdateInfo(String s1, String s2, int percent) {
		if(ui != null) ui.setUpdateInfo(s1, s2, percent);
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
		return Config.get("path") + client() + File.separator;
	}
	
	public String getModpackDir() {
		return Config.get("path") + id() + File.separator;
	}

}

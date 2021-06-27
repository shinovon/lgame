package ru.lgame.launcher;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import ru.lgame.launcher.ui.ModpackPanel;
import ru.lgame.launcher.utils.Log;
import ru.lgame.launcher.utils.WebUtils;

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
	
	public Modpack(String id) {
		this.id = id;
	}
	
	public Modpack parse(JSONObject o) {
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

	public int getState() {
		return Updater.getModpackState(this);
	}
	
	public JSONObject getUpdateJson() throws IOException {
		if(updateJson == null) updateJson = new JSONObject(WebUtils.get(update_data));
		return updateJson;
	}
	
	public JSONObject getClientUpdateJson() throws IOException {
		if(clientUpdateJson == null) clientUpdateJson = new JSONObject(WebUtils.get(client_update_data));
		return clientUpdateJson;
	}

	public JSONObject getUpdateJson(boolean b) throws IOException {
		if(b == false) return getUpdateJson();
		return updateJson = new JSONObject(WebUtils.get(update_data));
	}
	
	public JSONObject getClientUpdateJson(boolean b) throws IOException {
		if(b == false) return getClientUpdateJson();
		return clientUpdateJson = new JSONObject(WebUtils.get(client_update_data));
	}

}

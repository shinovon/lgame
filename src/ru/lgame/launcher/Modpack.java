package ru.lgame.launcher;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import ru.lgame.launcher.ui.ModpackPanel;
import ru.lgame.launcher.utils.Log;

/**
 * Объект сборки
 * @author Shinovon
 */
public class Modpack {
	
	private String id;
	private String name;
	private String category;
	private String description;
	private String lastVersion;
	private String image;
	private ModpackPanel ui;
	
	public Modpack(String id) {
		this.id = id;
	}
	
	public Modpack parse(JSONObject o) {
		name = o.optString("name", id);
		category = o.optString("category");
		description = o.optString("category");
		image = o.optString("img", null);
		description = o.optString("description");
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
		return lastVersion;
	}

	/**
	 * Создает панель, грузит картинку
	 */
	public ModpackPanel createPanel() {
		if(ui != null) return ui;
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

}

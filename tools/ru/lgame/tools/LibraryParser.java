package ru.lgame.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class LibraryParser {
	
	public static void main(String[] args) {
		File f = new File("G:\\Games\\Minecraft\\versions\\Forge 1.19.2\\Forge 1.19.2.json");
		JSONArray res = new JSONArray();
		ArrayList<String> al = new ArrayList<String>();
		try {
			JSONObject _j = new JSONObject(getString(f.toString()));
			JSONArray l = _j.getJSONArray("libraries");
			for(Object o: l) {
				JSONObject j = (JSONObject) o;
				JSONArray rules = j.optJSONArray("rules");
				String name = j.getString("name");
				String[] n = name.split(":");
				String dir = n[0].replace(".", "/")+"/"+n[1]+"/"+n[2];
				String file = n[1]+"-"+n[2];
				if(n.length > 3) {
					file += "-"+n[3];
				}
				String path = dir + "/" + file;
				if(rules != null) {
					JSONObject ro = rules.getJSONObject(0);
					//String osname = null;
					if(ro.getString("action").equals("allow") && ro.has("os") &&
							((/*osname = */ro.getJSONObject("os").getString("name")).equals("osx")
									/*|| osname.equals("linux")*/)) {
						continue;
					}
				}
				if(al.contains(name)) continue;
				al.add(name);
				if(j.getJSONObject("downloads").has("artifact")) {
					JSONObject r = new JSONObject();
					JSONObject dj = j.getJSONObject("downloads").getJSONObject("artifact");
					r.put("name", name);
					r.put("path", path + ".jar");
					String url = dj.getString("url");
					if(url.startsWith("/")) url = "https://eu02-www.tlaun.ch/repo/" + url;
					r.put("url", url);
					r.put("sha1", dj.getString("sha1"));
					r.put("size", dj.getInt("size"));
					if(j.optBoolean("downloadOnly")) {
						r.put("downloadOnly", "true");
					}
					res.put(r);
				}
				JSONObject cj = j.getJSONObject("downloads").optJSONObject("classifiers");
				if(cj != null) {
					JSONObject win = cj.optJSONObject("natives-windows");
					if(win != null) {
						JSONObject r2 = new JSONObject();
						r2.put("natives", true);
						r2.put("name", j.getString("name"));
						r2.put("path", path + "-natives-windows.jar");
						r2.put("url", win.getString("url"));
						r2.put("sha1", win.getString("sha1"));
						r2.put("size", win.getInt("size"));
						res.put(r2);
					}
				}
			}
			System.out.println(res.toString(3).replace("   ", "\t"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getString(String file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
	}

}

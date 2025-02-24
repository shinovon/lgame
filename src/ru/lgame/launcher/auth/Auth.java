package ru.lgame.launcher.auth;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import org.json.JSONObject;

import ru.lgame.launcher.utils.HttpUtils;
import ru.lgame.launcher.utils.logging.Log;

/**
 * Используется для авторизации
 * @author Shinovon
 */
public final class Auth {

	private AuthType type;
	
	private String username;
	private String uuid;
	
	private String mail;
	private String password;
	
	private String accessToken;
	private String clientToken;
	
	private Auth() {
	}

	private Auth(String username, String uuid) {
		this.username = username;
		this.uuid = uuid;
		this.type = AuthType.USERNAME;
	}

	public static Auth get(String username) {
		return new Auth(username, null);
	}

	public static Auth get(String username, String uuid) {
		return new Auth(username, uuid);
	}

	public static Auth getEly(String mail, String password) throws Exception {
		Auth auth = new Auth();
		auth.type = AuthType.ELY;
		auth.mail = mail;
		auth.password = password;
		
		auth.loginEly();
		
		return auth;
	}

	public static Auth parseEly(String string) throws Exception {
		String[] split = string.split(";");
		
		Auth auth = new Auth();
		auth.type = AuthType.ELY;
		
		auth.username = split[0];
		auth.uuid = split[1];
		auth.accessToken = split[2];
		auth.clientToken = split[3];
		
		auth.checkAuth();
		
		return auth;
	}

	private void loginEly() throws Exception {
		JSONObject params = new JSONObject();
		params.put("username", mail);
		params.put("password", password);
		params.put("clientToken", UUID.randomUUID().toString());
		
		JSONObject res = new JSONObject(HttpUtils.postUtf("https://authserver.ely.by/auth/authenticate", params.toString(), "application/json"));
		
		if (res.has("error")) {
			throw new Exception(res.getString("error") + ": " + res.optString("errorMessage"));
		}
		
		accessToken = res.getString("accessToken");
		clientToken = res.getString("clientToken");
		
		JSONObject selectedProfile = res.getJSONObject("selectedProfile");
		uuid = selectedProfile.getString("id");
		username = selectedProfile.getString("name");
	}
	
	private boolean validateEly() throws IOException {
		JSONObject params = new JSONObject();
		params.put("accessToken", accessToken);
		
		return !new JSONObject(HttpUtils.postUtf("https://authserver.ely.by/auth/validate", params.toString(), "application/json"))
				.has("error");
	}

	private boolean refreshEly() throws IOException {
		JSONObject params = new JSONObject();
		params.put("accessToken", accessToken);
		params.put("clientToken", clientToken);
		
		JSONObject res = new JSONObject(HttpUtils.postUtf("https://authserver.ely.by/auth/refresh", params.toString(), "application/json"));
		
		if (res.has("error")) return false;
		
		accessToken = res.getString("accessToken");
		clientToken = res.getString("clientToken");
		
		JSONObject selectedProfile = res.optJSONObject("selectedProfile");
		if (selectedProfile != null) {
			uuid = selectedProfile.getString("id");
			username = selectedProfile.getString("name");
		}
		return true;
	}

	public boolean isCracked() {
		return type == AuthType.USERNAME;
	}
	
	public boolean isMojang() {
		return type == AuthType.MOJANG;
	}
	
	public boolean isMicrosoft() {
		return type == AuthType.MICROSOFT;
	}
	
	public boolean isLGame() {
		return type == AuthType.LGAME;
	}
	
	public boolean isEly() {
		
		return type == AuthType.ELY;
	}
	
	public String getUsername() {
		return username;
	}

	public String getUUID() {
		return uuid;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String export() {
		if (isCracked()) {
			String s = username;
			if (uuid != null) {
				s += ";" + uuid;
			}
			return s;
		}
		if (isEly()) {
			return username + ";" + uuid + ";" + accessToken + ";" + clientToken;
		}
		return username;
	}

	public int checkAuth() {
		if (isCracked()) return 0;
		if (isEly()) {
			try {
				if (validateEly()) return 0;
				return refreshEly() ? 0 : 1;
			} catch (IOException e) {
				Log.error("Ely auth validation failed", e);
				return -1;
			}
		}
		return -1;
	}
	
	public String toString() {
		return "Auth {" + getType().toString() + "," + getUsername() + "}";
	}
	
	public AuthType getType() {
		return type;
	}

}

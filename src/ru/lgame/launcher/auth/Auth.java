package ru.lgame.launcher.auth;

import java.util.Base64;

/**
 * Используется для авторизации
 * @author Shinovon
 */
public final class Auth {

	private AuthType type;
	
	private String username;
	
	private String mail;
	private String password;
	
	private Auth() {
	}

	private Auth(String username) {
		this.username = username;
		this.type = AuthType.USERNAME;
	}

	public static Auth fromUsername(String username) {
		return new Auth(username);
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
	
	public String getUsername() {
		return username;
	}

	public String getMojangUUID() {
		return null;
	}
	
	public String getNNIDAuthToken() {
		return null;
	}

	public int checkAuth() {
		if(isCracked()) return 0;
		return -1;
	}
	
	public String toString() {
		return "Auth {" + getType().toString() + "," + getUsername() + "}";
	}
	
	public AuthType getType() {
		return type;
	}
	
	private String[] decrypt(String enc) {
		try {
			String s = new String(Base64.getDecoder().decode(enc), "UTF-8");
			return s.split(":");
		} catch (Exception e) {
			return null;
		}
	}

}

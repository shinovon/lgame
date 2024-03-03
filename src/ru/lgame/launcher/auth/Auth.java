package ru.lgame.launcher.auth;

import java.net.Proxy;
import java.util.Base64;
import java.util.Map;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

/**
 * Используется для авторизации
 * @author Shinovon
 */
public final class Auth {

	private boolean cracked;
	private boolean mojang;
	private boolean microsoft;
	private boolean lgame;
	
	private String username;
	
	private String mojangUUID;
	private String mojangAuthToken;
	private YggdrasilUserAuthentication mojangAuthInst;
	private String M;
	private String P;
	
	private Auth() {
	}

	private Auth(String username) {
		this.username = username;
		this.cracked = true;
	}

	/**
	 * Создает объект авторизации с никнеймом
	 */
	public static Auth fromUsername(String username) {
		return new Auth(username);
	}

	/**
	 * Создает объект авторизации с авторизации Mojang
	 * @throws Exception 
	 */
	public static Auth fromMojang(String email, String password) throws Exception {
		return new Auth().mojangAuth(email, password);
	}
	
	public static Auth fromMojang(String enc) throws Exception {
		return new Auth().mojangAuth(enc);
	}
	
	public static Auth fromMojangStorage(Map<String, Object> storage) throws Exception {
		return new Auth().mojangAuth(storage);
	}
	
	private Auth mojangAuth(String enc) throws Exception {
		String[] s = decrypt(enc);
		createSession(s[0], s[1], Proxy.NO_PROXY);
		return this;
	}

	private Auth mojangAuth(String email, String password) throws Exception {
		createSession(email, password, Proxy.NO_PROXY);
		return this;
	}
	
	private Auth mojangAuth(Map<String, Object> storage) throws Exception {
		createSessionFromStorage(storage, Proxy.NO_PROXY);
		return this;
	}
	
	private void createSession(String username, String password, Proxy proxy) throws Exception {
		YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy, "");
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service
				.createUserAuthentication(Agent.MINECRAFT);
		auth.setUsername(this.M = username);
		auth.setPassword(this.P = password);

		auth.logIn();
		this.mojang = true;
		this.mojangAuthInst = auth;
		this.username = auth.getSelectedProfile().getName();
		this.mojangUUID = auth.getSelectedProfile().getId().toString();
		this.mojangAuthToken = auth.getAuthenticatedToken();
	}
	
	private void createSessionFromStorage(Map<String, Object> storage, Proxy proxy) throws Exception {
		YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy, "");
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service
				.createUserAuthentication(Agent.MINECRAFT);
		auth.loadFromStorage(storage);

		auth.logIn();
		this.mojang = true;
		this.mojangAuthInst = auth;
		this.username = auth.getSelectedProfile().getName();
		this.mojangUUID = auth.getSelectedProfile().getId().toString();
		this.mojangAuthToken = auth.getAuthenticatedToken();
	}

	public boolean isCracked() {
		return cracked;
	}
	
	public boolean isMojang() {
		return mojang;
	}
	
	public boolean isMicrosoft() {
		return microsoft;
	}
	
	public boolean isLGame() {
		return lgame;
	}
	
	public String getMojangUUID() {
		return mojangUUID;
	}
	
	public String getMojangAuthToken() {
		return mojangAuthToken;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getNNIDAuthToken() {
		return null;
	}

	public Map<String, Object> mojangForStorage() {
		return this.mojangAuthInst.saveForStorage();
	}

	public int checkAuth() {
		if(isCracked()) return 0;
		if(isMojang()) {
			try {
				this.mojangAuthInst.logIn();
				return 0;
			} catch(InvalidCredentialsException e) { 
				e.printStackTrace();
				return -2;
			} catch (Exception e) {
				e.printStackTrace();
				return -3;
			}
		}
		return -1;
	}
	
	public String toString() {
		return "Auth {" + getType() + "," + getUsername() + "}";
	}
	
	public String getType() {
		if(isCracked()) return "CRACKED";
		if(isMojang()) return "MOJANG";
		if(isMicrosoft()) return "MICROSOFT";
		if(isLGame()) return "LGAME";
		return "UNKNOWN";
	}
	
	private String[] decrypt(String enc) {
		try {
			String s = new String(Base64.getDecoder().decode(enc), "UTF-8");
			return s.split(":");
		} catch (Exception e) {
			return null;
		}
	}

	String mojangMailPasswordEncrypted() {
		try {
			return new String(Base64.getEncoder().encode((M + ":" + P).getBytes("UTF-8")), "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	public String getMojangUserProperties() {
		if(!isMojang()) return null;
		return mojangAuthInst.getUserProperties().toString();
	}

}

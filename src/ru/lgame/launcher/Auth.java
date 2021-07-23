package ru.lgame.launcher;

import java.net.Proxy;

import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

/**
 * Используется для авторизации
 * @author Shinovon
 */
public final class Auth {

	private boolean cracked;
	private boolean mojang;
	private String username;
	private String mojangUUID;
	private String mojangAuthToken;
	
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
	
	private Auth mojangAuth(String email, String password) throws Exception {
		createSession(email, password, Proxy.NO_PROXY);
		return this;
	}
	
	private void createSession(String username, String password, Proxy proxy) throws Exception {
	    YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(proxy, "");
	    YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service
	            .createUserAuthentication(Agent.MINECRAFT);

	    auth.setUsername(username);
	    auth.setPassword(password);

	    auth.logIn();
	    username = auth.getSelectedProfile().getName();
	    mojangUUID = auth.getSelectedProfile().getId().toString();
	    mojangAuthToken = auth.getAuthenticatedToken();
	}
	 

	public boolean isCracked() {
		return cracked;
	}
	
	public boolean isMojang() {
		return mojang;
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

}

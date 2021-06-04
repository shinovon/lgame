package ru.lgame.launcher;

/**
 * Используется для авторизации
 * @author Shinovon
 */
public final class Auth {

	private boolean cracked;
	private String username;
	
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
	
	public boolean isCracked() {
		return cracked;
	}
	
	public String getUsername() {
		return username;
	}
	
	/**
	 * @deprecated не готово
	 */
	public String getAuthToken() {
		return null;
	}

}

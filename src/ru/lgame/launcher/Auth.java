package ru.lgame.launcher;

public final class Auth {

	private boolean cracked;
	private String username;
	
	private Auth() {
	}

	private Auth(String username) {
		this.username = username;
		this.cracked = true;
	}

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
	 * @deprecated Не готово
	 */
	public String getAuthToken() {
		return null;
	}

}

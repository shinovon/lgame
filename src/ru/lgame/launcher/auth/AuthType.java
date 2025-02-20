package ru.lgame.launcher.auth;

public enum AuthType {
	USERNAME("CRACKED"), MOJANG("MOJANG"), MICROSOFT("MICROSOFT"), LGAME("LGAME"), UNKNOWN("UNKNOWN");

	private String name;
	
	AuthType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
}

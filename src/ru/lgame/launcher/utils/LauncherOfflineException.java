package ru.lgame.launcher.utils;

import java.io.IOException;

public class LauncherOfflineException extends RuntimeException {

	public LauncherOfflineException() {
	}

	public LauncherOfflineException(IOException e) {
		super(e);
	}

	private static final long serialVersionUID = 2453176167102030014L;

}

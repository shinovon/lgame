package ru.lgame.launcher.utils;

import java.io.IOException;

/**
 * @since 0.5
 * @author Shinovon
 */
public class LauncherOfflineException extends RuntimeException {

	public LauncherOfflineException() {
		super();
	}

	public LauncherOfflineException(IOException e) {
		super(e);
	}

	private static final long serialVersionUID = 2453176167102030014L;

}

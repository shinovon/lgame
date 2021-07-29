package org.apache.logging.log4j;

import ru.lgame.launcher.utils.logging.LoggerImpl;

public class LogManager {

	public static Logger getLogger() {
		return new LoggerImpl("AuthLib");
	}
}

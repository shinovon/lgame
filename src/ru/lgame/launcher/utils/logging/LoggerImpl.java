package ru.lgame.launcher.utils.logging;

import org.apache.logging.log4j.Logger;

/**
 * Подмена log4j логов для перехвата логов библиотек
 * @author Shinovon
 */
public class LoggerImpl implements Logger {
	
	private String sec;

	public LoggerImpl(String sec) {
		this.sec = sec;
	}
	
	private static String format(String s, Object... o) {
		for(Object f: o) {
			s = s.replaceFirst("{}", String.valueOf(f));
		}
		return s;
	}

	private String sec() {
		return sec;
	}

	@Override
	public void debug(Object o) {
		Log.log("DEBUG", sec(), String.valueOf(o));
	}

	@Override
	public void debug(Object o, Throwable e) {
		Log.log("DEBUG", sec(), String.valueOf(o) + "\n" + Log.exceptionToString(e));
	}

	@Override
	public void debug(String s) {
		Log.log("DEBUG", sec(), s);
	}

	@Override
	public void debug(String s, Object... o) {
		Log.log("DEBUG", sec(), format(s, o));
	}

	@Override
	public void debug(String s, Object o) {
		Log.log("DEBUG", sec(), format(s, o));
	}

	@Override
	public void debug(String s, Object o1, Object o2) {
		Log.log("DEBUG", sec(), format(s, o1, o2));
	}

	@Override
	public void debug(String s, Object o1, Object o2, Object o3) {
		Log.log("DEBUG", sec(), format(s, o1, o2, o3));
	}

	@Override
	public void error(String s) {
		Log.log("ERROR", sec(), s);
	}

	@Override
	public void error(String s, Object... o) {
		Log.log("DEBUG", sec(), format(s, o));
	}

	@Override
	public void error(String s, Throwable e) {
		Log.log("ERROR", sec(), s + ": " + Log.exceptionToString(e));
	}

	@Override
	public void error(String s, Object o) {
		Log.log("ERROR", sec(), format(s, o));
	}
	
	@Override
	public void error(String s, Object o1, Object o2) {
		Log.log("ERROR", sec(), format(s, o1, o2));
	}

	@Override
	public void error(String s, Object o1, Object o2, Object o3) {
		Log.log("ERROR", sec(), format(s, o1, o2, o3));
	}

	@Override
	public void fatal(String s) {
		Log.log("FATAL", sec(), s);
	}

	@Override
	public void fatal(String s, Object... o) {
		Log.log("DEBUG", sec(), format(s, o));
	}

	@Override
	public void fatal(String s, Throwable e) {
		Log.log("FATAL", sec(), s + ": " + Log.exceptionToString(e));
	}

	@Override
	public void fatal(String s, Object o) {
		Log.log("FATAL", sec(), format(s, o));
	}

	@Override
	public void fatal(String s, Object o1, Object o2) {
		Log.log("FATAL", sec(), format(s, o1, o2));
	}

	@Override
	public void fatal(String s, Object o1, Object o2, Object o3) {
		Log.log("FATAL", sec(), format(s, o1, o2, o3));
	}

	@Override
	public void info(Object o) {
		Log.log("INFO ", sec(), String.valueOf(o));
	}

	@Override
	public void info(Object o, Throwable e) {
		Log.log("INFO ", sec(), String.valueOf(o) + "\n" + Log.exceptionToString(e));
	}

	@Override
	public void info(String s) {
		Log.log("INFO ", sec(), s);
	}

	@Override
	public void info(String s, Object... o) {
		Log.log("INFO ", sec(), format(s, o));
	}

	@Override
	public void info(String s, Throwable e) {
		Log.log("INFO ", sec(), String.valueOf(s) + "\n" + Log.exceptionToString(e));
	}

	@Override
	public void info(String s, Object o) {
		Log.log("INFO ", sec(), format(s, o));
	}

	@Override
	public void info(String s, Object o1, Object o2) {
		Log.log("INFO ", sec(), format(s, o1, o2));
	}

	@Override
	public void info(String s, Object o1, Object o2, Object o3) {
		Log.log("INFO ", sec(), format(s, o1, o2, o3));
	}

	@Override
	public void warn(Object o) {
		Log.log("WARN ", sec(), String.valueOf(o));
	}

	@Override
	public void warn(Object o, Throwable e) {
		Log.log("WARN ", sec(), String.valueOf(o) + "\n" + Log.exceptionToString(e));
	}

	@Override
	public void warn(String s) {
		Log.log("WARN ", sec(), s);
	}

	@Override
	public void warn(String s, Object... o) {
		Log.log("WARN ", sec(), format(s, o));
	}

	@Override
	public void warn(String s, Throwable e) {
		Log.log("WARN ", sec(), String.valueOf(s) + "\n" + Log.exceptionToString(e));
	}

	@Override
	public void warn(String s, Object o) {
		Log.log("WARN ", sec(), format(s, o));
	}

	@Override
	public void warn(String s, Object o1, Object o2) {
		Log.log("WARN ", sec(), format(s, o1, o2));
	}

	@Override
	public void warn(String s, Object o1, Object o2, Object o3) {
		Log.log("WARN ", sec(), format(s, o1, o2, o3));
	}

}

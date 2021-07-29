package ru.lgame.launcher.utils.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import ru.lgame.launcher.Launcher;

public class Log {
	
	private static StringBuffer buffer = new StringBuffer(7000);

	public static void info(String x) {
		log("INFO ", x);
	}

	public static void warn(String x) {
		log("WARN ", x);
	}

	public static void error(String x) {
		log("ERROR", x);
	}

	public static void debug(String x) {
		if(Launcher.DEBUG) log("DEBUG", x);
	}

	public static void error(String s, Throwable e) {
		error(s + ": " + exceptionToString(e));
	}

	public static void warn(String s, Throwable e) {
		warn(s + ": " + exceptionToString(e));
	}

	static void log(String lvl, String x) {
		log(lvl, null, x);
	}

	static void log(String lvl, String sec, String x) {
		x = x.replace("\r", "");
		String[] arr = x.split("\n");
		String p = "[" + date() + "] [" + lvl + "]" + (sec != null ? " [" + sec + "]" : "");
		for(int i = 0; i < arr.length; i++) {
			String s = p + " " + arr[i];
			println(s);
		}
	}

	private static void println(String s) {
		System.out.println(s);
		buffer.append(s).append("\n");
		if(Launcher.inst != null && Launcher.inst.loggerFrame() != null)
			Launcher.inst.loggerFrame().append(s + "\n");
	}

	private static String date() {
		Calendar c = Calendar.getInstance();
		return i(c.get(Calendar.DAY_OF_MONTH)) + "." + i(c.get(Calendar.MONTH)+1) + "." + c.get(Calendar.YEAR) + " " + i(c.get(Calendar.HOUR_OF_DAY)) + ":" + i(c.get(Calendar.MINUTE)) + ":" + i(c.get(Calendar.SECOND));
	}
	
	private static String i(int i) {
		if(i < 10) return "0" + i;
		return "" + i;
	}
	
	public static String exceptionToString(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	public static void clearBuffer() {
		buffer.delete(0, buffer.length());
	}
	
	public static String getLog() {
		return buffer.toString();
	}
}

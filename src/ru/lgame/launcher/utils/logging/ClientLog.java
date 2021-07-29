package ru.lgame.launcher.utils.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import ru.lgame.launcher.Launcher;

public class ClientLog extends PrintStream {
	
	private static ClientLog instance;
	private StringWriter sw;
	private PrintWriter pw;
	
	public static ClientLog getInstance() {
		if(instance == null) instance = new ClientLog();
		return instance;
	}

	ClientLog() {
		super(System.out, true);
		sw = new StringWriter(5000);
		pw = new PrintWriter(sw);
	}

	public void write(byte[] buf, int off, int len) {
		super.write(buf, off, len);
		try {
			String s = new String(buf, off, len, "UTF-8");
			if(pw != null) pw.write(s);
			if(Launcher.inst != null && Launcher.inst.loggerFrame() != null)
				Launcher.inst.loggerFrame().append(s);
		} catch (Exception e) {
		}
	}
	
	public void reset() {
		try {
			pw.close();
			sw.close();
		} catch (IOException e) {
		}
		sw = new StringWriter(5000);
		pw = new PrintWriter(sw);
	}
	
	public String getLog() {
		return sw.toString();
	}
	
	public String getLastException() {
		int en = getLog().lastIndexOf("\nLOG END");
		if(en == -1) {
			en = getLog().length();
		}
		try {
			String x = "---- Minecraft Crash Report ----";
			int i = getLog().lastIndexOf(x);
			if(i == -1) {
				x = "Exception in thread \"main\"";
				i = getLog().lastIndexOf(x);
				if(i == -1) { 
					x = "Error: ";
					i = getLog().lastIndexOf(x);
					return getLog().substring(i, en);
				}
				return getLog().substring(i, en);
			}
			return getLog().substring(i, en);
		} catch (Exception e) { }
		return null;
	}
	
}

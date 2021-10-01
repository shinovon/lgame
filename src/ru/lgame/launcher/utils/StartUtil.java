package ru.lgame.launcher.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.lgame.launcher.Config;
import ru.lgame.launcher.utils.logging.Log;

/**
 * @author Shinovon
 */
public class StartUtil {
	public static Process startJarProcess(File dir, Set<File> classpath, String mainClass, List<String> jvmArgs, List<String> appArgs) throws IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add(getJavaExec());
		cmd.addAll(jvmArgs);
		cmd.add("-classpath");
		cmd.add(constructClassPath(classpath));
		cmd.add(mainClass);
		cmd.addAll(appArgs);
		Log.info(cmd.toString());
		return startProcess(dir, cmd);
	}

	private static Process startProcess(File dir, List<String> cmd) throws IOException {
		return new ProcessBuilder(new String[0]).command(cmd).directory(dir).start();
	}

	private static String getJavaExec() {
		String p = Config.get("javapath");
		String exec = "java.exe";
		if(p != null && p != "" && p != "  " && p != " " && p.length() > 3) {
			p = p.replace("\\", File.separator);
			p = p.replace("/", File.separator);
			if(p.endsWith("java.exe")) return p;
			if(p.endsWith("bin" + File.separator)) return p + exec;
			if(p.endsWith("bin")) return p + File.separator + exec;
			if(p.endsWith(File.separator)) return p + "bin" + File.separator + exec;
			Log.debug("1: " + p);
			return p + File.separator + "bin" + File.separator + exec;
		}
		String home = System.getProperty("java.home");
		if(home == null || home == "" || home == " " || home.length() < 2)
			throw new RuntimeException("invalid java.home value");
		Log.debug("2: " + home);
		return home + File.separator + "bin" + File.separator + exec;
	}

	public static String findJavaDir() {
		String s = System.getProperty("java.home");
		return s;
	}
	
	private static String getJavaPathDir() {
		String[] paths = System.getenv("path").split(";");
		int d = 0;
		for (int i = 0; i < paths.length; i++)
			if (paths[i].contains("java"))
				d = i;
		return paths[d];
	}

	private static String constructClassPath(Set<File> classpathList) throws IOException {
		StringBuilder classpathBuilder = new StringBuilder();
		for (File classpathEntry : classpathList) {
			if (!classpathEntry.exists()) {
				throw new FileNotFoundException("classpath not found: " + classpathEntry.getAbsolutePath());
			}
			if (classpathBuilder.length() > 0) {
				classpathBuilder.append(File.pathSeparatorChar);
			}
			classpathBuilder.append(classpathEntry.getAbsolutePath());
		}
		return classpathBuilder.toString();
	}
}

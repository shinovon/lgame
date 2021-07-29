package ru.lgame.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.lgame.launcher.utils.logging.Log;

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
		if(p != null && p != "") {
			if(p.endsWith(File.separator)) return p + "bin" + File.separator + exec;
			return p + File.separator + "bin" + File.separator + exec;
		}
		return System.getProperty("java.home") + File.separator + "bin" + File.separator + exec;
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

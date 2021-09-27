package ru.lgame.launcher.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Shinovon
 */
public class FileUtils {
	public static String getString(File file) throws IOException {
		return getString(file.getAbsolutePath());
	}
	
	public static String getString(String file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
	}

	public static void writeString(File file, String s) throws IOException {
		if(file.exists()) file.delete();
	    Path targetPath = Paths.get(file.toString());
	    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
	    Files.write(targetPath, bytes, StandardOpenOption.CREATE);
	}
}


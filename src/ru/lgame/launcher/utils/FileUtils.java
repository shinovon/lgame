package ru.lgame.launcher.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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

	public static void deleteDirectoryRecursion(Path path) throws IOException {
		if(!Files.exists(path)) return;
		deleteDirectoryContents(path);
		Files.delete(path);
	}
	
	public static void deleteDirectoryContents(Path path) throws IOException {
		if(!Files.exists(path)) return;
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursion(entry);
				}
			}
		}
	}

	public static long sizeOf(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException(file + " does not exist");
		}
		if (file.isDirectory()) {
			return sizeOfDirectory0(file);
		}
		return file.length();
	}
	
	private static long sizeOfDirectory0(File directory) {
		final File[] files = directory.listFiles();
		if (files == null) {
			return 0L;
		}
		long size = 0L;
		for (final File file : files) {
			size += sizeOf0(file);
			if (size < 0L) {
				break;
			}
		}
		return size;
	}

	private static long sizeOf0(final File file) {
		if (file.isDirectory()) {
			return sizeOfDirectory0(file);
		}
		return file.length();
	}
}


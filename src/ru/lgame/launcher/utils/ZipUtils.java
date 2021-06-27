package ru.lgame.launcher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
	public static ProgressListener listener;
	public static String currentFile;
	public static boolean done = false;
	
	public static void setListener(ProgressListener p) {
		listener = p;
	}

	public static void zip(String input, String file, String output) throws IOException {
		if(listener != null) listener.startZip(input);
		currentFile = input;
		byte[] buffer = new byte[1024];

		FileOutputStream fos = new FileOutputStream(output);
		ZipOutputStream zos = new ZipOutputStream(fos);
		ZipEntry ze = new ZipEntry(file);
		zos.putNextEntry(ze);
		FileInputStream in = new FileInputStream(input);
		int len;
		while ((len = in.read(buffer)) > 0) {
			zos.write(buffer, 0, len);
		}
		in.close();
		zos.closeEntry();
		zos.close();
		if(listener != null) listener.doneZip(input);
		currentFile = "";
	}

	public static void unzip(String zipFile, String outputFolder) throws IOException, InterruptedException {
		if(Thread.interrupted()) throw new InterruptedException("Thread.interrupted()");
		Log.info("Unzipping " + zipFile + " to " + outputFolder);
		if(listener != null) listener.startUnzip(zipFile);
		currentFile = "";
		done = false;
		byte[] buf = new byte[4096];

		File folder = new File(outputFolder);
		if(!folder.exists()) {
			folder.mkdir();
		}
		int totalEntries = 0;
		ZipFile zf = new ZipFile(zipFile);
		for(Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements(); entries.nextElement()) totalEntries++;
		zf.close();
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		int processedEntries = 0;
		ZipEntry ze = zis.getNextEntry();
		int p = 0;
		while (ze != null) {
			p = (int)(((float) processedEntries / (float)totalEntries) * 100F);
			if(Thread.interrupted()) {
				zis.closeEntry();
				zis.close();
				throw new InterruptedException("Thread.interrupted()");
			}
			String fileName = ze.getName();
			boolean isDir = ze.isDirectory();
			File newFile = new File(outputFolder + File.separator + fileName);
			new File(newFile.getParent()).mkdirs();
			if(!isDir) {
				done = false;
				FileOutputStream fos = new FileOutputStream(newFile);
				currentFile = newFile.getName();
				if(listener != null) listener.unzipProgress(currentFile, p, 0);
				int len;
				while ((len = zis.read(buf)) > 0) {
					if(Thread.interrupted()) {
						fos.close();
						zis.closeEntry();
						zis.close();
						throw new InterruptedException("Thread.interrupted()");
					}
					fos.write(buf, 0, len);
				}
				fos.close();
				done = true;
			} else {
				done = false;
				new File(outputFolder + File.separator + fileName + File.separator).mkdirs();
				currentFile = newFile.getName() + File.separator;
				done = true;
			}
			if(listener != null) listener.unzipProgress(currentFile, p, 100);
			ze = zis.getNextEntry();
			processedEntries++;
		}
		zis.closeEntry();
		zis.close();
		if(listener != null) listener.doneUnzip(zipFile);
		currentFile = "";
	}
	
	public interface ProgressListener {
		public void startZip(String zipFile);

		public void doneZip(String zipFile);
		
		public void startUnzip(String zipFile);
		
		public void unzipProgress(String currentFile, int totalPercent, int currentFilePercent);

		public void doneUnzip(String zipFile);
	}

}

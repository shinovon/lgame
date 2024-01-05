package ru.lgame.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

public class Hasher {
	static boolean client = false;
	static String path = ".\\web files\\vanillaremake\\mods";
	
	static JSONObject json;
	
	public static void main(String[] args) {
		json = new JSONObject();
		path += "\\";
		if(client) {
			path += "client.jar";
			try {
				System.out.println(sha1s(path));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			FileFilter filter = (file) -> file.isDirectory() || file.getName().toLowerCase().endsWith(".zip") || file.getName().toLowerCase().endsWith(".so") || file.getName().toLowerCase().endsWith(".dll") || file.getName().toLowerCase().endsWith(".jar");
			File[] mods = new File(path).listFiles(filter);
			for(File f: mods) {
				if(f.isDirectory()) {
					recursion(f);
				} else {
					json.put(path(f.getPath()), sha1s(f.getPath()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(json.toString(3).replace("   ", "	"));
	}
	
	private static String path(String s) {
		return s.replace(path, "").replace("\\", "/");
	}

	private static void recursion(File f) throws Exception {
		FileFilter filter = (file) -> file.isDirectory() || file.getName().toLowerCase().endsWith(".zip") || file.getName().toLowerCase().endsWith(".so") || file.getName().toLowerCase().endsWith(".dll") || file.getName().toLowerCase().endsWith(".jar");
		File[] mods = f.listFiles(filter);
		for(File f2: mods) {
			if(f2.isDirectory()) {
				recursion(f2);
			} else {
				json.put(path(f2.getPath()), sha1s(f2.getPath()));
			}
		}
	}

	public static String sha1s(String f) throws IOException, NoSuchAlgorithmException { 
		File file = new File(f); 
		MessageDigest shaDigest = MessageDigest.getInstance("SHA-1"); 
		String shaChecksum = getFileChecksum(shaDigest, file); 
		return shaChecksum; 
	}
	
	public static String getFileChecksum(MessageDigest digest, File file) throws IOException { 
		FileInputStream fis = new FileInputStream(file); 
		byte[] byteArray = new byte[1024]; 
		int bytesCount = 0; 
	
		while ((bytesCount = fis.read(byteArray)) != -1) { 
			digest.update(byteArray, 0, bytesCount); 
		}
	
		fis.close(); 
		byte[] bytes = digest.digest(); 
		StringBuilder sb = new StringBuilder(); 
		
		for(int i=0; i< bytes.length ;i++) { 
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1)); 
		} 
		
		return sb.toString(); 
	}
}

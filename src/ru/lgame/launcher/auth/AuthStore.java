package ru.lgame.launcher.auth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import com.mojang.authlib.exceptions.InvalidCredentialsException;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.utils.FileUtils;
import ru.lgame.launcher.utils.logging.Log;

public final class AuthStore {
	
	private static final String secret_key1 = "LGAMEAUTHSTORELG";
	private static final String secret_key2 = "BIGLANCHBYSHINLG";

	private static Auth selected;
	private static ArrayList<Auth> list;
	
	private AuthStore() {
	}
	
	public static void init() throws InvalidCredentialsException {
		list = new ArrayList<Auth>();
		load();
	}
	
	private static void load() throws InvalidCredentialsException {
		InvalidCredentialsException ice = null;
		if(!authStoreFile().exists()) return;
		try {
			String l = FileUtils.getString(authStoreFile());
			String[] a = l.split("\n");
			for(String s: a) loadAccount(s);
		} catch (IOException e) {
			Log.error("Failed to load accounts", e);
		} catch (InvalidCredentialsException e) {
			ice = e;
			Log.error("Mojang account token expired", e);
		}
		Log.debug("loaded accounts: " + list);
		save();
		if(ice != null) throw ice;
	}

	public static void save() {
		if(authStoreFile().exists()) authStoreFile().delete();
		StringBuilder sb = new StringBuilder();
		for(Auth a: list) try {
			sb.append(getAccountString(a)).append("\n");
		} catch (Exception e) {
			Log.error("Failed to save account: " + a, e);
			return;
		}
		try {
			FileUtils.writeString(authStoreFile(), sb.toString());
		} catch (IOException e) {
			Log.error("Failed to save accounts", e);
		}
		Log.debug("saved accounts: " + list);
	}
	
	private static void loadAccount(String x) throws InvalidCredentialsException {
		try {
			//Log.debug("reading account entry: " + x);
			if(x.length() <= 3) return;
			String c = decodeAES1(new String(Base64.getDecoder().decode(x), "UTF-8"));
			String[] sa = split(c, ":|:");
			String type = sa[0];
			//Log.debug("decoded descriptor: " + Arrays.toString(sa));
			String s = decodeAES2(sa[2]);
			//Log.debug("decrypted auth data: " + s);
			Auth a = null;
			if(type.equals("MOJANG")) {
				//a = Auth.fromMojangStorage(stringToMap(s));
				a = Auth.fromMojang(s);
			} else if(type.equals("CRACKED")) {
				a = Auth.fromUsername(s);
			} else {
				Log.debug(s);
			}
			if(a != null) {
				Log.debug("added account: " + a);
				if(!list.contains(a)) list.add(a);
				if(sa[1].equals("1")) selected = a;
			}
		} catch (InvalidCredentialsException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getAccountString(Auth a) throws Exception {
		String fs = "";
		String c = "";
		try {
			if(a.isMojang()) {
				//c = mapToString(a.mojangForStorage());
				c = a.mojangMailPasswordEncrypted();
			} else if(a.isCracked()) {
				c = a.getUsername();
			} else {
				c = "null";
			}
		} catch (Exception e) {
		}
		String sel = "0";
		if(a.equals(selected)) sel = "1";
		fs = "" + a.getType() + ":|:" + sel + ":|:" + encodeAES2(c);
		return new String(Base64.getEncoder().encode(encodeAES1(fs).getBytes("UTF-8")), "UTF-8");
	}
	
	private static String mapToString(Map<String, Object> map) {
		return new JSONObject(map).toString();
	}
	
	private static Map<String, Object> stringToMap(String str) {
		return new JSONObject(str).toMap();
	}

	private static File authStoreFile() {
		return new File(Launcher.getLauncherDir() + "accounts");
	}

	public static Auth getSelected() {
		return selected;
	}

	private static String decodeAES1(String s) throws Exception {
		return decodeAES(s, secret_key1);
	}

	private static String encodeAES1(String s) throws Exception {
		return encodeAES(s, secret_key1);
	}

	private static String decodeAES2(String s) throws Exception {
		return decodeAES(s, secret_key2);
	}

	private static String encodeAES2(String s) throws Exception {
		return encodeAES(s, secret_key2);
	}

	private static String decodeAES(String s, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec b = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, b);
		byte[] a = cipher.doFinal(Base64.getDecoder().decode(s.replace("\r", "").replace("\n", "").getBytes("UTF-8")));
		return new String(a);
	}

	private static String encodeAES(String s, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec b = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, b);
		byte[] a = s.getBytes("UTF-8");
		byte[] c = cipher.doFinal(a);
		return new String(Base64.getEncoder().encode(c), "UTF-8");
	}
	
	public static void add(Auth a) {
		if(!list.contains(a)) list.add(a);
		save();
	}

	public static Auth[] list() {
		return list.toArray(new Auth[0]).clone();
	}

	public static void remove(Auth a) {
		if(list.contains(a)) list.remove(a);
		if(selected == a) selected = null;
		save();
	}

	public static void setSelected(Auth a) {
		if(list.contains(a)) selected = a;
		save();
	}

	private static String[] split(String str, String d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		ArrayList<String> v = new ArrayList<String>();
		v.add(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + d.length());
			if((i = str.indexOf(d)) != -1)
				v.add(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.add(str);
		return v.toArray(new String[0]);
	}
}

package ru.lgame.launcher;

/**
 * @author Shinovon
 */
public class Errors {

	public static final int UPDATER_RUN_GETUPDATEJSON_IOEXCEPTION = 1001;
	public static final int UPDATER_RUN_GETUPDATEJSON_JSONEXCEPTION = 1002;
	public static final int UPDATER_RUN_CHECKMODPACK_EXCEPTION = 1003;
	public static final int UPDATER_RUN_CHECKCLIENT_EXCEPTION = 1004;
	public static final int UPDATER_GETMODPACKSTATE_ILLEGAL_VALUE = 1005;
	public static final int UPDATER_SCRIPTEDDOWNLOAD_CHECK = 1006;
	public static final int UPDATER_SCRIPTEDDOWNLOAD_DOWNLOAD = 1007;
	public static final int UPDATER_SCRIPTEDUNZIP_UNZIP = 1008;
	public static final int UPDATER_RUN_GETCLIENTSTARTJSON_IOEXCEPTION = 1009;
	
	public static String toHexString(int i) {
		String s = Integer.toHexString(i);
		while(s.length() < 8) {
			s = "0" + s;
		}
		return "0x" + s;
	}

}

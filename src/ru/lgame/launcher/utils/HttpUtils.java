package ru.lgame.launcher.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.utils.logging.Log;

/**
 * @author Shinovon
 */
public class HttpUtils {
	
	private final Runnable progressUpdRun = new Runnable() {
		public void run() {
			if(file == null) return;
			if(listener == null) return;
			int percent;
			if(downloaded <= 0) percent = 0;
			else percent = (int) ((double) ((double) downloaded / (double) need) * 100D);
			listener.downloadProgress(file, speed, percent, need - downloaded);
		}
	};

	private ProgressListener listener;
	
	public int downloaded;
	public int need;
	private String file;
	private static String useragent = "Mozilla/5.0";

	public static double speed;
	
	public void setListener(ProgressListener p) {
		listener = p;
	}
	
	private static double round(double d) {
		double pow = 100;
		return (double) ((int) (d * pow)) / pow;
	}

	public final void download(String uri, String path) throws IOException, InterruptedException {
		File f = new File(path);
		this.file = f.getName();
		File d = f.getParentFile();
		if(!d.exists()) d.mkdirs();
		if(Thread.interrupted()) throw new InterruptedException("download");
		Thread st = null;
		if(listener != null) {
			downloaded = 0;
			need = 0;
			speed = 0;
			st = new Thread("DST "+file) {
				public void run() {
					int ld = downloaded;
					try {
						while(true) {
							Thread.sleep(1000);
							speed = round((downloaded - ld) / 1024D / 1024D);
							ld = downloaded;
						}
					} catch (Exception e) {
					}
				}
			};
		}
		try {
			HttpURLConnection con = getHttpConnection(uri);
			//con.setRequestProperty("Accept-Encoding", "gzip");
			con.setRequestMethod("GET");
			con.setConnectTimeout(10000);
			con.setReadTimeout(30000);
			con.setDoInput(true);
			con.connect();
			int res = con.getResponseCode();
			if(res == 404 || res == 401 || res == 403 || res == 500 || res == 501 || res == 503) throw new IOException("HTTP " + res);
			FileOutputStream fout = new FileOutputStream(path);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = getHttpInputStream(con, false);
			byte buffer[] = new byte[512 * 1024];
			int read;
			need = con.getContentLength();
			Log.info("Downloading: \"" + uri + "\" to \"" + path + "\", size: " + (need / 1024) + "k");
			if(need == -1) Log.warn("Content-size unknown");
			int i = 0;
			if(st != null) st.start();
			if(listener != null) listener.startDownload(f.getName());
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				downloaded += read;
				if(i++ >= 32) {
					i = 0;
					if(listener != null) {
						Launcher.inst.queue(progressUpdRun);
					}

					if(Thread.interrupted()) {
						Log.warn("Download interrupt!");
						out.close();
						fout.close();
						in.close();
						((HttpURLConnection) con).disconnect();
						Launcher.inst.queue(new Runnable() {
							public void run() {
								try {
									new File(path).delete();
								} catch (Exception e) {
								}
							}
						});
						throw new InterruptedException("Thread.interrupted()");
					}
				}
			}
			if(listener != null) listener.downloadProgress(file, speed, 100, 0);
			Log.debug("Size of " + file + " " + downloaded);
			if(need != -1 && downloaded != need) Log.warn("Content-Size and actual file size does not match!! " + downloaded + " vs " + need + " needed");
			if(st != null) st.interrupt();
			in.close();
			con.disconnect();
			byte[] bytes = out.toByteArray();
			out.close();
			fout.write(bytes);
			fout.close();
			if(listener != null) listener.doneDownload(f.getName());
		} catch (IOException e) {
			Launcher.inst.queue(new Runnable() {
				public void run() {
					try {
						new File(path).delete();
					} catch (Exception e) {
					}
				}
			});
			throw new IOException(uri, e);
		}
		file = null;
		downloaded = 0;
		need = 0;
	}

	public static int getHttpContentLength(String url) throws IOException {
		HttpURLConnection con = getHttpConnection(url);
		con.setRequestMethod("HEAD");
		con.connect();
		int i = con.getContentLength();
		con.disconnect();
		return i;
	}

	public static String getHttpContentType(String url) throws IOException {
		HttpURLConnection con = getHttpConnection(url);
		con.setRequestMethod("HEAD");
		con.connect();
		String s = con.getContentType();
		con.disconnect();
		return s;
	}

	private static HttpURLConnection getHttpConnection(String url) throws IOException {
		return getHttpConnection(new URL(url));
	}

	private static HttpURLConnection getHttpConnection(URL url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty("User-Agent", useragent);
		con.setRequestProperty("User-UID", getHWID());
		return con;
	}
	
	public static String getHWID() {
        try {
            String s = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            StringBuffer sb = new StringBuffer();
            byte b[] = md.digest();
            for (byte aByteData : b) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
        	return "null";
        }
    }

	private static InputStream getHttpInputStream(HttpURLConnection con, boolean err) throws IOException {
		if(con.getContentEncoding() != null && con.getContentEncoding().equalsIgnoreCase("gzip"))
			return new GZIPInputStream(con.getInputStream());
		try {
			return con.getInputStream();
		} catch (IOException e) {
			if(!err) throw e;
			return con.getErrorStream();
		}
	}

	private static InputStream getHttpInputStream(HttpURLConnection con) throws IOException {
		return getHttpInputStream(con, true);
	}

	public final static String get(final String url) throws IOException {
		return new String(getBytes(url), "UTF-8");
	}
	
	public interface ProgressListener {
		public void startDownload(String filename);
		
		public void downloadProgress(String name, double speed, int percent, int bytesLeft);

		public void doneDownload(String filename);
	}

	public static byte[] getBytes(String url) throws IOException {
		Log.debug("GET " + url);
		InputStream is = null;
		try {
			HttpURLConnection con = getHttpConnection(url);
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept-Encoding", "gzip");
			con.connect();
			//Log.debug("Connected, getting text");
			int r = con.getResponseCode();
			if(r == 404) {
				con.disconnect();
				throw new FileNotFoundException(url);
			}
			if(r == 522) {
				con.disconnect();
				throw new IOException("Cloudflare error 522");
			}
			//Log.debug("response code: " + con.getResponseCode());
			is = getHttpInputStream(con);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = con.getContentLength();
			if(i <= 0) i = 128;
			final byte[] buf = new byte[i];
			int read;
			while ((read = is.read(buf)) != -1) {
				baos.write(buf, 0, read);
			}
			is.close();
			con.disconnect();
			byte[] b = baos.toByteArray();
			baos.close();
			return b;
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw new IOException(url, e);
		} finally {
			if(is != null) is.close();
		}
	}

	public static void postReq(String url, String data) throws IOException {
		Log.debug("POST " + url);
		Log.debug("POST DATA: " + data);
		try {
			HttpURLConnection con = getHttpConnection(url);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.connect();
			OutputStream os = con.getOutputStream();
			os.write(data.getBytes("UTF-8"));
			if(con.getResponseCode() == 404) {
				con.disconnect();
				throw new FileNotFoundException(url);
			}
			con.disconnect();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw new IOException(url, e);
		}
	}
}

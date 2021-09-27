package ru.lgame.launcher.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import ru.lgame.launcher.Launcher;
import ru.lgame.launcher.utils.logging.Log;

/**
 * @author Shinovon
 */
public class WebUtils {
	private static ProgressListener listener;
	
	public static int downloaded;
	public static int need;
	private static String useragent = "Mozilla/5.0";

	public static double speed;
	
	public static void setListener(ProgressListener p) {
		listener = p;
	}

	public static void downloadExperimental(final String uri, final String fileName) throws IOException {
		downloaded = 0;
		need = 0;

		final URL url = new URL(uri);
		ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());

		FileOutputStream fileOutputStream = new FileOutputStream(fileName);
		FileChannel fileChannel = fileOutputStream.getChannel();
		fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		fileChannel.close();
		fileOutputStream.close();
	}

	public final static void download(String a, String b) throws IOException, InterruptedException {
		downloadNormal(a, b);
	}
	
	private static double round(double d) {
		int pow = 100;
		double tmp = d * pow;
		return (double) (int) ((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp) / pow;
	}

	public final static void downloadNormal(String uri, String path)
			throws IOException, InterruptedException {
		File f = new File(path);
		File d = f.getParentFile();
		if(!d.exists()) d.mkdirs();
		if(Thread.interrupted()) throw new InterruptedException("Thread.interrupted()");
		if(listener != null)
		downloaded = 0;
		need = 0;
		speed = 0;
		Thread st = new Thread() {
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
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", useragent);
			//con.setRequestProperty("Accept-Encoding", "gzip");
			con.setRequestMethod("GET");
			FileOutputStream fout = new FileOutputStream(path);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			con.setConnectTimeout(10000);
			con.setReadTimeout(20000);
			con.setDoInput(true);
			con.connect();
			InputStream in = getHTTPInputStream(con);
			byte buffer[] = new byte[16 * 1024];
			int read;
			need = con.getContentLength();
			Log.info("Downloading: " + uri + " to " + path + " size: " + (need / 1024) + "k");
			int i = 0;
			st.start();
			if(listener != null) listener.startDownload(f.getName());
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				downloaded += read;
				if(i++ >= 32) {
					i = 0;
					if(listener != null) {
						int percent;
						if(downloaded <= 0) percent = 0;
						else percent = (int) ((double) ((double) downloaded / (double) need) * 100D);
						listener.downloadProgress(f.getName(), speed, percent, need - downloaded);
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
			if(listener != null) listener.downloadProgress(f.getName(), speed, 100, 0);
			Log.debug("Size of " + path + " " + downloaded);
			if(downloaded != need) Log.warn("Content-Size and actual file size does not match!!");
			st.interrupt();
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
		downloaded = 0;
		need = 0;
	}

	private static InputStream getHTTPInputStream(HttpURLConnection con) throws IOException {
		if(con.getContentEncoding() != null && con.getContentEncoding().equalsIgnoreCase("gzip"))
			return new GZIPInputStream(con.getInputStream());
		try {
			return con.getInputStream();
		} catch (IOException e) {
			return con.getErrorStream();
		}
	}

	public final static String get(final String url) throws IOException {
		return new String(getBytes(url), "UTF-8");
	}

	public static String post(final String url, final Map<String, String> requestMap, final String body)
			throws IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", useragent);
		if(requestMap != null) requestMap.forEach(con::setRequestProperty);
		con.setDoOutput(true);
		con.setDoInput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(body);
		wr.flush();
		wr.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		StringBuilder response = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine).append("\n");
		}
		in.close();
		con.disconnect();
		return response.toString();
	}

	public static String getс(String url) throws IOException {
		return get(url).replace(" ", "").replace("\r", "").replace("\n", "");
	}
	
	public interface ProgressListener {
		public void startDownload(String filename);
		
		public void downloadProgress(String name, double speed, int percent, int bytesLeft);

		public void doneDownload(String filename);
	}

	public static byte[] getBytes(String url) throws IOException {
		Log.info("GET " + url);
		InputStream is = null;
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", useragent);
			con.setRequestProperty("Accept-Encoding", "gzip");
			con.connect();
			//Log.debug("Connected, getting text");
			if(con.getResponseCode() == 404) {
				con.disconnect();
				throw new FileNotFoundException();
			}
			//Log.debug("response code: " + con.getResponseCode());
			is = getHTTPInputStream(con);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int read;
			while ((read = is.read(buf)) != -1) {
				baos.write(buf, 0, read);
			}
			is.close();
			con.disconnect();
			byte[] b = baos.toByteArray();
			baos.close();
			return b;
		} catch (IOException e) {
			throw new IOException(url, e);
		} finally {
			if(is != null) is.close();
		}
		
	}
}

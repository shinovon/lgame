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
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import ru.lgame.launcher.Launcher;

public class WebUtils {
	private static ProgressListener listener;
	
	public static int downloaded;
	public static int need;
	public static double percent;
	private static String useragent = "Mozilla/5.0";
	
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

	public final static void downloadNormal(String uri, String path)
			throws IOException, InterruptedException {
		if(Thread.interrupted()) throw new InterruptedException("Thread.interrupted()");
		if(listener != null)
		downloaded = 0;
		need = 0;
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("User-Agent", useragent);
			con.setRequestMethod("GET");
			FileOutputStream out = new FileOutputStream(path);
			InputStream in = ((URLConnection) con).getInputStream();
			byte buffer[] = new byte[4096];
			int read;
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			need = con.getContentLength();
			Log.info("Downloading: " + uri + " to " + path + " size: " + need + "k");
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				downloaded += read;
				percent = (double) ((double) ((double) downloaded / (double) need) * 100f);
				if(Thread.interrupted()) {
					Log.warn("Download interrupt!");
					out.close();
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
			Log.debug("Size of " + path + " " + downloaded);
			if(downloaded != need) Log.warn("Content-Size and actual file size does not match!!");
			in.close();
			out.close();
			((HttpURLConnection) con).disconnect();
		} catch (IOException e) {
			throw new IOException(uri, e);
		} finally {
			Launcher.inst.queue(new Runnable() {
				public void run() {
					try {
						new File(path).delete();
					} catch (Exception e) {
					}
				}
			});
		}
		downloaded = 0;
		need = 0;
	}

	public final static String get(final String url) throws IOException {
		Log.debug("GET " + url);
		InputStream is = null;
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", useragent);
			con.connect();
			Log.debug("Connected, getting text");
			if(con.getResponseCode() == 404) {
				con.disconnect();
				throw new FileNotFoundException();
			}
			//Log.debug("response code: " + con.getResponseCode());
			is = con.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int read;
			while ((read = is.read(buf)) != -1) {
				baos.write(buf, 0, read);
			}
			is.close();
			con.disconnect();
			String x = new String(baos.toByteArray(), "UTF-8");
			baos.close();
			return x;
		} catch (IOException e) {
			throw new IOException(url, e);
		} finally {
			if(is != null) is.close();
		}
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
		
		public void downloadProgress(String filename, int percent);

		public void doneDownload(String zipFile);
	}
}

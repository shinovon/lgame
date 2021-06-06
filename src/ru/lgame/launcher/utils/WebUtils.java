package ru.lgame.launcher.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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

import javax.net.ssl.HttpsURLConnection;

public class WebUtils {
	
	public static int downloaded;
	public static int downloadedk;
	public static int need;
	public static int needk;
	public static int needm;
	public static double downloadedm;
	public static double percent;
	private static String useragent = "Mozilla/5.0";

	public final static void downloadExperimental(final String uri, final String fileName) throws IOException {
		downloaded = 0;
		need = 0;
		downloadedk = 0;
		downloadedm = 0;
		needk = 0;
		needm = 0;

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

	public final static void downloadNormal(final String uri, final String fileName)
			throws IOException, InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		downloaded = 0;
		need = 0;
		downloadedk = 0;
		downloadedm = 0;
		needk = 0;
		needm = 0;
		final URL url = new URL(uri);
		final Object connection = uri.startsWith("https://") ? ((HttpsURLConnection) url.openConnection())
				: ((HttpURLConnection) url.openConnection());
		((URLConnection) connection).setRequestProperty("User-Agent", useragent);
		((HttpURLConnection) connection).setRequestMethod("GET");
		final FileOutputStream out = new FileOutputStream(fileName);
		final InputStream in = ((URLConnection) connection).getInputStream();
		byte buffer[] = new byte[4096];
		int read;
		((HttpURLConnection) connection).setConnectTimeout(10000);
		((HttpURLConnection) connection).setReadTimeout(10000);
		need = ((HttpURLConnection) connection).getContentLength();
		needk = need / 1024;
		needm = needk / 1024;
		Log.info("Downloading: " + uri + " to " + fileName + " size: " + need + "k");
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
			downloaded += read;
			downloadedk = downloaded / 1024;
			downloadedm = (double) downloadedk / (double) 1024;
			percent = (double) ((double) ((double) downloaded / (double) need) * 100f);
			if (Thread.interrupted()) {
				out.close();
				in.close();
				((HttpURLConnection) connection).disconnect();
				Log.warn("Download interrupt!");
				throw new InterruptedException();
			}
		}
		Log.debug("Size of " + fileName + " " + downloaded);
		if(downloaded != need) Log.warn("Content-Size and actual file size does not match!!");
		in.close();
		out.close();
		((HttpURLConnection) connection).disconnect();
		downloaded = 0;
		need = 0;
		downloadedk = 0;
		downloadedm = 0;
		needk = 0;
		needm = 0;
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
			//Log.debug("response code: " + con.getResponseCode());
			is = con.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final byte[] buf = new byte[8192];
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
			if (is != null) is.close();
		}
	}

	public final static String post(final String url, final Map<String, String> requestMap, final String body)
			throws IOException {
		final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", useragent);
		if (requestMap != null) requestMap.forEach(con::setRequestProperty);
		con.setDoOutput(true);
		con.setDoInput(true);
		final DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(body);
		wr.flush();
		wr.close();
		final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		final StringBuilder response = new StringBuilder();
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
}

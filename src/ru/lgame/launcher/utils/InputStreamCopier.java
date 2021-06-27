package ru.lgame.launcher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamCopier extends Thread {
	private final InputStream input;
	private final OutputStream output;

	public InputStreamCopier(final InputStream inputStream, final OutputStream outputStream) {
		this.input = inputStream;
		this.output = outputStream;
	}

	@Override
	public final void run() {
		try {
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = this.input.read(buffer)) != -1) {
				this.output.write(buffer, 0, read);
				if(Thread.interrupted()) return;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
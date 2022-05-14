package net.mantagames.jgm.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EmptySocket extends Socket {
	private static final byte[] EMPTY = new byte[0];
	private InputStream ins = new ByteArrayInputStream(new byte[255]);

	public InputStream getInputStream() {
		return ins;//new ByteArrayInputStream(EMPTY);
	}

	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream(1);
	}
	
	public void fakeReceive(byte[] b) {
		ins = new ByteArrayInputStream(b);
	}
}
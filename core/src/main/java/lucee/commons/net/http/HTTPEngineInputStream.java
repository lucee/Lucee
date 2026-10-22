package lucee.commons.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lucee.commons.net.http.HTTPEngineBasic.HTTPDownloaderHeadResponse;

/**
 * the content stream of a response, closing it releases the connection of the response
 */
public class HTTPEngineInputStream extends InputStream {

	private HTTPResponse rsp;
	private InputStream is;
	private HTTPDownloaderHeadResponse meta;

	public HTTPEngineInputStream(HTTPResponse rsp, InputStream is) {
		this.rsp = rsp;
		this.is = is;
	}

	public HTTPDownloaderHeadResponse getHTTPDownloaderHeadResponse() {
		if (meta == null) meta = new HTTPDownloaderHeadResponse(rsp);
		return meta;
	}

	@Override
	public void close() throws IOException {
		try {
			is.close();
		}
		finally {
			rsp.close();
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		return is.readAllBytes();
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		return is.readNBytes(len);
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		return is.readNBytes(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return is.skip(n);
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		is.reset();
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return is.transferTo(out);
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}
}

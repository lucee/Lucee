package lucee.runtime.net.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Reads the parts of a multipart stream (RFC 2046, RFC 7578) one after the other. The body of a
 * part is streamed, so nothing is held in memory except a small buffer.
 *
 * <pre>
 * MultipartReader r = new MultipartReader(in, boundary);
 * while (r.nextPart()) {
 * 	String headers = r.readHeaders(charset);
 * 	InputStream body = r.getBody();
 * }
 * </pre>
 */
public final class MultipartReader {

	public static final int DEFAULT_HEADER_SIZE_MAX = 10240;

	private static final int BUFFER_SIZE = 16 * 1024;
	private static final byte CR = 0x0D;
	private static final byte LF = 0x0A;
	private static final byte DASH = '-';

	private final InputStream in;
	private final int headerSizeMax;
	private final byte[] buf;
	private int pos;
	private int limit;
	private boolean eof;

	private byte[] delimiter; // CRLF -- boundary
	private int[] shift;

	private boolean closed;
	private boolean bodyDone = true;
	private boolean headersRead;
	private boolean started;

	private final InputStream body = new Body();

	public MultipartReader(InputStream in, byte[] boundary) {
		this(in, boundary, DEFAULT_HEADER_SIZE_MAX);
	}

	public MultipartReader(InputStream in, byte[] boundary, int headerSizeMax) {
		this.in = in;
		this.headerSizeMax = headerSizeMax;
		this.buf = new byte[BUFFER_SIZE + boundary.length + 16];
		setBoundary(boundary);
		// the stream is read as if a line break stands in front of it, so the first delimiter
		// is found like all the others
		buf[0] = CR;
		buf[1] = LF;
		limit = 2;
		started = true;
	}

	/**
	 * Replaces the boundary, for a nested multipart/mixed section. Call nextPart afterwards, the
	 * content in front of the first boundary of the section is skipped.
	 */
	public void setBoundary(byte[] boundary) {
		setBoundary(boundary, true);
	}

	/**
	 * Goes back to the boundary of the enclosing section, the line break in front of its next
	 * boundary is part of the stream already.
	 */
	public void restoreBoundary(byte[] boundary) {
		setBoundary(boundary, false);
	}

	private void setBoundary(byte[] boundary, boolean insertLineBreak) {
		if (boundary == null || boundary.length == 0) throw new IllegalArgumentException("the boundary is empty");
		if (boundary.length > 200) throw new IllegalArgumentException("the boundary is too long");
		if (boundary.length + 4 + 2 > buf.length - 2) {
			throw new IllegalArgumentException("the boundary is too long");
		}
		delimiter = new byte[boundary.length + 4];
		delimiter[0] = CR;
		delimiter[1] = LF;
		delimiter[2] = DASH;
		delimiter[3] = DASH;
		System.arraycopy(boundary, 0, delimiter, 4, boundary.length);

		// Horspool shift table
		shift = new int[256];
		int d = delimiter.length;
		for (int i = 0; i < 256; i++)
			shift[i] = d;
		for (int i = 0; i < d - 1; i++)
			shift[delimiter[i] & 0xFF] = d - 1 - i;

		closed = false;
		bodyDone = false;
		headersRead = true;
		if (started && insertLineBreak) {
			// the first boundary of a nested section follows the headers directly, without a line break
			System.arraycopy(buf, pos, buf, pos + 2, limit - pos);
			buf[pos] = CR;
			buf[pos + 1] = LF;
			limit += 2;
		}
	}

	/**
	 * moves to the start of the next part.
	 *
	 * @return false if there is no further part
	 */
	public boolean nextPart() throws IOException {
		if (closed) return false;
		// skip what is left of the current part (or the preamble)
		if (!bodyDone) {
			byte[] skip = new byte[2048];
			while (readBody(skip, 0, skip.length) != -1) {
			}
		}
		// the delimiter
		ensure(delimiter.length + 2);
		if (limit - pos < delimiter.length) throw new MalformedMultipartException("the stream ended unexpectedly");
		if (limit - pos < delimiter.length + 2 && eof) {
			// the stream ends right after the last delimiter, without the closing dashes
			byte[] rest = new byte[limit - pos - delimiter.length];
			System.arraycopy(buf, pos + delimiter.length, rest, 0, rest.length);
			if (rest.length == 0 || (rest.length == 1 && rest[0] == DASH)) {
				pos = limit;
				closed = true;
				return false;
			}
		}
		if (limit - pos < delimiter.length + 2) throw new MalformedMultipartException("the stream ended unexpectedly");
		pos += delimiter.length;
		byte b0 = buf[pos++];
		byte b1 = buf[pos++];
		if (b0 == DASH && b1 == DASH) {
			closed = true;
			return false;
		}
		if (b0 != CR || b1 != LF) throw new MalformedMultipartException("unexpected characters follow a boundary");
		bodyDone = false;
		headersRead = false;
		return true;
	}

	/**
	 * reads the header block of the current part, the lines are separated by a line break and a
	 * folded line is not unfolded.
	 */
	public String readHeaders(Charset charset) throws IOException {
		if (headersRead) throw new IllegalStateException("the headers are already read");
		headersRead = true;
		// no headers at all
		ensure(2);
		if (limit - pos >= 2 && buf[pos] == CR && buf[pos + 1] == LF) {
			pos += 2;
			return "";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		int state = 0; // number of bytes of CRLFCRLF matched
		int size = 0;
		while (true) {
			if (pos == limit) {
				fill();
				if (pos == limit) throw new MalformedMultipartException("the stream ended unexpectedly");
			}
			byte b = buf[pos++];
			if (++size > headerSizeMax) throw new MalformedMultipartException("the header section is bigger than " + headerSizeMax + " bytes");
			baos.write(b);
			if (b == ((state & 1) == 0 ? CR : LF)) {
				if (++state == 4) break;
			}
			else state = b == CR ? 1 : 0;
		}
		byte[] bytes = baos.toByteArray();
		return new String(bytes, 0, bytes.length - 4, charset != null ? charset : StandardCharsets.UTF_8);
	}

	/**
	 * the body of the current part, it ends at the next delimiter
	 */
	public InputStream getBody() {
		return body;
	}

	private int readBody(byte[] b, int off, int len) throws IOException {
		if (bodyDone) return -1;
		if (len == 0) return 0;
		int d = delimiter.length;
		while (true) {
			int available = limit - pos;
			if (available >= d) {
				int index = indexOfDelimiter();
				if (index != -1) {
					int n = Math.min(index - pos, len);
					if (n == 0) {
						if (index == pos) {
							bodyDone = true;
							return -1;
						}
					}
					System.arraycopy(buf, pos, b, off, n);
					pos += n;
					return n;
				}
				// the delimiter can start in the last bytes
				int n = Math.min(available - (d - 1), len);
				if (n > 0) {
					System.arraycopy(buf, pos, b, off, n);
					pos += n;
					return n;
				}
			}
			if (eof) throw new MalformedMultipartException("the stream ended unexpectedly");
			fill();
		}
	}

	private int indexOfDelimiter() {
		int d = delimiter.length;
		int i = pos;
		int last = limit - d;
		while (i <= last) {
			int j = d - 1;
			while (j >= 0 && buf[i + j] == delimiter[j])
				j--;
			if (j < 0) return i;
			i += shift[buf[i + d - 1] & 0xFF];
		}
		return -1;
	}

	private void ensure(int count) throws IOException {
		while (limit - pos < count && !eof)
			fill();
	}

	private void fill() throws IOException {
		if (eof) return;
		if (pos > 0) {
			System.arraycopy(buf, pos, buf, 0, limit - pos);
			limit -= pos;
			pos = 0;
		}
		// the last 2 bytes are kept free for the line break in front of a nested section
		int r = in.read(buf, limit, buf.length - 2 - limit);
		if (r == -1) eof = true;
		else limit += r;
	}

	public static class MalformedMultipartException extends IOException {

		private static final long serialVersionUID = 6131245284119545431L;

		public MalformedMultipartException(String msg) {
			super(msg);
		}
	}

	private final class Body extends InputStream {

		@Override
		public int read() throws IOException {
			byte[] one = new byte[1];
			int r;
			while ((r = readBody(one, 0, 1)) == 0) {
			}
			return r == -1 ? -1 : one[0] & 0xFF;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return readBody(b, off, len);
		}

		@Override
		public void close() {
		}
	}
}

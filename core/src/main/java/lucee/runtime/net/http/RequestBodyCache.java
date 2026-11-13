package lucee.runtime.net.http;

import java.lang.ref.SoftReference;
import java.nio.charset.Charset;

/**
 * Holds the request body of a single request, so it is read from the request only once and then
 * shared by every call that needs it (for example every getHTTPRequestData() call). The raw bytes
 * are held through a soft reference, so the memory can still be reclaimed under memory pressure, in
 * that case the body is simply read again.
 */
final class RequestBodyCache {

	private static final class Entry {
		private final byte[] data;
		private Charset charset;
		private String string;

		private Entry(byte[] data) {
			this.data = data;
		}
	}

	private SoftReference<Entry> ref;

	synchronized byte[] getBytes() {
		Entry e = ref == null ? null : ref.get();
		return e == null ? null : e.data;
	}

	synchronized void setBytes(byte[] data) {
		ref = new SoftReference<Entry>(new Entry(data));
	}

	/**
	 * returns the cached body as a trimmed String for the given charset, the String is created only
	 * once per charset
	 *
	 * @param data the bytes as returned by getBytes, if they are not the cached bytes anymore, the String
	 *            is created without caching
	 */
	synchronized String getString(byte[] data, Charset cs) {
		Entry e = ref == null ? null : ref.get();
		if (e == null || e.data != data) return toString(data, cs);
		if (e.string == null || e.charset != cs) {
			e.string = toString(data, cs);
			e.charset = cs;
		}
		return e.string;
	}

	private static String toString(byte[] data, Charset cs) {
		if (cs != null) return new String(data, cs).trim();
		return new String(data).trim();
	}
}

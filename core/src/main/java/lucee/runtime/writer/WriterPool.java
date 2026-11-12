package lucee.runtime.writer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Per-PageContext bundle of writer-pipeline pool state. One instance lives on each PageContextImpl
 * (eagerly created, final reference) and survives PC borrow/release cycles. Inner fields are
 * mutable and lazily populated by CFMLWriterImpl as they're needed.
 *
 * All access is from the single thread that owns the owning PageContextImpl; no synchronisation.
 */
public final class WriterPool {

	// buffers whose backing array exceeds this are discarded rather than pooled after a request
	static final int MAX_REUSABLE_BUFFER_SIZE = 131072;

	/**
	 * ByteArrayOutputStream that exposes the backing array length so WriterPool.release() can decide
	 * whether the buffer has grown beyond MAX_REUSABLE_BUFFER_SIZE and should be discarded.
	 */
	static final class PooledByteArrayOutputStream extends ByteArrayOutputStream {

		PooledByteArrayOutputStream(int size) {
			super(size);
		}

		int capacity() {
			return buf.length;
		}
	}

	public ByteArrayOutputStream baos;
	public StringBuilder responseBuffer;
	public char[] encodeBuffer;
	public CharBuffer encodeCharBuffer;
	public ByteBuffer encodeByteBuffer;
	public CharsetEncoder encoder;
	public Charset encoderCharset;

	/**
	 * Called once per request, from PageContextImpl.release(), after the response has been sent.
	 * Resets small buffers for reuse; discards oversized ones so a single large response does not
	 * permanently bloat every idle PageContext in the pool.
	 */
	public void release() {
		if (responseBuffer != null) {
			if (responseBuffer.capacity() > MAX_REUSABLE_BUFFER_SIZE)
				responseBuffer = null;
			else
				responseBuffer.setLength(0);
		}
		if (baos instanceof PooledByteArrayOutputStream) {
			if (((PooledByteArrayOutputStream) baos).capacity() > MAX_REUSABLE_BUFFER_SIZE)
				baos = null;
			else
				baos.reset();
		}
		else if (baos != null) {
			baos = null; // unknown type or oversized plain BAOS — don't pool it
		}
	}
}

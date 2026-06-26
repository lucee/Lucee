package lucee.runtime.writer;

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

	public StringBuilder responseBuffer;
	public char[]         encodeBuffer;
	public CharBuffer     encodeCharBuffer;
	public ByteBuffer     encodeByteBuffer;
	public CharsetEncoder encoder;
	public Charset        encoderCharset;
}

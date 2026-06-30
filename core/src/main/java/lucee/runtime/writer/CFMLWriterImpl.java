/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.legacy.CacheItem;
import lucee.runtime.net.http.HttpServletResponseWrap;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;

/**
 * Implementation of a JSpWriter
 */
public class CFMLWriterImpl extends CFMLWriter {

	private static final int INITIAL_BUFFER_SIZE = 32768;
	private static final int CHILD_INITIAL_BUFFER_SIZE = 10000;
	private static final int MAX_REUSABLE_BUFFER_SIZE = 131072;
	private static final int OUTPUT_ENCODE_BUFFER_SIZE = 8192;
	private static final int MIN_COMPRESS_BYTES = 2048; // matches Tomcat's default compressionMinSize
	private OutputStream out;
	private HttpServletResponse response;
	private boolean flushed;
	private StringBuilder htmlHead;
	private StringBuilder htmlBody;
	private StringBuilder buffer;
	private boolean closed = false;
	private boolean closeConn;
	private boolean contentLength;
	private CacheItem cacheItem;
	private HttpServletRequest request;
	private Boolean _allowCompression;
	private PageContext pc;

	/**
	 * constructor of the class
	 * 
	 * @param response Response Object
	 * @param bufferSize buffer Size
	 * @param autoFlush do auto flush Content
	 */
	public CFMLWriterImpl(PageContext pc, HttpServletRequest request, HttpServletResponse response, int bufferSize, boolean autoFlush, boolean closeConn, boolean showVersion,
			boolean contentLength) {
		super(bufferSize, autoFlush);
		this.pc = pc;
		this.request = request;
		this.response = response;
		this.autoFlush = autoFlush;
		this.bufferSize = bufferSize;
		this.closeConn = closeConn;
		this.contentLength = contentLength;
	}

	private void _check() throws IOException {
		if (autoFlush && buffer != null && buffer.length() > bufferSize) {
			_flush(true);
		}
	}

	private StringBuilder adoptOrAlloc() {
		PageContextImpl pcImpl = (PageContextImpl) pc;
		// child PCs are recycled outside the release/initialize lifecycle — can't reuse safely
		if (pcImpl.isChild()) return new StringBuilder(CHILD_INITIAL_BUFFER_SIZE);
		WriterPool pool = pcImpl.getWriterPool();
		StringBuilder existing = pool.responseBuffer;
		if (existing != null && existing.capacity() <= MAX_REUSABLE_BUFFER_SIZE) {
			existing.setLength(0);
			return existing;
		}
		pool.responseBuffer = new StringBuilder(INITIAL_BUFFER_SIZE);
		return pool.responseBuffer;
	}

	protected void initOut() throws IOException {
		if (out == null) {
			out = getOutputStream(false);
			// out=response.getWriter();
		}
	}

	@Override
	public void print(char[] arg) throws IOException {
		if (buffer == null) buffer = adoptOrAlloc();
		buffer.append(arg);
		_check();
	}

	@Override
	public void setBufferConfig(int bufferSize, boolean autoFlush) throws IOException {
		this.bufferSize = bufferSize;
		this.autoFlush = autoFlush;
		_check();
	}

	@Override
	public void appendHTMLBody(String text) throws IOException {

		if (htmlBody == null) htmlBody = new StringBuilder(256);

		htmlBody.append(text);
	}

	@Override
	public void writeHTMLBody(String text) throws IOException {

		if (flushed) throw new IOException("Page is already flushed");

		htmlBody = new StringBuilder(text);
	}

	@Override
	public String getHTMLBody() throws IOException {

		if (flushed) throw new IOException("Page is already flushed");

		return htmlBody == null ? "" : htmlBody.toString();
	}

	@Override
	public void flushHTMLBody() throws IOException {
		if (htmlBody != null) {
			if (buffer == null) buffer = adoptOrAlloc();
			buffer.append(htmlBody);
			resetHTMLBody();
		}
	}

	@Override
	public void resetHTMLBody() throws IOException {
		if (flushed) throw new IOException("Page is already flushed");
		htmlBody = null;
	}

	@Override
	public void appendHTMLHead(String text) throws IOException {

		if (flushed) throw new IOException("Page is already flushed");

		if (htmlHead == null) htmlHead = new StringBuilder(256);

		htmlHead.append(text);
	}

	@Override
	public void writeHTMLHead(String text) throws IOException {

		if (flushed) throw new IOException("Page is already flushed");

		htmlHead = new StringBuilder(text);
	}

	@Override
	public String getHTMLHead() throws IOException {

		if (flushed) throw new IOException("Page is already flushed");

		return htmlHead == null ? "" : htmlHead.toString();
	}

	@Override
	public void flushHTMLHead() throws IOException {
		if (htmlHead != null) {
			if (buffer == null) buffer = adoptOrAlloc();
			buffer.append(htmlHead);
			resetHTMLHead();
		}
	}

	@Override
	public void resetHTMLHead() throws IOException {
		if (flushed) throw new IOException("Page is already flushed");
		htmlHead = null;
	}

	/**
	 * just a wrapper function for ACF
	 * 
	 * @throws IOException
	 */
	public void initHeaderBuffer() throws IOException {
		resetHTMLHead();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (buffer == null) buffer = adoptOrAlloc();
		buffer.append(cbuf, off, len);
		_check();
	}

	@Override
	public void clear() throws IOException {
		if (flushed) throw new IOException("Response buffer is already flushed");
		clearBuffer();
	}

	@Override
	public void clearBuffer() {
		buffer = null;
	}

	@Override
	public void flush() throws IOException {
		flushBuffer(this.closeConn);
		out.flush();
		response.flushBuffer();
	}

	private void _flush(boolean closeConn) throws IOException {
		flushBuffer(closeConn);
		out.flush();
		response.flushBuffer();
	}

	/**
	 * Flush the output buffer to the underlying character stream, without flushing the stream itself.
	 * This method is non-private only so that it may be invoked by PrintStream. @throws
	 * IOException @throws
	 */
	protected final void flushBuffer(boolean closeConn) throws IOException {
		if (!flushed && closeConn) {
			response.setHeader("connection", "close");
			// if(showVersion)response.setHeader(Constants.NAME+"-Version", version);

		}
		initOut();
		Charset charset = ReqRspUtil.getCharacterEncoding(null, response);
		byte[] barrForCache = null;
		if (htmlHead == null && htmlBody == null) {
			if (buffer != null && buffer.length() > 0) {
				if (cacheItem != null && cacheItem.isValid()) {
					ByteArrayOutputStream collector = new ByteArrayOutputStream(
							Math.max(64, buffer.length()));
					writeBufferTo(collector, charset);
					barrForCache = collector.toByteArray();
					cacheItem.store(barrForCache, flushed);
					out.write(barrForCache);
				}
				else {
					writeBufferTo(out, charset);
				}
			}
		}
		else {
			byte[] barr = _toString(true).getBytes(charset);
			if (cacheItem != null && cacheItem.isValid()) {
				cacheItem.store(barr, flushed);
			}
			out.write(barr);
		}
		flushed = true;

		buffer = null; // to not change to clearBuffer, produce problem with CFMLWriterWhiteSpace.clearBuffer
	}

	private void writeBufferTo(OutputStream os, Charset charset) throws IOException {
		int len = buffer.length();
		if (len == 0) return;
		PageContextImpl pcImpl = (PageContextImpl) pc;
		boolean child = pcImpl.isChild();
		WriterPool pool = child ? null : pcImpl.getWriterPool();
		char[] tmp = adoptEncodeBuffer(pool, child);
		CharBuffer cb = adoptEncodeCharBuffer(pool, child, tmp);
		ByteBuffer bb = adoptEncodeByteBuffer(pool, child);
		CharsetEncoder enc = adoptEncoder(pool, child, charset);

		bb.clear();
		int off = 0;
		while (off < len) {
			int n = Math.min(OUTPUT_ENCODE_BUFFER_SIZE, len - off);
			buffer.getChars(off, off + n, tmp, 0);
			// Don't end a non-final chunk on a lone high surrogate. CharsetEncoder
			// would leave it in cb (per contract), but we overwrite cb's backing
			// array on the next iteration — the high surrogate would be lost.
			// Push the boundary back one char so the pair stays whole in chunk N+1.
			if (n > 1 && off + n < len && Character.isHighSurrogate(tmp[n - 1])) {
				n--;
			}
			cb.position(0).limit(n);
			boolean endOfInput = (off + n == len);
			encodeAndDrain(enc, cb, bb, os, endOfInput);
			off += n;
		}
		flushEncoder(enc, bb, os);
	}

	private static void encodeAndDrain(CharsetEncoder enc, CharBuffer cb, ByteBuffer bb, OutputStream os, boolean endOfInput) throws IOException {
		while (true) {
			CoderResult r = enc.encode(cb, bb, endOfInput);
			if (r.isUnderflow()) return;
			if (r.isOverflow()) {
				drainByteBuffer(bb, os);
				continue;
			}
			r.throwException();
		}
	}

	private static void flushEncoder(CharsetEncoder enc, ByteBuffer bb, OutputStream os) throws IOException {
		while (true) {
			CoderResult r = enc.flush(bb);
			if (r.isUnderflow()) break;
			if (r.isOverflow()) drainByteBuffer(bb, os);
		}
		if (bb.position() > 0) drainByteBuffer(bb, os);
	}

	private static void drainByteBuffer(ByteBuffer bb, OutputStream os) throws IOException {
		bb.flip();
		os.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
		bb.clear();
	}

	private static char[] adoptEncodeBuffer(WriterPool pool, boolean child) {
		if (child) return new char[OUTPUT_ENCODE_BUFFER_SIZE];
		if (pool.encodeBuffer == null) pool.encodeBuffer = new char[OUTPUT_ENCODE_BUFFER_SIZE];
		return pool.encodeBuffer;
	}

	private static CharBuffer adoptEncodeCharBuffer(WriterPool pool, boolean child, char[] tmp) {
		if (child) return CharBuffer.wrap(tmp);
		if (pool.encodeCharBuffer == null) pool.encodeCharBuffer = CharBuffer.wrap(tmp);
		return pool.encodeCharBuffer;
	}

	private static ByteBuffer adoptEncodeByteBuffer(WriterPool pool, boolean child) {
		if (child) return ByteBuffer.allocate(OUTPUT_ENCODE_BUFFER_SIZE);
		if (pool.encodeByteBuffer == null) pool.encodeByteBuffer = ByteBuffer.allocate(OUTPUT_ENCODE_BUFFER_SIZE);
		return pool.encodeByteBuffer;
	}

	private static CharsetEncoder adoptEncoder(WriterPool pool, boolean child, Charset charset) {
		if (child) return charset.newEncoder();
		if (pool.encoder != null && charset.equals(pool.encoderCharset)) {
			pool.encoder.reset();
			return pool.encoder;
		}
		pool.encoder = charset.newEncoder();
		pool.encoderCharset = charset;
		return pool.encoder;
	}

	private String _toString(boolean releaseHeadData) {

		if (htmlBody == null && htmlHead == null) {
			return buffer == null ? "" : buffer.toString();
		}

		String str = buffer == null ? "" : buffer.toString();
		if (htmlHead != null) {

			int index = StringUtil.indexOfIgnoreCase(str, "</head>");
			if (index > -1) {

				str = StringUtil.insertAt(str, htmlHead, index);
			}
			else {

				index = StringUtil.indexOfIgnoreCase(str, "<head>") + 7;
				if (index > 6) {

					str = StringUtil.insertAt(str, htmlHead, index);
				}
				else {

					str = htmlHead.append(str).toString();
				}
			}
		}

		if (htmlBody != null) {

			int index = StringUtil.indexOfIgnoreCase(str, "</body>");
			if (index > -1) {

				str = StringUtil.insertAt(str, htmlBody, index);
			}
			else {

				str += htmlBody.toString();
			}
		}

		if (releaseHeadData) {
			htmlBody = null;
			htmlHead = null;
		}

		return str;
	}

	@Override
	public String toString() {
		return _toString(false);
	}

	@Override
	public void close() throws IOException {
		if (response == null || closed) return;
		// boolean closeConn=true;
		if (out == null) {
			if (response.isCommitted()) {
				closed = true;
				return;
			}
			Charset charset = ReqRspUtil.getCharacterEncoding(null, response);
			PageContextImpl pcImpl = (PageContextImpl) pc;
			boolean child = pcImpl.isChild();
			WriterPool pool = child ? null : pcImpl.getWriterPool();
			ByteArrayOutputStream collector = null;
			byte[] barrDirect = null;
			int barrLen = 0;
			if (htmlHead == null && htmlBody == null) {
				if (buffer != null && buffer.length() > 0) {
					if (pool != null && pool.baos != null) {
						pool.baos.reset();
						collector = pool.baos;
					}
					else {
						collector = new ByteArrayOutputStream(buffer.length());
						if (pool != null) pool.baos = collector;
					}
					writeBufferTo(collector, charset);
					barrLen = collector.size();
				}
			}
			else {
				barrDirect = _toString(true).getBytes(charset);
				barrLen = barrDirect.length;
			}

			if (cacheItem != null) {
				cacheItem.store(collector != null ? collector.toByteArray() : (barrDirect != null ? barrDirect : new byte[0]), false);
				// writeCache(barr,false);
			}

			if (closeConn) response.setHeader("connection", "close");
			// if(showVersion)response.setHeader(Constants.NAME+"-Version", version);
			boolean allowCompression;
			if (barrLen <= MIN_COMPRESS_BYTES) allowCompression = false;
			else if (_allowCompression != null) allowCompression = _allowCompression.booleanValue();
			else allowCompression = pcImpl.getAllowCompression();
			out = getOutputStream(allowCompression);

			if (contentLength && !(out instanceof GZIPOutputStream)) ReqRspUtil.setContentLength(response, barrLen);

			if (collector != null) collector.writeTo(out);
			else if (barrDirect != null) out.write(barrDirect);
			out.flush();
			out.close();

			out = null;
		}
		else {
			_flush(closeConn);
			out.close();
			out = null;
		}
		closed = true;
	}

	private OutputStream getOutputStream(boolean allowCompression) throws IOException {

		if (allowCompression) {

			String encodings = ReqRspUtil.getHeader(request, "Accept-Encoding", "");
			if (encodings != null && encodings.indexOf("gzip") != -1) {
				boolean inline = HttpServletResponseWrap.get();
				if (!inline) {
					ServletOutputStream os = response.getOutputStream();
					response.setHeader("Content-Encoding", "gzip");
					return new GZIPOutputStream(os);
				}
			}
		}
		return response.getOutputStream();
	}

	@Override
	public int getRemaining() {
		return bufferSize - (buffer == null ? 0 : buffer.length());
	}

	@Override
	public void newLine() throws IOException {
		println();
	}

	@Override
	public void print(boolean arg) throws IOException {
		print(arg ? new char[] { 't', 'r', 'u', 'e' } : new char[] { 'f', 'a', 'l', 's', 'e' });
	}

	@Override
	public void print(char arg) throws IOException {
		if (buffer == null) buffer = adoptOrAlloc();
		buffer.append(arg);
		_check();
	}

	@Override
	public void print(int arg) throws IOException {
		_print(String.valueOf(arg));
	}

	@Override
	public void print(long arg) throws IOException {
		_print(String.valueOf(arg));

	}

	@Override
	public void print(float arg) throws IOException {
		_print(String.valueOf(arg));
	}

	@Override
	public void print(double arg) throws IOException {
		_print(String.valueOf(arg));
	}

	@Override
	public void print(String arg) throws IOException {
		if (buffer == null) buffer = adoptOrAlloc();
		buffer.append(arg);
		_check();
	}

	@Override
	public void print(Object arg) throws IOException {
		_print(String.valueOf(arg));
	}

	@Override
	public void println() throws IOException {
		_print("\n");

	}

	@Override
	public void println(boolean arg) throws IOException {
		print(arg ? new char[] { 't', 'r', 'u', 'e', '\n' } : new char[] { 'f', 'a', 'l', 's', 'e', '\n' });
	}

	@Override
	public void println(char arg) throws IOException {
		print(new char[] { arg, '\n' });
	}

	@Override
	public void println(int arg) throws IOException {
		print(arg);
		println();
	}

	@Override
	public void println(long arg) throws IOException {
		print(arg);
		println();
	}

	@Override
	public void println(float arg) throws IOException {
		print(arg);
		println();
	}

	@Override
	public void println(double arg) throws IOException {
		print(arg);
		println();
	}

	@Override
	public void println(char[] arg) throws IOException {
		print(arg);
		println();
	}

	@Override
	public void println(String arg) throws IOException {
		_print(arg);
		println();
	}

	@Override
	public void println(Object arg) throws IOException {
		print(arg);
		println();
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		print(cbuf);
	}

	@Override
	public void write(int c) throws IOException {
		print(c);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(), off, len);
	}

	@Override
	public void write(String str) throws IOException {
		if (buffer == null) buffer = adoptOrAlloc();
		buffer.append(str);
		_check();
	}

	@Override
	public void writeRaw(String str) throws IOException {
		_print(str);
	}

	/**
	 * @return Returns the flushed.
	 */
	public boolean isFlushed() {
		return flushed;
	}

	@Override
	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	private void _print(String arg) throws IOException {
		if (buffer == null) buffer = adoptOrAlloc();
		buffer.append(arg);
		_check();
	}

	@Override
	public OutputStream getResponseStream() throws IOException {
		initOut();
		return out;
	}

	@Override
	public void doCache(lucee.runtime.cache.legacy.CacheItem ci) {
		this.cacheItem = ci;
	}

	/**
	 * @return the cacheResource
	 */
	@Override
	public CacheItem getCacheItem() {
		return cacheItem;
	}

	// only for compatibility to other vendors
	public String getString() {
		return toString();
	}

	@Override
	public void setAllowCompression(boolean allowCompression) {
		this._allowCompression = Caster.toBoolean(allowCompression);
	}

}
/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	private static final int BUFFER_SIZE = 100000;
	// private static final String VERSIONj = Info.getVersionAsString();
	private OutputStream out;
	private HttpServletResponse response;
	private boolean flushed;
	private StringBuilder htmlHead;
	private StringBuilder htmlBody;
	private StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
	private boolean closed = false;
	private boolean closeConn;
	private boolean showVersion;
	private boolean contentLength;
	private CacheItem cacheItem;
	private HttpServletRequest request;
	private Boolean _allowCompression;
	private PageContext pc;
	private String version;

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
		this.showVersion = showVersion;
		this.contentLength = contentLength;
		// this.allowCompression=allowCompression;
		version = pc.getConfig().getFactory().getEngine().getInfo().getVersion().toString();
	}

	/*
	 * * constructor of the class
	 * 
	 * @param response Response Object / public JspWriterImpl(HttpServletResponse response) {
	 * this(response, BUFFER_SIZE, false); }
	 */

	private void _check() throws IOException {
		if (autoFlush && buffer.length() > bufferSize) {
			_flush(true);
		}
	}

	/**
	 * @throws IOException
	 */
	protected void initOut() throws IOException {
		if (out == null) {
			out = getOutputStream(false);
			// out=response.getWriter();
		}
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(char[])
	 */
	@Override
	public void print(char[] arg) throws IOException {
		buffer.append(arg);
		_check();
	}

	/**
	 * reset configuration of buffer
	 * 
	 * @param bufferSize size of the buffer
	 * @param autoFlush does the buffer autoflush
	 * @throws IOException
	 */
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

			buffer.append(htmlBody);
			resetHTMLBody();
		}
	}

	/**
	 * @see lucee.runtime.writer.CFMLWriter#resetHTMLHead()
	 */
	@Override
	public void resetHTMLBody() throws IOException {
		if (flushed) throw new IOException("Page is already flushed");
		htmlBody = null;
	}

	/**
	 * 
	 * @param text
	 * @throws IOException
	 */
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

	/**
	 * @see lucee.runtime.writer.CFMLWriter#getHTMLHead()
	 */
	@Override
	public String getHTMLHead() throws IOException {

		if (flushed) throw new IOException("Page is already flushed");

		return htmlHead == null ? "" : htmlHead.toString();
	}

	@Override
	public void flushHTMLHead() throws IOException {

		if (htmlHead != null) {

			buffer.append(htmlHead);
			resetHTMLHead();
		}
	}

	/**
	 * @see lucee.runtime.writer.CFMLWriter#resetHTMLHead()
	 */
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

	/**
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		buffer.append(cbuf, off, len);
		_check();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#clear()
	 */
	@Override
	public void clear() throws IOException {
		if (flushed) throw new IOException("Response buffer is already flushed");
		clearBuffer();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#clearBuffer()
	 */
	@Override
	public void clearBuffer() {
		buffer = new StringBuilder(BUFFER_SIZE);
	}

	/**
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		flushBuffer(true);
		// weil flushbuffer das out erstellt muss ich nicht mehr checken
		out.flush();
	}

	/**
	 * @see java.io.Writer#flush()
	 */
	private void _flush(boolean closeConn) throws IOException {
		flushBuffer(closeConn);
		// weil flushbuffer das out erstellt muss ich nicht mehr checken
		out.flush();

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
		byte[] barr = _toString(true).getBytes(ReqRspUtil.getCharacterEncoding(null, response));

		if (cacheItem != null && cacheItem.isValid()) {
			cacheItem.store(barr, flushed);
			// writeCache(barr,flushed);
		}
		flushed = true;
		out.write(barr);

		buffer = new StringBuilder(BUFFER_SIZE); // to not change to clearBuffer, produce problem with CFMLWriterWhiteSpace.clearBuffer
	}

	private String _toString(boolean releaseHeadData) {

		if (htmlBody == null && htmlHead == null) return buffer.toString();

		String str = buffer.toString();

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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return _toString(false);
	}

	/**
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		if (response == null || closed) return;
		// boolean closeConn=true;
		if (out == null) {
			if (response.isCommitted()) {
				closed = true;
				return;
			}
			// print.out(_toString());
			byte[] barr = _toString(true).getBytes(ReqRspUtil.getCharacterEncoding(null, response));

			if (cacheItem != null) {
				cacheItem.store(barr, false);
				// writeCache(barr,false);
			}

			if (closeConn) response.setHeader("connection", "close");
			// if(showVersion)response.setHeader(Constants.NAME+"-Version", version);
			boolean allowCompression;
			if (barr.length <= 512) allowCompression = false;
			else if (_allowCompression != null) allowCompression = _allowCompression.booleanValue();
			else allowCompression = ((PageContextImpl) pc).getAllowCompression();
			out = getOutputStream(allowCompression);

			if (contentLength && !(out instanceof GZIPOutputStream)) ReqRspUtil.setContentLength(response, barr.length);

			out.write(barr);
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
			if (encodings.indexOf("gzip") != -1) {
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

	/*
	 * private void writeCache(byte[] barr,boolean append) throws IOException { cacheItem.store(barr,
	 * append); //IOUtil.copy(new ByteArrayInputStream(barr),
	 * cacheItem.getResource().getOutputStream(append),true,true);
	 * //MetaData.getInstance(cacheItem.getDirectory()).add(cacheItem.getName(), cacheItem.getRaw()); }
	 */

	/**
	 * @see javax.servlet.jsp.JspWriter#getRemaining()
	 */
	@Override
	public int getRemaining() {
		return bufferSize - buffer.length();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#newLine()
	 */
	@Override
	public void newLine() throws IOException {
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(boolean)
	 */
	@Override
	public void print(boolean arg) throws IOException {
		print(arg ? new char[] { 't', 'r', 'u', 'e' } : new char[] { 'f', 'a', 'l', 's', 'e' });
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(char)
	 */
	@Override
	public void print(char arg) throws IOException {
		buffer.append(arg);
		_check();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(int)
	 */
	@Override
	public void print(int arg) throws IOException {
		_print(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(long)
	 */
	@Override
	public void print(long arg) throws IOException {
		_print(String.valueOf(arg));

	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(float)
	 */
	@Override
	public void print(float arg) throws IOException {
		_print(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(double)
	 */
	@Override
	public void print(double arg) throws IOException {
		_print(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(java.lang.String)
	 */
	@Override
	public void print(String arg) throws IOException {
		buffer.append(arg);
		_check();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#print(java.lang.Object)
	 */
	@Override
	public void print(Object arg) throws IOException {
		_print(String.valueOf(arg));
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println()
	 */
	@Override
	public void println() throws IOException {
		_print("\n");

	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(boolean)
	 */
	@Override
	public void println(boolean arg) throws IOException {
		print(arg ? new char[] { 't', 'r', 'u', 'e', '\n' } : new char[] { 'f', 'a', 'l', 's', 'e', '\n' });
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(char)
	 */
	@Override
	public void println(char arg) throws IOException {
		print(new char[] { arg, '\n' });
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(int)
	 */
	@Override
	public void println(int arg) throws IOException {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(long)
	 */
	@Override
	public void println(long arg) throws IOException {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(float)
	 */
	@Override
	public void println(float arg) throws IOException {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(double)
	 */
	@Override
	public void println(double arg) throws IOException {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(char[])
	 */
	@Override
	public void println(char[] arg) throws IOException {
		print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(java.lang.String)
	 */
	@Override
	public void println(String arg) throws IOException {
		_print(arg);
		println();
	}

	/**
	 * @see javax.servlet.jsp.JspWriter#println(java.lang.Object)
	 */
	@Override
	public void println(Object arg) throws IOException {
		print(arg);
		println();
	}

	/**
	 * @see java.io.Writer#write(char[])
	 */
	@Override
	public void write(char[] cbuf) throws IOException {
		print(cbuf);
	}

	/**
	 * @see java.io.Writer#write(int)
	 */
	@Override
	public void write(int c) throws IOException {
		print(c);
	}

	/**
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(), off, len);
	}

	/**
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write(String str) throws IOException {
		buffer.append(str);
		_check();
	}

	/**
	 * @see lucee.runtime.writer.CFMLWriter#writeRaw(java.lang.String)
	 */
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
		buffer.append(arg);
		_check();
	}

	/**
	 * @see lucee.runtime.writer.CFMLWriter#getResponseStream()
	 */
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
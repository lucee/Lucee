/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.net.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.lang.Pair;
import lucee.commons.net.URLEncoder;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTimeImpl;

/**
 * 
 */
public final class HttpServletResponseWrap extends HttpServletResponseWrapper implements HttpServletResponse, Serializable {

	private Cookie[] cookies = new Cookie[0];
	private Pair[] headers = new Pair[0];
	private int status = 200;
	private String statusCode = "OK";
	private String charset = "ISO-8859-1";
	private int contentLength = -1;
	private String contentType = null;
	private Locale locale = Locale.getDefault();
	private int bufferSize = -1;
	private boolean commited;
	// private byte[] outputDatad;
	private OutputStream out;// =new DevNullOutputStream();
	private boolean outInit = false;
	private PrintWriter writer;
	private ServletOutputStreamDummy outputStream;

	private static ThreadLocal<Boolean> local = new ThreadLocal<Boolean>();

	public static void set(boolean value) {
		local.set(Caster.toBoolean(value));
	}

	public static boolean get() {
		return Caster.toBooleanValue(local.get(), false);
	}

	public static void release() {
		local.set(Boolean.FALSE);
	}

	/**
	 * Constructor of the class
	 */
	public HttpServletResponseWrap(HttpServletResponse rsp) {
		this(rsp, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM);
	}

	public HttpServletResponseWrap(HttpServletResponse rsp, OutputStream out) {
		super(rsp);
		this.out = out;
	}

	@Override
	public void addCookie(Cookie cookie) {
		Cookie[] tmp = new Cookie[cookies.length + 1];
		for (int i = 0; i < cookies.length; i++) {
			tmp[i] = cookies[i];
		}
		tmp[cookies.length] = cookie;
		cookies = tmp;
	}

	@Override
	public boolean containsHeader(String key) {
		return ReqRspUtil.get(headers, key) != null;
	}

	@Override
	public String encodeURL(String value) {
		return URLEncoder.encode(value);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return URLEncoder.encode(url);
	}

	@Override
	public String encodeUrl(String value) {
		return URLEncoder.encode(value);
	}

	@Override
	public String encodeRedirectUrl(String value) {
		return URLEncoder.encode(value);
	}

	@Override
	public void sendError(int code, String codeText) throws IOException {
		// TODO impl
	}

	@Override
	public void sendError(int code) throws IOException {
		// TODO impl
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		addHeader("location", location);
	}

	@Override
	public void setDateHeader(String key, long value) {
		setHeader(key, new DateTimeImpl(value, false).castToString());
	}

	@Override
	public void addDateHeader(String key, long value) {
		addHeader(key, new DateTimeImpl(value, false).castToString());
	}

	@Override
	public void setHeader(String key, String value) {
		headers = ReqRspUtil.set(headers, key, value);
	}

	@Override
	public void addHeader(String key, String value) {
		headers = ReqRspUtil.add(headers, key, value);
	}

	@Override
	public void setIntHeader(String key, int value) {
		setHeader(key, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String key, int value) {
		addHeader(key, String.valueOf(value));
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void setStatus(int status, String statusCode) {
		setStatus(status);
		this.statusCode = statusCode;
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	public void setCharacterEncoding(String charset) {
		this.charset = charset;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		// if(writer!=null) throw new IOException("output already initallised as Writer");
		if (outputStream != null) return outputStream;
		return outputStream = new ServletOutputStreamDummy(out);
	}

	public ServletOutputStream getExistingOutputStream() {
		return outputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		// if(outputStream!=null) throw new IOException("output already initallised as OutputStream");
		if (writer != null) return writer;
		return writer = new PrintWriter(getOutputStream());

	}

	public PrintWriter getExistingWriter() {
		return writer;
	}

	@Override
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void setBufferSize(int size) {
		this.bufferSize = size;
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) writer.flush();
		else if (outputStream != null) outputStream.flush();
		commited = true;
	}

	@Override
	public void resetBuffer() {
		commited = true;
	}

	@Override
	public boolean isCommitted() {
		return commited;
	}

	@Override
	public void reset() {
		commited = true;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @return the charset
	 */
	public String getCharsetEncoding() {
		return charset;
	}

	/**
	 * @return the commited
	 */
	public boolean isCommited() {
		return commited;
	}

	/**
	 * @return the contentLength
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @return the cookies
	 */
	public Cookie[] getCookies() {
		return cookies;
	}

	/**
	 * @return the headers
	 */
	public Pair[] getHeaders() {
		return headers;
	}

	/*
	 * *
	 * 
	 * @return the outputData / public byte[] getOutputData() { return outputData; }
	 * 
	 * public void setOutputData(byte[] outputData) { this.outputData=outputData; }
	 */

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

}
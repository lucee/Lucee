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
package lucee.runtime.tag;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PostContentAbort;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Defines the MIME type returned by the current page. Optionally, lets you specify the name of a
 * file to be returned with the page.
 **/
public final class Content extends BodyTagImpl {

	private static final int RANGE_NONE = 0;
	private static final int RANGE_YES = 1;
	private static final int RANGE_NO = 2;

	/** Defines the File/ MIME content type returned by the current page. */
	private String type;

	/** The name of the file being retrieved */
	private String strFile;

	/**
	 * Yes or No. Yes discards output that precedes the call to cfcontent. No preserves the output that
	 * precedes the call. Defaults to Yes. The reset and file attributes are mutually exclusive. If you
	 * specify a file, the reset attribute has no effect.
	 */
	private boolean reset = true;

	private int _range = RANGE_NONE;

	/**
	 * Yes or No. Yes deletes the file after the download operation. Defaults to No. This attribute
	 * applies only if you specify a file with the file attribute.
	 */
	private boolean deletefile = false;

	private byte[] content;

	@Override
	public void release() {
		super.release();
		type = null;
		strFile = null;
		reset = true;
		deletefile = false;
		content = null;
		_range = RANGE_NONE;
	}

	/**
	 * set the value type Defines the File/ MIME content type returned by the current page.
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		this.type = type.trim();
	}

	public void setRange(boolean range) {
		this._range = range ? RANGE_YES : RANGE_NO;
	}

	/**
	 * set the value file The name of the file being retrieved
	 * 
	 * @param file value to set
	 **/
	public void setFile(String file) {
		this.strFile = file;
	}

	/**
	 * the content to output as binary
	 * 
	 * @param content value to set
	 * @deprecated replaced with <code>{@link #setVariable(Object)}</code>
	 **/
	@Deprecated
	public void setContent(byte[] content) {
		this.content = content;
	}

	public void setVariable(Object variable) throws PageException {
		if (variable instanceof String) this.content = Caster.toBinary(pageContext.getVariable((String) variable));
		else this.content = Caster.toBinary(variable);
	}

	/**
	 * set the value reset Yes or No. Yes discards output that precedes the call to cfcontent. No
	 * preserves the output that precedes the call. Defaults to Yes. The reset and file attributes are
	 * mutually exclusive. If you specify a file, the reset attribute has no effect.
	 * 
	 * @param reset value to set
	 **/
	public void setReset(boolean reset) {
		this.reset = reset;
	}

	/**
	 * set the value deletefile Yes or No. Yes deletes the file after the download operation. Defaults
	 * to No. This attribute applies only if you specify a file with the file attribute.
	 * 
	 * @param deletefile value to set
	 **/
	public void setDeletefile(boolean deletefile) {
		this.deletefile = deletefile;
	}

	@Override
	public int doStartTag() throws PageException {
		// try {
		return _doStartTag();
		/*
		 * } catch (IOException e) { throw Caster.toPageException(e); }
		 */
	}

	private int _doStartTag() throws PageException {
		// check the file before doing anything else
		Resource file = null;
		if (content == null && !StringUtil.isEmpty(strFile)) file = ResourceUtil.toResourceExisting(pageContext, strFile);

		// get response object
		HttpServletResponse rsp = pageContext.getHttpServletResponse();

		// check committed
		if (rsp.isCommitted()) throw new ApplicationException("Content was already flushed", "you can't rewrite the header of a response after part of the page was flushed");

		// set type
		if (!StringUtil.isEmpty(type, true)) {
			type = type.trim();
			ReqRspUtil.setContentType(rsp, type);

			// TODO more dynamic implementation, configuration in admin?
			if (!HTTPUtil.isTextMimeType(type)) {
				((PageContextImpl) pageContext).getRootOut().setAllowCompression(false);
			}
		}

		Range[] ranges = getRanges();
		boolean hasRanges = ranges != null && ranges.length > 0;
		if (_range == RANGE_YES || hasRanges) {
			rsp.setHeader("Accept-Ranges", "bytes");
		}
		else if (_range == RANGE_NO) {
			rsp.setHeader("Accept-Ranges", "none");
			hasRanges = false;
		}

		// set content
		if (this.content != null || file != null) {
			pageContext.clear();
			InputStream is = null;
			OutputStream os = null;
			long totalLength, contentLength;
			try {
				os = getOutputStream();

				if (content != null) {
					// ReqRspUtil.setContentLength(rsp,content.length);
					contentLength = content.length;
					totalLength = content.length;
					is = new BufferedInputStream(new ByteArrayInputStream(content));
				}
				else {
					// ReqRspUtil.setContentLength(rsp,file.length());
					pageContext.getConfig().getSecurityManager().checkFileLocation(file);
					contentLength = totalLength = file.length();
					is = IOUtil.toBufferedInputStream(file.getInputStream());
				}

				// write
				if (!hasRanges) IOUtil.copy(is, os, false, false);
				else {
					contentLength = 0;
					long off, len, to;
					for (int i = 0; i < ranges.length; i++) {
						off = ranges[i].from;
						if (ranges[i].to == -1) {
							len = -1;
							to = totalLength - 1;
						}
						else {
							to = ranges[i].to;
							if (to >= totalLength) to = totalLength - 1;
							len = to - ranges[i].from + 1;
						}
						rsp.addHeader("Content-Range", "bytes " + off + "-" + to + "/" + Caster.toString(totalLength));
						rsp.setStatus(206);
						// print.e("Content-Range: bytes "+off+"-"+to+"/"+Caster.toString(totalLength));
						contentLength += to - off + 1L;
						// ReqRspUtil.setContentLength(rsp,len);
						IOUtil.copy(is, os, off, len);
					}
				}
				if (!(os instanceof GZIPOutputStream)) ReqRspUtil.setContentLength(rsp, contentLength);
			}
			catch (IOException ioe) {}
			finally {
				IOUtil.flushEL(os);
				IOUtil.closeEL(is, os);
				if (deletefile && file != null) ResourceUtil.removeEL(file, true);
				((PageContextImpl) pageContext).getRootOut().setClosed(true);
			}
			throw new PostContentAbort();
		}
		// clear current content
		else if (reset) pageContext.clear();

		return EVAL_BODY_INCLUDE;// EVAL_PAGE;
	}

	private OutputStream getOutputStream() throws PageException, IOException {
		try {
			return pageContext.getResponseStream();
		}
		catch (IllegalStateException ise) {
			throw new TemplateException("Content was already sent to user, flush");
		}
	}

	@Override
	public int doEndTag() {
		return strFile == null ? EVAL_PAGE : SKIP_PAGE;
	}

	/**
	 * sets if tag has a body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {}

	private Range[] getRanges() {
		HttpServletRequest req = pageContext.getHttpServletRequest();
		Enumeration names = req.getHeaderNames();

		if (names == null) return null;
		String name;
		Range[] range;
		while (names.hasMoreElements()) {
			name = (String) names.nextElement();

			if ("range".equalsIgnoreCase(name)) {
				range = getRanges(name, req.getHeader(name));
				if (range != null) return range;
			}
		}
		return null;
	}

	private Range[] getRanges(String name, String range) {
		if (StringUtil.isEmpty(range, true)) return null;
		range = StringUtil.removeWhiteSpace(range);
		if (range.indexOf("bytes=") == 0) range = range.substring(6);
		String[] arr = null;
		try {
			arr = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(range, ','));
		}
		catch (PageException e) {
			failRange(name, range);
			return null;
		}
		String item;
		int index;
		long from, to;

		Range[] ranges = new Range[arr.length];
		for (int i = 0; i < ranges.length; i++) {
			item = arr[i].trim();
			index = item.indexOf('-');
			if (index != -1) {
				from = Caster.toLongValue(item.substring(0, index), 0);
				to = Caster.toLongValue(item.substring(index + 1), -1);
				if (to != -1 && from > to) {
					failRange(name, range);
					return null;
					// throw new ExpressionException("invalid range definition, from have to bigger than to
					// ("+from+"-"+to+")");
				}
			}
			else {
				from = Caster.toLongValue(item, 0);
				to = -1;
			}
			ranges[i] = new Range(from, to);

			if (i > 0 && ranges[i - 1].to >= from) {
				LogUtil.log(ThreadLocalPageContext.getConfig(pageContext), Log.LEVEL_ERROR, Content.class.getName(),
						"there is an overlapping of 2 ranges (" + ranges[i - 1] + "," + ranges[i] + ")");
				return null;
			}

		}
		return ranges;
	}

	private void failRange(String name, String range) {
		LogUtil.log(ThreadLocalPageContext.getConfig(pageContext), Log.LEVEL_INFO, Content.class.getName(), "failed to parse the header field [" + name + ":" + range + "]");
	}
}

class Range {

	long from;
	long to;

	public Range(long from, long len) {
		this.from = from;
		this.to = len;
	}

	@Override
	public String toString() {
		return from + "-" + to;
	}
}

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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.system.CallStackGet;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;

public final class Location extends TagImpl {

	/**
	 * Yes or No. clientManagement must be enabled. Yes appends client variable information to the URL
	 * you specify in the url.
	 */
	private boolean addtoken = true;

	/** The URL of the HTML file or CFML page to open. */
	private String url = "";

	private int statuscode = 302;

	private boolean abort = false;

	@Override
	public void release() {
		super.release();
		addtoken = true;
		url = "";
		statuscode = 302;
		abort = false;
	}

	/**
	 * @param statuscode the statuscode to set
	 * @throws ApplicationException
	 */
	public void setStatuscode(double statuscode) throws ApplicationException {
		int sc = (int) statuscode;
		if (sc < 300 || sc > 307) throw new ApplicationException("invalid value for attribute statuscode [" + Caster.toString(statuscode) + "]",
				"attribute must have one of the folloing values [300|301|302|303|304|305|307]");

		this.statuscode = sc;
	}

	/**
	 * set the value addtoken Yes or No. clientManagement must be enabled. Yes appends client variable
	 * information to the URL you specify in the url.
	 * 
	 * @param addtoken value to set
	 **/
	public void setAddtoken(boolean addtoken) {
		this.addtoken = addtoken;
	}

	/**
	 * if set to true then the request will be aborted instead of redirected to allow developers to
	 * troubleshoot code that contains redirects
	 * 
	 * @param abort
	 */
	public void setAbort(boolean abort) {
		this.abort = abort;
	}

	/**
	 * set the value url The URL of the HTML file or CFML page to open.
	 * 
	 * @param url value to set
	 * @throws ApplicationException
	 **/
	public void setUrl(String url) throws ApplicationException {
		this.url = url.trim();
		if (this.url.length() == 0) throw new ApplicationException("invalid url [empty string] for attribute url");
		if (StringUtil.hasLineFeed(url)) throw new ApplicationException("invalid url [" + url + "] for attribute url, carriage-return or line-feed inside the url are not allowed");
	}

	@Override
	public int doStartTag() throws PageException {
		try {
			pageContext.getOut().clear();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		HttpServletResponse rsp = pageContext.getHttpServletResponse();

		url = HTTPUtil.encode(url);

		// add token
		if (addtoken && needId()) {
			String[] arr = url.split("\\?");

			// only string_name
			if (arr.length == 1) {
				url += "?" + pageContext.getURLToken();
			}
			// script_name and query_string
			else if (arr.length > 1) {
				url = arr[0] + "?" + pageContext.getURLToken();
				for (int i = 1; i < arr.length; i++)
					url += "&" + arr[i];
			}
			url = ReqRspUtil.encodeRedirectURLEL(rsp, url);
		}

		Log log = pageContext.getConfig().getLog("trace");
		if (abort) {
			if (log != null && log.getLogLevel() <= Log.LEVEL_ERROR)
				log.log(Log.LEVEL_ERROR, "cftrace", "abort redirect to " + url + " at " + CallStackGet.call(pageContext, "text"));
			throw new ExpressionException("abort redirect to " + url);
		}
		else {
			if (log != null && log.getLogLevel() <= Log.LEVEL_INFO) log.log(Log.LEVEL_INFO, "cftrace", "redirect to " + url + " at " + CallStackGet.call(pageContext, "text"));
		}

		rsp.setHeader("Connection", "close"); // IE unter IIS6, Win2K3 und Resin
		rsp.setStatus(statuscode);
		rsp.setHeader("location", url);

		try {
			pageContext.forceWrite("<html>\n<head>\n\t<title>Document Moved</title>\n");
			// pageContext.forceWrite("\t<script>window.location='"+JSStringFormat.invoke(url)+"';</script>\n");
			pageContext.forceWrite("</head>\n<body>\n\t<h1>Object Moved</h1>\n");
			pageContext.forceWrite("</body>\n</html>");
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		if (pageContext.getConfig().debug()) pageContext.getDebugger().setOutput(false);
		throw new Abort(Abort.SCOPE_REQUEST);
	}

	private boolean needId() {
		ApplicationContext ac = pageContext.getApplicationContext();
		return ac.isSetClientManagement() || ac.isSetSessionManagement();
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}
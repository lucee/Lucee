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
package lucee.runtime.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import lucee.commons.digest.Base64Encoder;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.security.Credentials;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.util.URLResolver;
import lucee.runtime.schedule.ScheduleTaskPro;

class ExecutionThread extends Thread {

	private Config config;
	// private Log log;
	private ScheduleTask task;
	private String charset;

	public ExecutionThread(Config config, ScheduleTask task, String charset) {
		this.config = config;
		this.task = task;
		this.charset = charset;
	}

	@Override
	public void run() {
		if (ThreadLocalPageContext.getConfig() == null && config != null) ThreadLocalConfig.register(config);
		execute(config, task, charset);
	}

	public static void execute(Config config, ScheduleTask task, String charset) {
		Scheduler scheduler = ((ScheduleTaskImpl) task).getScheduler();
		if (scheduler instanceof SchedulerImpl && !((SchedulerImpl) scheduler).active()) return;
		Log log = getLog(config);
		boolean hasError = false;
		String logName = "schedule task:" + task.getTask();
		// init
		// HttpClient client = new HttpClient();
		// client.setStrictMode(false);
		// HttpState state = client.getState();

		String url;
		if (task.getUrl().getQuery() == null) url = task.getUrl().toExternalForm() + "?RequestTimeout=" + (task.getTimeout() / 1000);
		else if (StringUtil.isEmpty(task.getUrl().getQuery())) url = task.getUrl().toExternalForm() + "RequestTimeout=" + (task.getTimeout() / 1000);
		else {
			if (StringUtil.indexOfIgnoreCase(task.getUrl().getQuery() + "", "RequestTimeout") != -1) url = task.getUrl().toExternalForm();
			else url = task.getUrl().toExternalForm() + "&RequestTimeout=" + (task.getTimeout() / 1000);
		}

		// HttpMethod method = new GetMethod(url);
		// HostConfiguration hostConfiguration = client.getHostConfiguration();
		String userAgent = ((ScheduleTaskPro) task).getUserAgent();
		if (StringUtil.isEmpty(userAgent))
			userAgent = Constants.NAME + " Scheduler";
			//userAgent = "CFSCHEDULE"; this old userAgent string is on block listslists
		
		Header[] headers = new Header[] { HTTPEngine.header("User-Agent", userAgent) };

		// method.setRequestHeader("User-Agent","CFSCHEDULE");

		// Userame / Password
		Credentials credentials = task.getCredentials();
		String user = null, pass = null;
		if (credentials != null) {
			user = credentials.getUsername();
			pass = credentials.getPassword();
			// get.addRequestHeader("Authorization","Basic admin:spwwn1p");
			String plainCredentials = user + ":" + pass;
			String base64Credentials = Base64Encoder.encode(plainCredentials.getBytes());
			String authorizationHeader = "Basic " + base64Credentials;
			headers.add( HTTPEngine.header("Authorization", authorizationHeader));
		}

		// Proxy
		ProxyData proxy = ProxyDataImpl.validate(task.getProxyData(), task.getUrl().getHost());
		if (proxy == null) {
			proxy = ProxyDataImpl.validate(config.getProxyData(), task.getUrl().getHost());
		}

		HTTPResponse rsp = null;

		// execute
		log.info(logName, "calling URL [" + url + "]");
		try {
			rsp = HTTPEngine.get(new URL(url), user, pass, task.getTimeout(), true, charset, null, proxy, headers.toArray(new Header[headers.size()]));
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc >= 200 && sc < 300) log.info(logName, "successfully called URL [" + url + "], response code " + sc);
				else log.warn(logName, "called URL [" + url + "] returned response code " + sc);
			}

		}
		catch (Exception e) {
			try {
				log.log(Log.LEVEL_ERROR, logName, e);
			}
			catch (Exception ee) {
				LogUtil.logGlobal(config, "scheduler", e);
				LogUtil.logGlobal(config, "scheduler", ee);
			}
			hasError = true;
		}

		// write file
		Resource file = task.getResource();
		if (!hasError && file != null && task.isPublish()) {
			String n = file.getName();
			if (n.indexOf("{id}") != -1) {
				n = StringUtil.replace(n, "{id}", CreateUUID.invoke(), false);
				file = file.getParentResource().getRealResource(n);
			}

			if (isText(rsp) && task.isResolveURL()) {

				String str;
				try {
					InputStream stream = rsp.getContentAsStream();
					str = stream == null ? "" : IOUtil.toString(stream, (Charset) null);
					if (str == null) str = "";
				}
				catch (IOException e) {
					str = e.getMessage();
				}

				try {
					str = new URLResolver().transform(str, task.getUrl(), false);
				}
				catch (PageException e) {
					log.log(Log.LEVEL_ERROR, logName, e);
					hasError = true;
				}
				try {
					IOUtil.write(file, str, charset, false);
				}
				catch (IOException e) {
					log.log(Log.LEVEL_ERROR, logName, e);
					hasError = true;
				}
			}
			else {
				try {
					IOUtil.copy(rsp.getContentAsStream(), file, true);
				}
				catch (IOException e) {
					log.log(Log.LEVEL_ERROR, logName, e);
					hasError = true;
				}
			}
			HTTPEngine.closeEL(rsp);
		}
	}

	private static Log getLog(Config config) {
		return config.getLog("scheduler");
	}

	private static boolean isText(HTTPResponse rsp) {
		ContentType ct = rsp.getContentType();
		if (ct == null) return true;
		String mimetype = ct.getMimeType();
		return mimetype == null || mimetype.startsWith("text") || mimetype.startsWith("application/octet-stream");

	}

}
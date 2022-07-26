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

import java.net.URL;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.security.Credentials;
import lucee.commons.security.CredentialsImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.schedule.ScheduleTask;
import lucee.runtime.schedule.ScheduleTaskImpl;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.schedule.ScheduleTaskPro;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.util.KeyConstants;

/**
 * Provides a programmatic interface to the Lucee scheduling engine. You can run a specified page at
 * scheduled intervals with the option to write out static HTML pages. This lets you offer users
 * access to pages that publish data, such as reports, without forcing users to wait while a
 * database transaction is performed in order to populate the data on the page.
 **/
public final class Schedule extends TagImpl {

	private static final short ACTION_RUN = 1;
	private static final short ACTION_UPDATE = 2;
	private static final short ACTION_DELETE = 3;
	private static final short ACTION_LIST = 4;
	private static final short ACTION_PAUSE = 5;
	private static final short ACTION_RESUME = 6;

	/** Password if URL is protected. */
	private String password = "";

	/** Required when action ='update'. The date when scheduling of the task should start. */
	private Date startdate;

	/** Specifies whether to resolve links in the result page to absolute references. */
	private boolean resolveurl;

	/**  */
	private short action;

	/** Host name or IP address of a proxy server. */
	private String proxyserver;

	/** user agent for the http request. */
	private String userAgent;

	/** The date when the scheduled task ends. */
	private Date enddate;

	/** Required with publish ='Yes' A valid filename for the published file. */
	private String strFile;

	/**
	 * Required when creating tasks with action = 'update'. Enter a value in seconds. The time when
	 ** scheduling of the task starts.
	 */
	private Time starttime;

	/**
	 * The port number on the proxy server from which the task is being requested. Default is 80. When
	 ** used with resolveURL, the URLs of retrieved documents that specify a port number are
	 * automatically resolved to preserve links in the retrieved document.
	 */
	private int proxyport = 80;

	/**
	 * The port number on the server from which the task is being scheduled. Default is 80. When used
	 ** with resolveURL, the URLs of retrieved documents that specify a port number are automatically
	 * resolved to preserve links in the retrieved document.
	 */
	private int port = -1;

	/** The time when the scheduled task ends. Enter a value in seconds. */
	private Time endtime;

	/**
	 * Required when creating tasks with action = 'update'. Interval at which task should be scheduled.
	 ** Can be set in seconds or as Once, Daily, Weekly, and Monthly. The default interval is one hour.
	 * The minimum interval is one minute.
	 */
	private String interval;

	/** Specifies whether the result should be saved to a file. */
	private boolean publish;

	/**
	 * Customizes the requestTimeOut for the task operation. Can be used to extend the default timeout
	 ** for operations that require more time to execute.
	 */
	private long requesttimeout = -1;

	/** Username if URL is protected. */
	private String username;

	/** Required when action = 'update'. The URL to be executed. */
	private String url;

	/** Required with publish ='Yes' The path location for the published file. */
	private String strPath;

	/** The name of the task to delete, update, or run. */
	private String task;
	private Scheduler scheduler;

	private String proxyuser;
	private String proxypassword = "";

	private String result = "cfschedule";
	private boolean hidden;
	private boolean readonly;
	private String serverPassword = null;
	private boolean paused;
	private boolean autoDelete;
	private boolean unique;

	public void setAutodelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	/**
	 * @param readonly the readonly to set
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param result The returnvariable to set.
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param proxypassword The proxypassword to set.
	 */
	public void setProxypassword(String proxypassword) {
		this.proxypassword = proxypassword;
	}

	/**
	 * @param proxyuser The proxyuser to set.
	 */
	public void setProxyuser(String proxyuser) {
		this.proxyuser = proxyuser;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * set the value password Password if URL is protected.
	 * 
	 * @param password value to set
	 **/
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * set the value startdate Required when action ='update'. The date when scheduling of the task
	 * should start.
	 * 
	 * @param objStartDate value to set
	 * @throws PageException
	 **/
	public void setStartdate(Object objStartDate) throws PageException {
		if (StringUtil.isEmpty(objStartDate)) return;
		this.startdate = new DateImpl(DateCaster.toDateAdvanced(objStartDate, pageContext.getTimeZone()));
	}

	/**
	 * set the value resolveurl Specifies whether to resolve links in the result page to absolute
	 * references.
	 * 
	 * @param resolveurl value to set
	 **/
	public void setResolveurl(boolean resolveurl) {
		this.resolveurl = resolveurl;
	}

	public void setServerpassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	/**
	 * set the value action
	 * 
	 * @param action value to set
	 * @throws ApplicationException
	 **/
	public void setAction(String action) throws ApplicationException {
		if (StringUtil.isEmpty(action)) return;
		action = action.toLowerCase().trim();
		if (action.equals("run")) this.action = ACTION_RUN;
		else if (action.equals("delete")) this.action = ACTION_DELETE;
		else if (action.equals("update")) this.action = ACTION_UPDATE;
		else if (action.equals("list")) this.action = ACTION_LIST;
		else if (action.equals("lists")) this.action = ACTION_LIST;
		else if (action.equals("pause")) this.action = ACTION_PAUSE;
		else if (action.equals("resume")) this.action = ACTION_RESUME;
		else throw new ApplicationException("attribute action with value [" + action + "] of tag schedule is invalid",
				"valid attributes are [delete,run,update,list,resume,pause]");
	}

	/**
	 * set the value proxyserver Host name or IP address of a proxy server.
	 * 
	 * @param proxyserver value to set
	 **/
	public void setProxyserver(String proxyserver) {
		this.proxyserver = proxyserver;
	}

	/**
	 * set the value of the userAgent for the http request.
	 * 
	 * @param userAgent value to set
	 **/
	public void setUseragent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * set the value enddate The date when the scheduled task ends.
	 * 
	 * @param enddate value to set
	 * @throws PageException
	 **/
	public void setEnddate(Object enddate) throws PageException {
		if (StringUtil.isEmpty(enddate)) return;
		this.enddate = new DateImpl(DateCaster.toDateAdvanced(enddate, pageContext.getTimeZone()));
	}

	/**
	 * set the value file Required with publish ='Yes' A valid filename for the published file.
	 * 
	 * @param file value to set
	 **/
	public void setFile(String file) {
		this.strFile = file;
	}

	/**
	 * set the value starttime Required when creating tasks with action = 'update'. Enter a value in
	 * seconds. The time when scheduling of the task starts.
	 * 
	 * @param starttime value to set
	 * @throws PageException
	 **/
	public void setStarttime(Object starttime) throws PageException {
		if (StringUtil.isEmpty(starttime)) return;
		this.starttime = DateCaster.toTime(pageContext.getTimeZone(), starttime);
	}

	/**
	 * set the value proxyport The port number on the proxy server from which the task is being
	 * requested. Default is 80. When used with resolveURL, the URLs of retrieved documents that specify
	 * a port number are automatically resolved to preserve links in the retrieved document.
	 * 
	 * @param proxyport value to set
	 * @throws PageException
	 **/
	public void setProxyport(Object oProxyport) throws PageException {
		if (StringUtil.isEmpty(oProxyport)) return;
		this.proxyport = Caster.toIntValue(oProxyport);
	}

	/**
	 * set the value port The port number on the server from which the task is being scheduled. Default
	 * is 80. When used with resolveURL, the URLs of retrieved documents that specify a port number are
	 * automatically resolved to preserve links in the retrieved document.
	 * 
	 * @param port value to set
	 * @throws PageException
	 **/
	public void setPort(Object oPort) throws PageException {
		if (StringUtil.isEmpty(oPort)) return;
		this.port = Caster.toIntValue(oPort);
	}

	/**
	 * set the value endtime The time when the scheduled task ends. Enter a value in seconds.
	 * 
	 * @param endtime value to set
	 * @throws PageException
	 **/
	public void setEndtime(Object endtime) throws PageException {
		if (StringUtil.isEmpty(endtime)) return;
		this.endtime = DateCaster.toTime(pageContext.getTimeZone(), endtime);
	}

	/**
	 * set the value operation The type of operation the scheduler performs when executing this task.
	 * 
	 * @param operation value to set
	 * @throws ApplicationException
	 **/
	public void setOperation(String operation) throws ApplicationException {
		if (StringUtil.isEmpty(operation)) return;
		operation = operation.toLowerCase().trim();
		if (!operation.equals("httprequest")) throw new ApplicationException("attribute operation must have the value [HTTPRequest]");
	}

	/**
	 * set the value interval Required when creating tasks with action = 'update'. Interval at which
	 * task should be scheduled. Can be set in seconds or as Once, Daily, Weekly, and Monthly. The
	 * default interval is one hour. The minimum interval is one minute.
	 * 
	 * @param interval value to set
	 **/
	public void setInterval(String interval) {
		if (StringUtil.isEmpty(interval)) return;
		interval = interval.trim().toLowerCase();
		if (interval.equals("week")) this.interval = "weekly";
		else if (interval.equals("day")) this.interval = "daily";
		else if (interval.equals("month")) this.interval = "monthly";
		else if (interval.equals("year")) this.interval = "yearly";
		this.interval = interval;
	}

	/**
	 * set the value publish Specifies whether the result should be saved to a file.
	 * 
	 * @param publish value to set
	 **/
	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	/**
	 * set the value requesttimeout Customizes the requestTimeOut for the task operation. Can be used to
	 * extend the default timeout for operations that require more time to execute.
	 * 
	 * @param requesttimeout value to set
	 **/
	public void setRequesttimeout(Object oRequesttimeout) throws PageException {
		if (StringUtil.isEmpty(oRequesttimeout)) return;
		this.requesttimeout = Caster.toLongValue(oRequesttimeout) * 1000L;
	}

	/**
	 * set the value username Username if URL is protected.
	 * 
	 * @param username value to set
	 **/
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * set the value url Required when action = 'update'. The URL to be executed.
	 * 
	 * @param url value to set
	 **/
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * set the value path Required with publish ='Yes' The path location for the published file.
	 * 
	 * @param path value to set
	 **/
	public void setPath(String path) {
		this.strPath = path;
	}

	/**
	 * set the value task The name of the task to delete, update, or run.
	 * 
	 * @param task value to set
	 **/
	public void setTask(String task) {
		this.task = task;
	}

	@Override
	public int doStartTag() throws PageException {
		scheduler = pageContext.getConfig().getScheduler();

		if (action != ACTION_LIST && task == null) {
			throw new ApplicationException("attribute [task] is required for tag [schedule] when action is not list");
		}

		switch (action) {
		case ACTION_DELETE:
			doDelete();
			break;
		case ACTION_RUN:
			doRun();
			break;
		case ACTION_UPDATE:
			doUpdate();
			break;
		case ACTION_LIST:
			doList();
			break;
		case ACTION_PAUSE:
			doPause(true);
			break;
		case ACTION_RESUME:
			doPause(false);
			break;
		}
		return SKIP_BODY;
	}

	/**
	 * @throws PageException
	 */
	private void doUpdate() throws PageException {
		String message = "missing attribute for tag schedule with action update";
		String detail = "required attributes are [startDate, startTime, URL, interval, operation]";

		Resource file = null;
		// if(publish) {
		if (!StringUtil.isEmpty(strFile) && !StringUtil.isEmpty(strPath)) {
			file = ResourceUtil.toResourceNotExisting(pageContext, strPath);
			file = file.getRealResource(strFile);
		}
		else if (!StringUtil.isEmpty(strFile)) {
			file = ResourceUtil.toResourceNotExisting(pageContext, strFile);
		}
		else if (!StringUtil.isEmpty(strPath)) {
			file = ResourceUtil.toResourceNotExisting(pageContext, strPath);
		}
		if (file != null) pageContext.getConfig().getSecurityManager().checkFileLocation(pageContext.getConfig(), file, serverPassword);

		// missing attributes
		if (startdate == null || starttime == null || url == null || interval == null) throw new ApplicationException(message, detail);

		// timeout
		if (requesttimeout < 0) requesttimeout = pageContext.getRequestTimeout();

		// username/password
		Credentials cr = null;
		if (username != null) cr = CredentialsImpl.toCredentials(username, password);

		try {
			ScheduleTask st = new ScheduleTaskImpl(scheduler, task, file, startdate, starttime, enddate, endtime, url, port, interval, requesttimeout, cr,
					ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword), resolveurl, publish, hidden, readonly, paused, autoDelete, unique, userAgent);
			scheduler.addScheduleTask(st, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		//
	}

	/**
	 * @throws PageException
	 */
	private void doRun() throws PageException {
		try {
			scheduler.runScheduleTask(task, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 */
	private void doDelete() throws PageException {
		try {
			scheduler.removeScheduleTask(task, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doList() throws PageException {
		// if(tr ue) throw new PageRuntimeException("qqq");
		ScheduleTask[] tasks = scheduler.getAllScheduleTasks();
		final String v = "VARCHAR";
		String[] cols = new String[] { "task", "path", "file", "startdate", "starttime", "enddate", "endtime", "url", "port", "interval", "timeout", "username", "password",
				"proxyserver", "proxyport", "proxyuser", "proxypassword", "resolveurl", "publish", "valid", "paused", "autoDelete", "unique", "useragent" };
		String[] types = new String[] { v, v, v, "DATE", "OTHER", "DATE", "OTHER", v, v, v, v, v, v, v, v, v, v, v, "BOOLEAN", v, "BOOLEAN", "BOOLEAN", "BOOLEAN", v };
		lucee.runtime.type.Query query = new QueryImpl(cols, types, tasks.length, "query");
		try {
			for (int i = 0; i < tasks.length; i++) {
				int row = i + 1;
				ScheduleTask task = tasks[i];
				query.setAt(KeyConstants._task, row, task.getTask());
				if (task.getResource() != null) {
					query.setAt(KeyConstants._path, row, task.getResource().getParent());
					query.setAt(KeyConstants._file, row, task.getResource().getName());
				}
				query.setAt("publish", row, Caster.toBoolean(task.isPublish()));
				query.setAt("startdate", row, task.getStartDate());
				query.setAt("starttime", row, task.getStartTime());
				query.setAt("enddate", row, task.getEndDate());
				query.setAt("endtime", row, task.getEndTime());
				query.setAt(KeyConstants._url, row, printUrl(task.getUrl()));
				query.setAt(KeyConstants._port, row, Caster.toString(HTTPUtil.getPort(task.getUrl())));
				query.setAt("interval", row, task.getStringInterval());
				query.setAt("timeout", row, Caster.toString(task.getTimeout() / 1000));
				query.setAt("valid", row, Caster.toString(task.isValid()));
				if (task.hasCredentials()) {
					query.setAt("username", row, task.getCredentials().getUsername());
					query.setAt("password", row, task.getCredentials().getPassword());
				}
				ProxyData pd = task.getProxyData();
				if (ProxyDataImpl.isValid(pd)) {
					query.setAt("proxyserver", row, pd.getServer());
					if (pd.getPort() > 0) query.setAt("proxyport", row, Caster.toString(pd.getPort()));
					if (ProxyDataImpl.hasCredentials(pd)) {
						query.setAt("proxyuser", row, pd.getUsername());
						query.setAt("proxypassword", row, pd.getPassword());
					}
				}
				query.setAt("useragent", row, Caster.toString(((ScheduleTaskPro) task).getUserAgent()));
				query.setAt("resolveurl", row, Caster.toString(task.isResolveURL()));

				query.setAt("paused", row, Caster.toBoolean(task.isPaused()));
				query.setAt("autoDelete", row, Caster.toBoolean(((ScheduleTaskImpl) task).isAutoDelete()));
				query.setAt("unique", row, Caster.toBoolean(((ScheduleTaskImpl) task).unique()));

			}
			pageContext.setVariable(result, query);
		}
		catch (DatabaseException e) {
		}

	}

	private void doPause(boolean pause) throws PageException {
		try {
			((SchedulerImpl) scheduler).pauseScheduleTask(task, pause, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}

	private String printUrl(URL url) {

		String qs = url.getQuery();
		if (qs == null) qs = "";
		else if (qs.length() > 0) qs = "?" + qs;

		String str = url.getProtocol() + "://" + url.getHost() + ":" + Caster.toString(HTTPUtil.getPort(url)) + url.getPath() + qs; 
		return str;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		readonly = false;
		strPath = null;
		strFile = null;
		starttime = null;
		startdate = null;
		endtime = null;
		enddate = null;
		url = null;
		port = -1;
		interval = null;
		requesttimeout = -1;
		username = null;
		password = "";
		proxyserver = null;
		userAgent = null;
		proxyport = 80;
		proxyuser = null;
		proxypassword = "";
		resolveurl = false;
		publish = false;
		result = "cfschedule";
		task = null;
		hidden = false;
		serverPassword = null;
		paused = false;
		unique = false;
		autoDelete = false;
	}

}
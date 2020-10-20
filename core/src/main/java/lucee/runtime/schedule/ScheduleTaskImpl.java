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
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URL;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.Md5;
import lucee.commons.net.HTTPUtil;
import lucee.commons.security.Credentials;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.Time;

/**
 * Define a single schedule Task
 */
public final class ScheduleTaskImpl implements ScheduleTask {

	public static final int INTERVAL_EVEREY = -1;
	public static final int INTERVAL_YEAR = 4;
	private String task;
	private short operation = OPERATION_HTTP_REQUEST;
	private Resource file;
	private Date startDate;
	private Time startTime;
	private URL url;
	private Date endDate;
	private Time endTime;
	private int interval;
	private long timeout;
	private Credentials credentials;
	private ProxyData proxy;
	private String userAgent;
	private boolean resolveURL;

	private long nextExecution;

	private String strInterval;

	private boolean publish;
	private boolean valid = true;
	private boolean hidden;
	private boolean readonly;
	private boolean paused;
	private boolean autoDelete;
	private String md5;
	private ScheduledTaskThread thread;
	private Scheduler scheduler;
	private boolean unique;

	/**
	 * constructor of the class
	 * 
	 * @param task Task name
	 * @param file Output File
	 * @param startDate Start Date
	 * @param startTime Start Time
	 * @param endDate
	 * @param endTime
	 * @param url URL to invoke
	 * @param port Port of the URL to invoke
	 * @param interval interval of the job
	 * @param timeout request timeout in miilisconds
	 * @param credentials username and password for the request
	 * @param proxyHost
	 * @param proxyPort
	 * @param proxyCredentials proxy username and password
	 * @param userAgent
	 * @param resolveURL resolve links in the output page to absolute references or not
	 * @param publish
	 * @throws IOException
	 * @throws ScheduleException
	 */
	public ScheduleTaskImpl(Scheduler scheduler, String task, Resource file, Date startDate, Time startTime, Date endDate, Time endTime, String url, int port, String interval,
			long timeout, Credentials credentials, ProxyData proxy, boolean resolveURL, boolean publish, boolean hidden, boolean readonly, boolean paused, boolean autoDelete,
			boolean unique, String userAgent) throws IOException, ScheduleException {

		this.scheduler = scheduler;
		String md5 = task.toLowerCase() + file + startDate + startTime + endDate + endTime + url + port + interval + timeout + credentials + proxy + resolveURL + publish + hidden
				+ readonly + paused + unique + userAgent;
		md5 = Md5.getDigestAsString(md5);
		this.md5 = md5;

		if (file != null && file.toString().trim().length() > 0) {
			Resource parent = file.getParentResource();
			if (parent == null || !parent.exists()) throw new IOException("Directory for output file [" + file + "] doesn't exist");
			if (file.exists() && !file.isFile()) throw new IOException("output file [" + file + "] is not a file");
		}
		if (timeout < 1) {
			throw new ScheduleException("value timeout must be greater than 0");
		}
		if (startDate == null) throw new ScheduleException("start date is required");
		if (startTime == null) throw new ScheduleException("start time is required");
		// if(endTime==null)endTime=new Time(23,59,59,999);

		this.task = task.trim();
		this.file = file;
		this.startDate = startDate;
		this.startTime = startTime;
		this.endDate = endDate;
		this.endTime = endTime;
		this.url = toURL(url, port);
		this.interval = toInterval(interval);
		this.strInterval = interval;
		this.timeout = timeout;
		this.credentials = credentials;
		this.proxy = proxy;
		this.userAgent = userAgent;
		this.resolveURL = resolveURL;
		this.publish = publish;
		this.hidden = hidden;
		this.readonly = readonly;
		this.paused = paused;
		this.autoDelete = autoDelete;
		this.unique = unique;
	}

	/**
	 * translate a String interval definition to an int definition
	 * 
	 * @param interval
	 * @return interval
	 * @throws ScheduleException
	 */
	private static int toInterval(String interval) throws ScheduleException {
		interval = interval.trim().toLowerCase();
		int i = Caster.toIntValue(interval, 0);
		if (i == 0) {
			interval = interval.trim();
			if (interval.equals("once")) return INTERVAL_ONCE;
			else if (interval.equals("daily")) return INTERVAL_DAY;
			else if (interval.equals("day")) return INTERVAL_DAY;
			else if (interval.equals("monthly")) return INTERVAL_MONTH;
			else if (interval.equals("month")) return INTERVAL_MONTH;
			else if (interval.equals("weekly")) return INTERVAL_WEEK;
			else if (interval.equals("week")) return INTERVAL_WEEK;
			throw new ScheduleException("invalid interval definition [" + interval + "], valid values are [once,daily,monthly,weekly or number]");
		}
		if (i < 10) {
			throw new ScheduleException("interval must be at least 10");
		}
		return i;
	}

	/**
	 * translate a urlString and a port definition to a URL Object
	 * 
	 * @param url URL String
	 * @param port URL Port Definition
	 * @return returns a URL Object
	 * @throws MalformedURLException
	 */
	private static URL toURL(String url, int port) throws MalformedURLException {
		URL u = HTTPUtil.toURL(url, HTTPUtil.ENCODED_AUTO);
		if (port == -1) return u;
		return new URL(u.getProtocol(), u.getHost(), port, u.getFile());
	}

	@Override
	public Credentials getCredentials() {
		return credentials;
	}

	@Override
	public boolean hasCredentials() {
		return credentials != null;
	}

	@Override
	public Resource getResource() {
		return file;
	}

	@Override
	public int getInterval() {
		return interval;
	}

	@Override
	public short getOperation() {
		return operation;
	}

	@Override
	public ProxyData getProxyData() {
		return proxy;
	}

	@Override
	public String getUserAgent() {
		return userAgent;
	}

	@Override
	public boolean isResolveURL() {
		return resolveURL;
	}

	@Override
	public String getTask() {
		return task;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public void setNextExecution(long nextExecution) {
		this.nextExecution = nextExecution;
	}

	@Override
	public long getNextExecution() {
		return nextExecution;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public Time getEndTime() {
		return endTime;
	}

	@Override
	public Time getStartTime() {
		return startTime;
	}

	@Override
	public String getIntervalAsString() {
		return strInterval;
	}

	@Override
	public String getStringInterval() {
		return strInterval;
	}

	@Override
	public boolean isPublish() {
		return publish;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return the hidden
	 */
	@Override
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the readonly
	 */
	public boolean isReadonly() {
		return readonly;
	}

	/**
	 * @param readonly the readonly to set
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public void setUseragent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String md5() {
		return md5;
	}

	public void startIfNecessary(CFMLEngineImpl engine) {
		if (thread != null) {
			if (thread.isAlive()) {
				if (thread.getState() == State.BLOCKED) {
					((SchedulerImpl) scheduler).getConfig().getLog("scheduler").info("scheduler", "thread is blocked");
					SystemUtil.stop(thread);
				}
				else if (thread.getState() != State.TERMINATED) {
					return; // existing is still fine, so nothing to start
				}
			}
			((SchedulerImpl) scheduler).getConfig().getLog("scheduler").info("scheduler", "thread needs a restart (" + thread.getState().name() + ")");

		}
		this.thread = new ScheduledTaskThread(engine, scheduler, this);
		setValid(true);
		thread.start();
	}

	public void stop() {
		Log log = ((SchedulerImpl) scheduler).getConfig().getLog("scheduler");
		log.info("scheduler", "stopping task [" + getTask() + "]");
		if (thread == null || !thread.isAlive()) {
			log.info("scheduler", "task [" + getTask() + "] was not running");
			return;
		}
		thread.stopIt();
	}

	public boolean unique() {
		return unique;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void log(int level, String msg) {
		String logName = "schedule task:" + task;
		((SchedulerImpl) scheduler).getConfig().getLog("scheduler").log(level, logName, msg);
	}

	public void log(int level, String msg, Throwable t) {
		String logName = "schedule task:" + task;
		((SchedulerImpl) scheduler).getConfig().getLog("scheduler").log(level, logName, msg, t);
	}
}
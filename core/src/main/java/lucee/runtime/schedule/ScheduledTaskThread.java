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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;

public class ScheduledTaskThread extends Thread {

	private static final long DAY = 24 * 3600000;
	// private Calendar calendar;

	private long startDate;
	private long startTime;
	private long endDate;
	private long endTime;
	private int intervall;
	private int amount;
	private boolean stop;

	private DateTimeUtil util;

	private int cIntervall;

	private ScheduleTask task;
	private final CFMLEngineImpl engine;
	private TimeZone timeZone;
	private SchedulerImpl scheduler;
	private List<ExecutionThread> exeThreads = new ArrayList<ExecutionThread>();
	private ExecutionThread exeThread;
	private final boolean unique;
	private Config config;

	public ScheduledTaskThread(CFMLEngineImpl engine, Scheduler scheduler, ScheduleTask task) {
		util = DateTimeUtil.getInstance();
		this.engine = engine;
		this.scheduler = (SchedulerImpl) scheduler;
		this.task = task;
		timeZone = ThreadLocalPageContext.getTimeZone(this.scheduler.getConfig());
		this.startDate = util.getMilliSecondsAdMidnight(timeZone, task.getStartDate().getTime());
		this.startTime = util.getMilliSecondsInDay(timeZone, task.getStartTime().getTime());
		this.endDate = task.getEndDate() == null ? Long.MAX_VALUE : util.getMilliSecondsAdMidnight(timeZone, task.getEndDate().getTime());
		this.endTime = task.getEndTime() == null ? DAY : util.getMilliSecondsInDay(timeZone, task.getEndTime().getTime());
		this.unique = ((ScheduleTaskImpl) task).unique();
		this.intervall = task.getInterval();
		if (intervall >= 10) {
			amount = intervall;
			intervall = ScheduleTaskImpl.INTERVAL_EVEREY;
		}
		else amount = 1;

		cIntervall = toCalndarIntervall(intervall);
		this.config = ThreadLocalPageContext.getConfig(this.scheduler.getConfig());
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void stopIt() {
		setStop(true);
		Log log = scheduler.getConfig().getLog("scheduler");
		log.info("scheduler", "stopping task thread [" + task.getTask() + "]");

		if (unique) {
			stop(log, exeThread);
		}
		else {
			Iterator<ExecutionThread> it = exeThreads.iterator();
			while (it.hasNext()) {
				stop(log, it.next());
			}
			cleanThreads();
		}

		// stop this thread itself
		SystemUtil.notify(this);
		SystemUtil.stop(this);
		if (this.isAlive()) log.warn("scheduler", "task [" + task.getTask() + "] could not be stopped:" + ExceptionUtil.toString(this.getStackTrace()));
		else log.info("scheduler", "task [" + task.getTask() + "] stopped");
	}

	private void stop(Log log, ExecutionThread et) {
		SystemUtil.stop(exeThread);
		if (et != null && et.isAlive()) log.warn("scheduler", "task thread [" + task.getTask() + "] could not be stopped:" + ExceptionUtil.toString(et.getStackTrace()));
		else log.info("scheduler", "task thread [" + task.getTask() + "] stopped");
	}

	@Override
	public void run() {
		if (ThreadLocalPageContext.getConfig() == null && config != null) ThreadLocalConfig.register(config);

		try {
			_run();
		}
		catch (Exception e) {
			log(Log.LEVEL_ERROR, e);
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
		finally {
			log(Log.LEVEL_INFO, "ending task");
			task.setValid(false);
			try {
				scheduler.removeIfNoLonerValid(task);
			}
			catch (Exception e) {}
		}

	}

	public void _run() {

		// check values
		if (startDate > endDate) {
			log(Log.LEVEL_ERROR, "Invalid task definition: enddate is before startdate");
			return;
		}
		if (intervall == ScheduleTaskImpl.INTERVAL_EVEREY && startTime > endTime) {
			log(Log.LEVEL_ERROR, "Invalid task definition: endtime is before starttime");
			return;
		}

		long today = System.currentTimeMillis();
		long execution;
		boolean isOnce = intervall == ScheduleTask.INTERVAL_ONCE;
		if (isOnce) {
			if (startDate + startTime < today) {
				log(Log.LEVEL_INFO, "not executing task because single execution was in the past");
				return;
			}
			execution = startDate + startTime;
		}
		else execution = calculateNextExecution(today, false);
		// long sleep=execution-today;

		log(Log.LEVEL_INFO, "First execution");

		while (true) {
			sleepEL(execution, today);
			if (stop) break;
			if (!engine.isRunning()) {
				log(Log.LEVEL_ERROR, "Engine is not running");
				break;
			}

			today = System.currentTimeMillis();
			long todayTime = util.getMilliSecondsInDay(null, today);
			long todayDate = today - todayTime;

			if (!task.isValid()) {
				log(Log.LEVEL_ERROR, "Task is not valid");
				break;
			}
			if (!task.isPaused()) {
				if (endDate < todayDate && endTime < todayTime) {
					log(Log.LEVEL_ERROR, String.format("End date %s has passed; now: %s", DateTimeUtil.format(endDate + endTime, null, timeZone),
							DateTimeUtil.format(todayDate + todayTime, null, timeZone)));
					break;
				}
				execute();
			}
			if (isOnce) {
				log(Log.LEVEL_INFO, "ending task after a single execution");
				break;
			}
			today = System.currentTimeMillis();
			execution = calculateNextExecution(today, true);

			if (!task.isPaused()) log(Log.LEVEL_DEBUG, "next execution runs at " + DateTimeUtil.format(execution, null, timeZone));
			// sleep=execution-today;
		}
	}

	private void log(int level, String msg) {
		try {
			String logName = "schedule task:" + task.getTask();
			((ConfigImpl) scheduler.getConfig()).getLog("scheduler").log(level, logName, msg);

		}
		catch (Exception e) {
			System.err.println(msg);
			System.err.println(e);
		}
	}

	private void log(int level, Exception e) {
		try {
			String logName = "schedule task:" + task.getTask();
			((ConfigImpl) scheduler.getConfig()).getLog("scheduler").log(level, logName, e);

		}
		catch (Exception ee) {
			LogUtil.logGlobal(config, "scheduler", ee);
		}
	}

	private void sleepEL(long when, long now) {
		long millis = when - now;

		try {
			while (true) {
				SystemUtil.wait(this, millis);
				millis = when - System.currentTimeMillis();
				if (millis <= 0) break;
				millis = 10;
			}
		}
		catch (Exception e) {
			log(Log.LEVEL_ERROR, e);
		}

	}

	private void execute() {
		if (scheduler.getConfig() != null) {
			// unique
			if (unique && exeThread != null && exeThread.isAlive()) {
				return;
			}

			ExecutionThread et = new ExecutionThread(scheduler.getConfig(), task, scheduler.getCharset());
			et.start();
			if (unique) {
				exeThread = et;
			}
			else {
				cleanThreads();
				exeThreads.add(et);
			}
		}
	}

	private void cleanThreads() {
		List<ExecutionThread> list = new ArrayList<ExecutionThread>();
		Iterator<ExecutionThread> it = exeThreads.iterator();
		ExecutionThread et;
		while (it.hasNext()) {
			et = it.next();
			if (et.isAlive()) list.add(et);
		}
		exeThreads = list;
	}

	private long calculateNextExecution(long now, boolean notNow) {
		long nowTime = util.getMilliSecondsInDay(timeZone, now);
		long nowDate = now - nowTime;

		// when second or date intervall switch to current date
		if (startDate < nowDate && (cIntervall == Calendar.SECOND || cIntervall == Calendar.DATE)) startDate = nowDate;

		// init calendar
		Calendar calendar = JREDateTimeUtil.getThreadCalendar(timeZone);
		calendar.setTimeInMillis(startDate + startTime);

		long time;
		while (true) {
			time = getMilliSecondsInDay(calendar);
			if (now <= calendar.getTimeInMillis() && time >= startTime) {
				// this is used because when cames back sometme to early
				if (notNow && (calendar.getTimeInMillis() - now) < 1000) {}
				else if (intervall == ScheduleTaskImpl.INTERVAL_EVEREY && time > endTime) now = nowDate + DAY;
				else break;
			}
			calendar.add(cIntervall, amount);
		}
		return calendar.getTimeInMillis();
	}

	private static int toCalndarIntervall(int intervall) {
		switch (intervall) {
		case ScheduleTask.INTERVAL_DAY:
			return Calendar.DATE;
		case ScheduleTask.INTERVAL_MONTH:
			return Calendar.MONTH;
		case ScheduleTask.INTERVAL_WEEK:
			return Calendar.WEEK_OF_YEAR;
		case ScheduleTask.INTERVAL_ONCE:
			return -1;

		}
		return Calendar.SECOND;
	}

	public static long getMilliSecondsInDay(Calendar c) {
		return (c.get(Calendar.HOUR_OF_DAY) * 3600000) + (c.get(Calendar.MINUTE) * 60000) + (c.get(Calendar.SECOND) * 1000) + (c.get(Calendar.MILLISECOND));

	}

	public Config getConfig() {
		return scheduler.getConfig();
	}

	public ScheduleTask getTask() {
		return task;
	}
}

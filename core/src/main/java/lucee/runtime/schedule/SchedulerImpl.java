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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.system.ExtensionExists;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

/**
 * scheduler class to execute the scheduled tasks
 */
public final class SchedulerImpl implements Scheduler {

	private Queue<TaskRef> tasks;
	private Resource schedulerFile;
	private StorageUtil su = new StorageUtil();
	private String charset;
	private final Config config;
	private final Object sync = new SerializableObject();
	// private String md5;

	private CFMLEngineImpl engine;

	/**
	 * constructor of the sheduler
	 * 
	 * @param engine
	 * @param config
	 * @param tasks
	 * @throws PageException
	 */
	public SchedulerImpl(CFMLEngine engine, Config config, Array tasks) throws PageException {
		this.engine = (CFMLEngineImpl) engine;
		this.config = config;
		this.tasks = readInAllTasks(tasks);
		init(config);
	}

	/**
	 * creates an empty Scheduler, used for event gateway context
	 * 
	 * @param engine
	 * @param xml
	 * @param config
	 */
	public SchedulerImpl(CFMLEngine engine, String xml, Config config) {
		this.engine = (CFMLEngineImpl) engine;
		this.config = config;
		tasks = new ConcurrentLinkedQueue<>();
		init(config);
	}

	/**
	 * initialize all tasks
	 */
	private void init(Config config) {
		// in case the Scheduler extension is not installed we do not start tasks
		if (!ExtensionExists.has(config, "97EB5427-F051-4684-91EBA6DBB5C5203F")) return;

		for (TaskRef ref: tasks) {
			init(ref.task);
		}
	}

	public void startIfNecessary() {
		for (TaskRef ref: tasks) {
			init(ref.task);
		}
	}

	private void init(ScheduleTaskImpl task) {
		task.startIfNecessary(engine);
	}

	public void stop() {
		for (TaskRef ref: tasks) {
			ref.task.stop();
		}
	}

	public void refresh(Array tasks) throws PageException {
		synchronized (sync) {
			Queue<TaskRef> newTasks = readInAllTasks(tasks);

			// changed,new
			for (TaskRef ref: newTasks) {
				addTask(ref.task);
			}

			// delete
			for (TaskRef ref: this.tasks) {
				if (!hasScheduleTask(newTasks, ref.task.getTask())) {
					removeTaskOnly(ref.task.getTask());
				}
			}
		}
	}

	private void removeTaskOnly(String name) {
		tasks.removeIf(ref -> {
			if (ref.task.getTask().equalsIgnoreCase(name)) {
				ref.task.log(Log.LEVEL_INFO, "task gets removed");
				ref.task.setValid(false);
				return true;
			}
			return false;
		});
	}

	/**
	 * read in all schedule tasks
	 * 
	 * @return
	 * 
	 * @return all schedule tasks
	 * @throws PageException
	 */
	private Queue<TaskRef> readInAllTasks(Array tasks) throws PageException {
		Queue<TaskRef> queue = new ConcurrentLinkedQueue<>();
		Iterator<?> it = tasks.getIterator();
		while (it.hasNext()) {
			queue.add(new TaskRef(readInTask((Struct) it.next())));
		}
		return queue;
	}

	private static List<String> readTaskNames(Array tasks) {
		List<String> list = new ArrayList<>();
		Iterator<?> it = tasks.getIterator();
		Struct sct;
		String name;
		while (it.hasNext()) {
			sct = (Struct) it.next();
			name = Caster.toString(sct.get(KeyConstants._name, null), null);
			if (!StringUtil.isEmpty(name, true)) list.add(name.trim());
		}
		return list;
	}

	/**
	 * read in a single task element
	 * 
	 * @param el
	 * @return matching task to Element
	 * @throws PageException
	 */
	private ScheduleTaskImpl readInTask(Struct el) throws PageException {
		long timeout = su.toLong(el, "timeout");
		if (timeout > 0 && timeout < 1000) timeout *= 1000;
		if (timeout < 0) timeout = 600000;
		try {
			ScheduleTaskImpl st = new ScheduleTaskImpl(this, su.toString(el, "name").trim(), su.toResource(config, el, "file"), su.toDate(config, el, "startDate"),
					su.toTime(config, el, "startTime"), su.toDate(config, el, "endDate"), su.toTime(config, el, "endTime"), su.toString(el, "url"), su.toInt(el, "port", -1),
					su.toString(el, "interval"), timeout, su.toCredentials(el, "username", "password"),
					ProxyDataImpl.getInstance(su.toString(el, "proxyHost"), su.toInt(el, "proxyPort", 80), su.toString(el, "proxyUser"), su.toString(el, "proxyPassword")),
					su.toBoolean(el, "resolveUrl"), su.toBoolean(el, "publish"), su.toBoolean(el, "hidden", false), su.toBoolean(el, "readonly", false),
					su.toBoolean(el, "paused", false), su.toBoolean(el, "autoDelete", false), su.toBoolean(el, "unique", false), su.toString(el, "userAgent").trim());
			return st;
		}
		catch (Exception e) {
			LogUtil.log(ThreadLocalPageContext.getConfigServer(config), SchedulerImpl.class.getName(), e);
			throw Caster.toPageException(e);
		}
	}

	private void addTask(ScheduleTaskImpl task) {
		for (TaskRef ref: tasks) {
			if (!ref.task.getTask().equals(task.getTask())) continue;
			if (!ref.task.md5().equals(task.md5())) {
				ref.task.log(Log.LEVEL_INFO, "invalidate task because the task is replaced with a new one");
				ref.task.setValid(false);
				ref.task = task;
				init(task);
			}
			return;
		}
		tasks.add(new TaskRef(task));
		init(task);
	}

	@Override
	public ScheduleTask getScheduleTask(String name) throws ScheduleException {
		for (TaskRef ref: tasks) {
			if (ref.task.getTask().equalsIgnoreCase(name)) return ref.task;
		}
		throw new ScheduleException("schedule task with name " + name + " doesn't exist");
	}

	@Override
	public ScheduleTask getScheduleTask(String name, ScheduleTask defaultValue) {
		for (TaskRef ref: tasks) {
			if (ref.task.getTask().equalsIgnoreCase(name)) return ref.task;
		}
		return defaultValue;
	}

	@Override
	public ScheduleTask[] getAllScheduleTasks() {
		ArrayList<ScheduleTask> list = new ArrayList<ScheduleTask>();
		for (TaskRef ref: tasks) {
			if (!ref.task.isHidden()) list.add(ref.task);
		}
		return list.toArray(new ScheduleTask[list.size()]);
	}

	@Override
	public void addScheduleTask(ScheduleTask task, boolean allowOverwrite) throws ScheduleException, IOException {
		synchronized (sync) {
			try {
				ConfigAdmin.updateScheduledTask((ConfigPro) config, task, true);
				addTask((ScheduleTaskImpl) task);
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
	}

	@Override
	public void pauseScheduleTask(String name, boolean pause, boolean throwWhenNotExist) throws ScheduleException, IOException {
		synchronized (sync) {
			try {
				ConfigAdmin.pauseScheduledTask((ConfigPro) config, name, pause, throwWhenNotExist, true);
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}

			for (TaskRef ref: tasks) {
				if (ref.task.getTask().equalsIgnoreCase(name)) {
					ref.task.setPaused(pause);
				}
			}
		}
	}

	public boolean hasScheduleTask(String name) {
		return hasScheduleTask(tasks, name);
	}

	public static boolean hasScheduleTask(Queue<TaskRef> tasks, String name) {
		for (TaskRef ref: tasks) {
			if (ref.task.getTask().equals(name)) return true;
		}
		return false;
	}

	@Override
	public void removeScheduleTask(String name, boolean throwWhenNotExist) throws IOException, ScheduleException {
		synchronized (sync) {

			tasks.removeIf(ref -> {
				if (ref.task.getTask().equalsIgnoreCase(name)) {
					ref.task.log(Log.LEVEL_INFO, "task gets removed");
					ref.task.setValid(false);
					return true;
				}
				return false;
			});

			try {
				ConfigAdmin.removeScheduledTask((ConfigPro) config, name, true);
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
	}

	public void removeIfNoLonerValid(ScheduleTask task) throws IOException {
		ScheduleTaskImpl sti = (ScheduleTaskImpl) task;
		if (sti.isValid() || !sti.isAutoDelete()) return;

		synchronized (sync) {
			sti = (ScheduleTaskImpl) task;
			if (sti.isValid() || !sti.isAutoDelete()) return;

			try {
				removeScheduleTask(task.getTask(), false);
			}
			catch (ScheduleException e) {}
		}
	}

	@Override
	public void runScheduleTask(String name, boolean throwWhenNotExist) throws IOException, ScheduleException {
		ScheduleTask task = getScheduleTask(name);
		if (task != null) {
			synchronized (sync) {
				task = getScheduleTask(name);
				if (task != null) {
					if (active()) execute(task);
				}
				else if (throwWhenNotExist) throw new ScheduleException("can't run schedule task [" + name + "], task doesn't exist");
			}
		}
	}

	public void execute(ScheduleTask task) {
		new ExecutionThread(config, task, charset).start();
	}

	public Config getConfig() {
		return config;
	}

	public String getCharset() {
		return charset;
	}

	public boolean active() {
		return engine == null || engine.active();
	}

	private static class TaskRef {
		private ScheduleTaskImpl task;

		public TaskRef(ScheduleTaskImpl task) {
			this.task = task;
		}
	}
}
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
package lucee.runtime.spooler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;

public class SpoolerEngineImpl implements SpoolerEngine {

	private static final TaskFileFilter FILTER = new TaskFileFilter();

	private static final Collection.Key LAST_EXECUTION = KeyImpl.intern("lastExecution");
	private static final Collection.Key NEXT_EXECUTION = KeyImpl.intern("nextExecution");

	private static final Collection.Key CLOSED = KeyImpl.intern("closed");
	private static final Collection.Key TRIES = KeyImpl.intern("tries");
	private static final Collection.Key TRIES_MAX = KeyImpl.intern("triesmax");

	private String label;

	// private LinkedList<SpoolerTask> openTaskss=new LinkedList<SpoolerTask>();
	// private LinkedList<SpoolerTask> closedTasks=new LinkedList<SpoolerTask>();
	private SimpleThread simpleThread;
	private final SerializableObject token = new SerializableObject();
	private SpoolerThread thread;
	// private ExecutionPlan[] plans;
	private Resource _persisDirectory;
	private long count = 0;
	private Log log;
	private Config config;
	private int add = 0;

	private Resource closedDirectory;
	private Resource openDirectory;

	private int maxThreads;

	public SpoolerEngineImpl(Config config, Resource persisDirectory, String label, Log log, int maxThreads) {
		this.config = config;
		this._persisDirectory = persisDirectory;

		closedDirectory = persisDirectory.getRealResource("closed");
		openDirectory = persisDirectory.getRealResource("open");
		// calculateSize();

		this.maxThreads = maxThreads;
		this.label = label;
		this.log = log;
		// print.ds(persisDirectory.getAbsolutePath());
		// load();
		if (getOpenTaskCount() > 0) start();
	}

	/*
	 * private void calculateSize() { closedCount=calculateSize(closedDirectory);
	 * openCount=calculateSize(openDirectory); }
	 */

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * @return the maxThreads
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	private int calculateSize(Resource res) {
		return ResourceUtil.directrySize(res, FILTER);
	}

	@Override
	public synchronized void add(SpoolerTask task) {
		// if there is no plan execute and forget
		if (task.getPlans() == null) {
			if (task instanceof Task) start((Task) task);
			else {
				start(new TaskWrap(task));
				log.error("spooler", "make class " + task.getClass().getName() + " a Task class");
			}
			return;
		}

		// openTasks.add(task);
		add++;
		if (task.nextExecution() == 0) task.setNextExecution(System.currentTimeMillis());
		task.setId(createId(task));
		store(task);
		start();
	}

	// add to interface
	public void add(Task task) {
		start(task);
	}

	private void start(Task task) {
		if (task == null) return;
		synchronized (task) {
			if (simpleThread == null || !simpleThread.isAlive()) {
				simpleThread = new SimpleThread(config, task);

				simpleThread.setPriority(Thread.MIN_PRIORITY);
				simpleThread.start();
			}
			else {
				simpleThread.tasks.add(task);
				simpleThread.interrupt();
			}
		}
	}

	private void start() {
		if (thread == null || !thread.isAlive()) {
			thread = new SpoolerThread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
		else if (thread.sleeping) {
			thread.interrupt();
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	private SpoolerTask getTaskById(Resource dir, String id) {
		return getTask(dir.getRealResource(id + ".tsk"), null);
	}

	private SpoolerTask getTaskByName(Resource dir, String name) {
		return getTask(dir.getRealResource(name), null);
	}

	private SpoolerTask getTask(Resource res, SpoolerTask defaultValue) {
		InputStream is = null;
		ObjectInputStream ois = null;

		SpoolerTask task = defaultValue;
		try {
			is = res.getInputStream();
			ois = new ObjectInputStream(is);
			task = (SpoolerTask) ois.readObject();
		}
		catch (Exception e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(), SpoolerEngineImpl.class.getName(), e);
			IOUtil.closeEL(is);
			IOUtil.closeEL(ois);
			res.delete();
		}
		IOUtil.closeEL(is);
		IOUtil.closeEL(ois);
		return task;
	}

	private void store(SpoolerTask task) {
		ObjectOutputStream oos = null;
		Resource persis = getFile(task);
		if (persis.exists()) persis.delete();
		try {
			oos = new ObjectOutputStream(persis.getOutputStream());
			oos.writeObject(task);
		}
		catch (IOException e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(), SpoolerEngineImpl.class.getName(), e);
		}
		finally {
			IOUtil.closeEL(oos);
		}
	}

	private void unstore(SpoolerTask task) {
		Resource persis = getFile(task);
		boolean exists = persis.exists();
		if (exists) persis.delete();
	}

	private void log(SpoolerTask task, Exception e, boolean before) {
		if (task instanceof SpoolerTaskPro) {
			SpoolerTaskPro taskp = (SpoolerTaskPro) task;
			SpoolerTaskListener listener = taskp.getListener();
			if (listener != null) listener.listen(config, e, before);
		}
		if (e == null) log.log(Log.LEVEL_INFO, "remote-client", "successfully executed: " + task.subject());
		else log.log(Log.LEVEL_ERROR, "remote-client", "failed to execute: " + task.subject(), e);
	}

	private Resource getFile(SpoolerTask task) {
		Resource dir = getPersisDirectory().getRealResource(task.closed() ? "closed" : "open");
		dir.mkdirs();
		return dir.getRealResource(task.getId() + ".tsk");
	}

	private String createId(SpoolerTask task) {
		Resource dir = getPersisDirectory().getRealResource(task.closed() ? "closed" : "open");
		dir.mkdirs();

		String id = null;
		do {
			id = StringUtil.addZeros(++count, 8);
		}
		while (dir.getRealResource(id + ".tsk").exists());
		return id;
	}

	public long calculateNextExecution(SpoolerTask task) {
		int _tries = 0;
		ExecutionPlan plan = null;
		ExecutionPlan[] plans = task.getPlans();

		for (int i = 0; i < plans.length; i++) {
			_tries += plans[i].getTries();
			if (_tries > task.tries()) {
				plan = plans[i];
				break;
			}
		}
		if (plan == null) return -1;
		return task.lastExecution() + (plan.getIntervall() * 1000);
	}

	@Override
	public Query getOpenTasksAsQuery(int startrow, int maxrow) throws PageException {
		return getTasksAsQuery(createQuery(), openDirectory, startrow, maxrow);
	}

	@Override
	public Query getClosedTasksAsQuery(int startrow, int maxrow) throws PageException {
		return getTasksAsQuery(createQuery(), closedDirectory, startrow, maxrow);
	}

	@Override
	public Query getAllTasksAsQuery(int startrow, int maxrow) throws PageException {
		if (maxrow < 0) maxrow = Integer.MAX_VALUE;

		Query query = createQuery();
		// print.o(startrow+":"+maxrow);
		getTasksAsQuery(query, openDirectory, startrow, maxrow);
		int records = query.getRecordcount();
		// no open tasks
		if (records == 0) {
			startrow -= getOpenTaskCount();
			if (startrow < 1) startrow = 1;
		}
		else {
			startrow = 1;
			maxrow -= records;
		}
		if (maxrow > 0) getTasksAsQuery(query, closedDirectory, startrow, maxrow);
		return query;
	}

	@Override
	public int getOpenTaskCount() {
		return calculateSize(openDirectory);
	}

	@Override
	public int getClosedTaskCount() {
		return calculateSize(closedDirectory);
	}

	private Query getTasksAsQuery(Query qry, Resource dir, int startrow, int maxrow) {
		String[] children = dir.list(FILTER);
		if (ArrayUtil.isEmpty(children)) return qry;
		if (children.length < maxrow) maxrow = children.length;
		SpoolerTask task;

		int to = startrow + maxrow;
		if (to > children.length) to = children.length;
		if (startrow < 1) startrow = 1;

		for (int i = startrow - 1; i < to; i++) {
			task = getTaskByName(dir, children[i]);
			if (task != null) addQueryRow(qry, task);
		}

		return qry;
	}

	private Query createQuery() throws DatabaseException {
		String v = "VARCHAR";
		String d = "DATE";
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "type", "name", "detail", "id", "lastExecution", "nextExecution", "closed", "tries", "exceptions", "triesmax" },
				new String[] { v, v, "object", v, d, d, "boolean", "int", "object", "int" }, 0, "query");
		return qry;
	}

	private void addQueryRow(lucee.runtime.type.Query qry, SpoolerTask task) {
		int row = qry.addRow();
		try {
			qry.setAt(KeyConstants._type, row, task.getType());
			qry.setAt(KeyConstants._name, row, task.subject());
			qry.setAt(KeyConstants._detail, row, task.detail());
			qry.setAt(KeyConstants._id, row, task.getId());

			qry.setAt(LAST_EXECUTION, row, new DateTimeImpl(task.lastExecution(), true));
			qry.setAt(NEXT_EXECUTION, row, new DateTimeImpl(task.nextExecution(), true));
			qry.setAt(CLOSED, row, Caster.toBoolean(task.closed()));
			qry.setAt(TRIES, row, Caster.toDouble(task.tries()));
			qry.setAt(TRIES_MAX, row, Caster.toDouble(task.tries()));
			qry.setAt(KeyConstants._exceptions, row, translateTime(task.getExceptions()));

			int triesMax = 0;
			ExecutionPlan[] plans = task.getPlans();
			for (int y = 0; y < plans.length; y++) {
				triesMax += plans[y].getTries();
			}
			qry.setAt(TRIES_MAX, row, Caster.toDouble(triesMax));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private Array translateTime(Array exp) {
		exp = (Array) Duplicator.duplicate(exp, true);
		Iterator<Object> it = exp.valueIterator();
		Struct sct;
		while (it.hasNext()) {
			sct = (Struct) it.next();
			sct.setEL(KeyConstants._time, new DateTimeImpl(Caster.toLongValue(sct.get(KeyConstants._time, null), 0), true));
		}
		return exp;
	}

	class SimpleThread extends Thread {

		// 'tasks' needs to be synchronized because the other thread will access this list.
		// otherwise tasks.size() will not match the actual size of the server and NPEs
		// and unlimited loops may result.
		List<Task> tasks = Collections.synchronizedList(new LinkedList<Task>());

		private Config config;

		public SimpleThread(Config config, Task task) {
			this.config = config;
			tasks.add(task);
		}

		@Override
		public void run() {
			Task task;
			while (tasks.size() > 0) {
				try {
					task = CollectionUtil.remove(tasks, 0, null);
					if (task != null) task.execute(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}

	}

	class SpoolerThread extends Thread {

		private SpoolerEngineImpl engine;
		private boolean sleeping;
		private final int maxThreads;

		public SpoolerThread(SpoolerEngineImpl engine) {
			this.maxThreads = engine.getMaxThreads();
			this.engine = engine;
			try {
				this.setPriority(MIN_PRIORITY);
			}
			// can throw security exceptions
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		@Override
		public void run() {
			String[] taskNames;
			// SpoolerTask[] tasks;
			SpoolerTask task = null;
			long nextExection;
			ThreadLocalConfig.register(engine.config);
			// ThreadLocalPageContext.register(engine.);
			List<TaskThread> runningTasks = new ArrayList<TaskThread>();
			TaskThread tt;
			int adds;

			while (getOpenTaskCount() > 0) {
				adds = engine.adds();
				taskNames = openDirectory.list(FILTER);
				// tasks=engine.getOpenTasks();
				nextExection = Long.MAX_VALUE;
				for (int i = 0; i < taskNames.length; i++) {
					task = getTaskByName(openDirectory, taskNames[i]);
					if (task == null) continue;

					if (task.nextExecution() <= System.currentTimeMillis()) {
						tt = new TaskThread(engine, task);
						tt.start();
						runningTasks.add(tt);
					}
					else if (task.nextExecution() < nextExection && nextExection != -1 && !task.closed()) nextExection = task.nextExecution();
					nextExection = joinTasks(runningTasks, maxThreads, nextExection);
				}

				nextExection = joinTasks(runningTasks, 0, nextExection);
				if (adds != engine.adds()) continue;

				if (nextExection == Long.MAX_VALUE) break;
				long sleep = nextExection - System.currentTimeMillis();

				// print.o("sleep:"+sleep+">"+(sleep/1000));
				if (sleep > 0) doWait(sleep);

				// if(sleep<0)break;
			}
			// print.o("end:"+getOpenTaskCount());
		}

		private long joinTasks(List<TaskThread> runningTasks, int maxThreads, long nextExection) {
			if (runningTasks.size() >= maxThreads) {
				Iterator<TaskThread> it = runningTasks.iterator();
				TaskThread tt;
				SpoolerTask task;
				while (it.hasNext()) {
					tt = it.next();
					SystemUtil.join(tt);
					task = tt.getTask();

					if (task != null && task.nextExecution() != -1 && task.nextExecution() < nextExection && !task.closed()) {
						nextExection = task.nextExecution();
					}
				}
				runningTasks.clear();
			}
			return nextExection;
		}

		private void doWait(long sleep) {
			try {
				sleeping = true;
				synchronized (this) {
					wait(sleep);
				}

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			finally {
				sleeping = false;
			}
		}

	}

	class TaskThread extends Thread {

		private SpoolerEngineImpl engine;
		private SpoolerTask task;

		public TaskThread(SpoolerEngineImpl engine, SpoolerTask task) {
			this.engine = engine;
			this.task = task;
		}

		public SpoolerTask getTask() {
			return task;
		}

		@Override
		public void run() {
			ThreadLocalConfig.register(engine.config);
			engine.execute(task);
			ThreadLocalConfig.release();

		}
	}

	/**
	 * remove that task from Spooler
	 * 
	 * @param task
	 */
	@Override
	public void remove(SpoolerTask task) {
		unstore(task);
		// if(!openTasks.remove(task))closedTasks.remove(task);
	}

	public void removeAll() {
		ResourceUtil.removeChildrenEL(openDirectory);
		ResourceUtil.removeChildrenEL(closedDirectory);
		SystemUtil.wait(this, 100);
		ResourceUtil.removeChildrenEL(openDirectory);
		ResourceUtil.removeChildrenEL(closedDirectory);
	}

	public int adds() {
		// return openTasks.size()>0;
		return add;
	}

	@Override
	public void remove(String id) {
		SpoolerTask task = getTaskById(openDirectory, id);
		if (task == null) task = getTaskById(closedDirectory, id);
		if (task != null) remove(task);
	}

	/*
	 * private SpoolerTask getTaskById(SpoolerTask[] tasks, String id) { for(int i=0;i<tasks.length;i++)
	 * { if(tasks[i].getId().equals(id)) { return tasks[i]; } } return null; }
	 */

	/**
	 * execute task by id and return eror throwd by task
	 * 
	 * @param id
	 */
	@Override
	public PageException execute(String id) {
		SpoolerTask task = getTaskById(openDirectory, id);
		if (task == null) task = getTaskById(closedDirectory, id);
		if (task != null) {
			return execute(task);
		}
		return null;
	}

	@Override
	public PageException execute(SpoolerTask task) {
		try {
			log(task, null, true);
			if (task instanceof SpoolerTaskSupport) // FUTURE this is bullshit, call the execute method directly, but you have to rewrite them for that
				((SpoolerTaskSupport) task)._execute(config);
			else task.execute(config);
			unstore(task);

			task.setLastExecution(System.currentTimeMillis());
			task.setNextExecution(-1);
			task.setClosed(true);
			log(task, null, false);
			task = null;
		}
		catch (Exception e) {
			task.setLastExecution(System.currentTimeMillis());
			task.setNextExecution(calculateNextExecution(task));

			if (task.nextExecution() == -1) {
				unstore(task);
				task.setClosed(true);
				log(task, e, false);
				store(task);
				task = null;
			}
			else {
				log(task, e, false);
				store(task);
			}

			return Caster.toPageException(e);
		}
		return null;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setPersisDirectory(Resource persisDirectory) {
		this._persisDirectory = persisDirectory;
	}

	public Resource getPersisDirectory() {
		if (_persisDirectory == null) {
			_persisDirectory = config.getRemoteClientDirectory();
		}
		return _persisDirectory;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

}

class TaskFileFilter implements ResourceNameFilter {

	@Override
	public boolean accept(Resource parent, String name) {
		return name != null && name.endsWith(".tsk");
	}

}

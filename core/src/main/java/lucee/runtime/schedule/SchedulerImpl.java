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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;

/**
 * scheduler class to execute the scheduled tasks
 */
public final class SchedulerImpl implements Scheduler {

	private ScheduleTaskImpl[] tasks;
	private Resource schedulerFile;
	private Document doc;
	private StorageUtil su = new StorageUtil();
	private String charset;
	private final Config config;
	private final Object sync = new SerializableObject();
	// private String md5;

	private CFMLEngineImpl engine;

	/**
	 * constructor of the sheduler
	 * 
	 * @param config
	 * @param schedulerDir schedule file
	 * @param log
	 * @throws IOException
	 * @throws SAXException
	 * @throws PageException
	 */
	public SchedulerImpl(CFMLEngine engine, Config config, Resource schedulerDir, String charset) throws SAXException, IOException, PageException {
		this.engine = (CFMLEngineImpl) engine;
		this.charset = charset;
		this.config = config;

		boolean newFile = initFile(schedulerDir);
		try {
			doc = XMLUtil.createDocument(schedulerFile, false);
		}
		catch (Exception e) {
			if (newFile) rethrow(e);
			config.getLog("scheduler").log(Log.LEVEL_FATAL, "startup", "could not load " + schedulerFile, e);
			reinitFile(schedulerDir);
			doc = XMLUtil.createDocument(schedulerFile, false);
		}

		tasks = readInAllTasks();
		init();
	}

	private void rethrow(Exception e) throws IOException, SAXException {
		if (e instanceof IOException) throw (IOException) e;
		if (e instanceof SAXException) throw (SAXException) e;
	}

	/**
	 * creates an empty Scheduler, used for event gateway context
	 * 
	 * @param engine
	 * @param config
	 * @param log
	 * @throws SAXException
	 * @throws IOException
	 * @throws PageException
	 */
	public SchedulerImpl(CFMLEngine engine, String xml, Config config) {
		this.engine = (CFMLEngineImpl) engine;
		this.config = config;
		try {
			doc = XMLUtil.createDocument(xml, false);
		}
		catch (Exception e) {}
		tasks = new ScheduleTaskImpl[0];
		init();
	}

	private boolean reinitFile(Resource schedulerDir) throws IOException {
		Resource src = schedulerDir.getRealResource("scheduler.xml");
		if (src.exists()) {
			Resource trg = schedulerDir.getRealResource("scheduler.xml.buggy");
			if (trg.exists()) trg.delete();
			src.moveTo(trg);
		}
		return initFile(schedulerDir);
	}

	private boolean initFile(Resource schedulerDir) throws IOException {
		this.schedulerFile = schedulerDir.getRealResource("scheduler.xml");
		if (!schedulerFile.exists()) {
			su.loadFile(schedulerFile, "/resource/schedule/default.xml");
			return true;
		}
		return false;
	}

	/**
	 * initialize all tasks
	 */
	private void init() {
		for (int i = 0; i < tasks.length; i++) {
			init(tasks[i]);
		}
	}

	public void startIfNecessary() {
		for (int i = 0; i < tasks.length; i++) {
			init(tasks[i]);
		}
	}

	private void init(ScheduleTask task) {
		((ScheduleTaskImpl) task).startIfNecessary(engine);
	}

	public void stop() {
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].stop();
		}
	}

	/**
	 * read in all schedule tasks
	 * 
	 * @return all schedule tasks
	 * @throws PageException
	 */
	private ScheduleTaskImpl[] readInAllTasks() throws PageException {
		Element root = doc.getDocumentElement();
		NodeList children = root.getChildNodes();
		ArrayList<ScheduleTaskImpl> list = new ArrayList<ScheduleTaskImpl>();

		int len = children.getLength();
		for (int i = 0; i < len; i++) {
			Node n = children.item(i);
			if (n instanceof Element && n.getNodeName().equals("task")) {
				list.add(readInTask((Element) n));
			}
		}
		return list.toArray(new ScheduleTaskImpl[list.size()]);
	}

	/**
	 * read in a single task element
	 * 
	 * @param el
	 * @return matching task to Element
	 * @throws PageException
	 */
	private ScheduleTaskImpl readInTask(Element el) throws PageException {
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
			LogUtil.log(ThreadLocalPageContext.getConfig(config), SchedulerImpl.class.getName(), e);
			throw Caster.toPageException(e);
		}
	}

	private void addTask(ScheduleTaskImpl task) {
		for (int i = 0; i < tasks.length; i++) {
			if (!tasks[i].getTask().equals(task.getTask())) continue;
			if (!tasks[i].md5().equals(task.md5())) {
				tasks[i].log(Log.LEVEL_INFO, "invalidate task because the task is replaced with a new one");
				tasks[i].setValid(false);
				tasks[i] = task;
				init(task);
			}
			return;
		}

		ScheduleTaskImpl[] tmp = new ScheduleTaskImpl[tasks.length + 1];
		for (int i = 0; i < tasks.length; i++) {
			tmp[i] = tasks[i];
		}
		tmp[tasks.length] = task;
		tasks = tmp;
		init(task);
	}

	/**
	 * sets all attributes in XML Element from Schedule Task
	 * 
	 * @param el
	 * @param task
	 */
	private void setAttributes(Element el, ScheduleTask task) {
		if (el == null) return;
		NamedNodeMap atts = el.getAttributes();

		for (int i = atts.getLength() - 1; i >= 0; i--) {
			Attr att = (Attr) atts.item(i);
			el.removeAttribute(att.getName());
		}

		su.setString(el, "name", task.getTask());
		su.setFile(el, "file", task.getResource());
		su.setDateTime(el, "startDate", task.getStartDate());
		su.setDateTime(el, "startTime", task.getStartTime());
		su.setDateTime(el, "endDate", task.getEndDate());
		su.setDateTime(el, "endTime", task.getEndTime());
		su.setString(el, "url", task.getUrl().toExternalForm());
		su.setInt(el, "port", task.getUrl().getPort());
		su.setString(el, "interval", task.getIntervalAsString());
		su.setInt(el, "timeout", (int) task.getTimeout());
		su.setCredentials(el, "username", "password", task.getCredentials());
		ProxyData pd = task.getProxyData();
		su.setString(el, "proxyHost", StringUtil.emptyIfNull(pd == null ? "" : pd.getServer()));
		su.setString(el, "proxyUser", StringUtil.emptyIfNull(pd == null ? "" : pd.getUsername()));
		su.setString(el, "proxyPassword", StringUtil.emptyIfNull(pd == null ? "" : pd.getPassword()));
		su.setInt(el, "proxyPort", pd == null ? 0 : pd.getPort());
		su.setBoolean(el, "resolveUrl", task.isResolveURL());
		su.setBoolean(el, "publish", task.isPublish());
		su.setBoolean(el, "hidden", ((ScheduleTaskImpl) task).isHidden());
		su.setBoolean(el, "readonly", ((ScheduleTaskImpl) task).isReadonly());
		su.setBoolean(el, "autoDelete", ((ScheduleTaskImpl) task).isAutoDelete());
		su.setBoolean(el, "unique", ((ScheduleTaskImpl) task).unique());
	}

	/**
	 * translate a schedule task object to a XML Element
	 * 
	 * @param task schedule task to translate
	 * @return XML Element
	 */
	private Element toElement(ScheduleTask task) {
		Element el = doc.createElement("task");
		setAttributes(el, task);
		return el;
	}

	@Override
	public ScheduleTask getScheduleTask(String name) throws ScheduleException {
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i].getTask().equalsIgnoreCase(name)) return tasks[i];
		}
		throw new ScheduleException("schedule task with name " + name + " doesn't exist");
	}

	@Override
	public ScheduleTask getScheduleTask(String name, ScheduleTask defaultValue) {
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i] != null && tasks[i].getTask().equalsIgnoreCase(name)) return tasks[i];
		}
		return defaultValue;
	}

	@Override
	public ScheduleTask[] getAllScheduleTasks() {
		ArrayList<ScheduleTask> list = new ArrayList<ScheduleTask>();
		for (int i = 0; i < tasks.length; i++) {
			if (!tasks[i].isHidden()) list.add(tasks[i]);
		}
		return list.toArray(new ScheduleTask[list.size()]);
	}

	@Override
	public void addScheduleTask(ScheduleTask task, boolean allowOverwrite) throws ScheduleException, IOException {
		// ScheduleTask exTask = getScheduleTask(task.getTask(),null);
		NodeList list = doc.getDocumentElement().getChildNodes();
		Element el = su.getElement(list, "name", task.getTask());

		if (!allowOverwrite && el != null) throw new ScheduleException("there is already a schedule task with name " + task.getTask());

		addTask((ScheduleTaskImpl) task);

		// Element update
		if (el != null) {
			setAttributes(el, task);
		}
		// Element insert
		else {
			doc.getDocumentElement().appendChild(toElement(task));
		}

		su.store(doc, schedulerFile);
	}

	@Override
	public void pauseScheduleTask(String name, boolean pause, boolean throwWhenNotExist) throws ScheduleException, IOException {

		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i].getTask().equalsIgnoreCase(name)) {
				tasks[i].setPaused(pause);

			}
		}

		NodeList list = doc.getDocumentElement().getChildNodes();
		Element el = su.getElement(list, "name", name);
		if (el != null) {
			el.setAttribute("paused", Caster.toString(pause));
			// el.getParentNode().removeChild(el);
		}
		else if (throwWhenNotExist) throw new ScheduleException("can't " + (pause ? "pause" : "resume") + " schedule task [" + name + "], task doesn't exist");

		// init();
		su.store(doc, schedulerFile);
	}

	@Override
	public void removeScheduleTask(String name, boolean throwWhenNotExist) throws IOException, ScheduleException {
		synchronized (sync) {
			int pos = -1;
			for (int i = 0; i < tasks.length; i++) {
				if (tasks[i].getTask().equalsIgnoreCase(name)) {
					tasks[i].log(Log.LEVEL_INFO, "task gets removed");
					tasks[i].setValid(false);
					pos = i;
				}
			}
			if (pos != -1) {
				ScheduleTaskImpl[] newTasks = new ScheduleTaskImpl[tasks.length - 1];
				int count = 0;
				for (int i = 0; i < tasks.length; i++) {
					if (i != pos) newTasks[count++] = tasks[i];

				}
				tasks = newTasks;
			}

			NodeList list = doc.getDocumentElement().getChildNodes();
			Element el = su.getElement(list, "name", name);
			if (el != null) {
				el.getParentNode().removeChild(el);
			}
			else if (throwWhenNotExist) throw new ScheduleException("can't delete schedule task [" + name + "], task doesn't exist");

			// init();
			su.store(doc, schedulerFile);
		}
	}

	public void removeIfNoLonerValid(ScheduleTask task) throws IOException {
		synchronized (sync) {
			ScheduleTaskImpl sti = (ScheduleTaskImpl) task;
			if (sti.isValid() || !sti.isAutoDelete()) return;

			try {
				removeScheduleTask(task.getTask(), false);
			}
			catch (ScheduleException e) {}
		}
	}

	@Override
	public void runScheduleTask(String name, boolean throwWhenNotExist) throws IOException, ScheduleException {
		synchronized (sync) {
			ScheduleTask task = getScheduleTask(name);
			if (task != null) {
				if (active()) execute(task);
			}
			else if (throwWhenNotExist) throw new ScheduleException("can't run schedule task [" + name + "], task doesn't exist");
			su.store(doc, schedulerFile);
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
}
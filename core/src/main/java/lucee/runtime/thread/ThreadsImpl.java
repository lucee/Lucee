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
package lucee.runtime.thread;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.Thread.State;
import java.util.Iterator;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.tag.Http;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;

public class ThreadsImpl extends StructSupport implements lucee.runtime.type.scope.Threads {

	private static final Key KEY_ERROR = KeyConstants._ERROR;
	private static final Key KEY_ELAPSEDTIME = KeyImpl.getInstance("ELAPSEDTIME");
	private static final Key KEY_OUTPUT = KeyConstants._OUTPUT;
	private static final Key KEY_PRIORITY = KeyImpl.getInstance("PRIORITY");
	private static final Key KEY_STARTTIME = KeyImpl.getInstance("STARTTIME");
	private static final Key KEY_STATUS = KeyConstants._STATUS;
	private static final Key KEY_STACKTRACE = KeyConstants._STACKTRACE;
	private static final Key KEY_CHILD_THREADS = KeyImpl.getInstance("childThreads");

	private static final Key[] DEFAULT_KEYS = new Key[] { KEY_ELAPSEDTIME, KeyConstants._NAME, KEY_OUTPUT, KEY_PRIORITY, KEY_STARTTIME, KEY_STATUS, KEY_STACKTRACE,
			KEY_CHILD_THREADS };

	private ChildThreadImpl ct;

	public ThreadsImpl(ChildThreadImpl ct) {
		this.ct = ct;
	}

	@Override
	public ChildThread getChildThread() {
		return ct;
	}

	@Override
	public final boolean containsKey(Key key) {
		return get(key, null) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return get(key, null) != null;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public int getType() {
		return -1;
	}

	@Override
	public String getTypeAsString() {
		return "thread";
	}

	@Override
	public void initialize(PageContext pc) {

	}

	@Override
	public boolean isInitalized() {
		return true;
	}

	@Override
	public void release(PageContext pc) {
	}

	@Override
	public void clear() {
		ct.content.clear();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		StructImpl sct = new StructImpl();
		boolean inside = deepCopy ? ThreadLocalDuplication.set(this, sct) : true;
		try {
			Iterator<Entry<Key, Object>> it = entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				sct.setEL(e.getKey(), deepCopy ? Duplicator.duplicate(e.getValue(), deepCopy) : e.getValue());
			}
		}
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}
		return sct;
	}

	private Object getMeta(Key key, Object defaultValue) {
		if (KEY_ELAPSEDTIME.equalsIgnoreCase(key)) return (String) getState() == "TERMINATED" ? 0 : new Double(ct.getEndTime() - ct.getStartTime());
		if (KeyConstants._NAME.equalsIgnoreCase(key)) return ct.getTagName();
		if (KEY_OUTPUT.equalsIgnoreCase(key)) return getOutput();
		if (KEY_PRIORITY.equalsIgnoreCase(key)) return ThreadUtil.toStringPriority(ct.getPriority());
		if (KEY_STARTTIME.equalsIgnoreCase(key)) return new DateTimeImpl(ct.getStartTime(), true);
		if (KEY_STATUS.equalsIgnoreCase(key)) return getState();
		if (KEY_ERROR.equalsIgnoreCase(key)) return ct.catchBlock;
		if (KEY_STACKTRACE.equalsIgnoreCase(key)) return getStackTrace();
		if (KEY_CHILD_THREADS.equalsIgnoreCase(key)) return Duplicator.duplicate(getThreads(), false);
		return defaultValue;
	}

	private Object getThreads() {
		return ct.getThreads();
	}

	private String getStackTrace() {
		StringBuilder sb = new StringBuilder();
		try {
			StackTraceElement[] trace = ct.getStackTrace();
			if (trace != null) for (int i = 0; i < trace.length; i++) {
				sb.append("\tat ");
				sb.append(trace[i]);
				sb.append("\n");
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return sb.toString();
	}

	private Object getOutput() {
		if (ct.output == null) return "";

		InputStream is = new ByteArrayInputStream(ct.output.toByteArray());
		return Http.getOutput(is, ct.contentType, ct.contentEncoding, true);

	}

	private Object getState() {
		/*
		 * 
		 * 
		 * The current status of the thread; one of the following values:
		 * 
		 */
		try {
			State state = ct.getState();
			if (State.NEW.equals(state)) return "NOT_STARTED";
			if (State.WAITING.equals(state)) return "WAITING";
			if (State.TERMINATED.equals(state)) {
				if (ct.terminated || ct.catchBlock != null) return "TERMINATED";
				return "COMPLETED";
			}

			return "RUNNING";
		}
		// java 1.4 execution
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (ct.terminated || ct.catchBlock != null) return "TERMINATED";
			if (ct.completed) return "COMPLETED";
			if (!ct.isAlive()) return "WAITING";
			return "RUNNING";

		}
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return get((PageContext) null, key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		Object _null = NullSupportHelper.NULL(pc);
		Object meta = getMeta(key, _null);
		if (meta != _null) return meta;
		return ct.content.get(pc, key, defaultValue);
	}

	@Override
	public final Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		Object _null = NullSupportHelper.NULL(pc);
		Object meta = getMeta(key, _null);
		if (meta != _null) return meta;
		return ct.content.get(pc, key);
	}

	@Override
	public Key[] keys() {
		Key[] skeys = CollectionUtil.keys(ct.content);

		if (skeys.length == 0 && ct.catchBlock == null) return DEFAULT_KEYS;

		Key[] rtn = new Key[skeys.length + (ct.catchBlock != null ? 1 : 0) + DEFAULT_KEYS.length];
		int index = 0;
		for (; index < DEFAULT_KEYS.length; index++) {
			rtn[index] = DEFAULT_KEYS[index];
		}
		if (ct.catchBlock != null) {
			rtn[index] = KEY_ERROR;
			index++;
		}

		for (int i = 0; i < skeys.length; i++) {
			rtn[index++] = skeys[i];
		}
		return rtn;
	}

	@Override
	public Object remove(Key key) throws PageException {
		Object _null = NullSupportHelper.NULL();
		if (isReadonly()) throw errorOutside();
		Object meta = getMeta(key, _null);
		if (meta != _null) throw errorMeta(key);
		return ct.content.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		if (isReadonly()) return null;
		return ct.content.removeEL(key);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {

		if (isReadonly()) throw errorOutside();
		Object _null = NullSupportHelper.NULL();
		Object meta = getMeta(key, _null);
		if (meta != _null) throw errorMeta(key);
		return ct.content.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		if (isReadonly()) return null;
		Object _null = NullSupportHelper.NULL();
		Object meta = getMeta(key, _null);
		if (meta != _null) return null;
		return ct.content.setEL(key, value);
	}

	@Override
	public int size() {
		return ct.content.size() + DEFAULT_KEYS.length + (ct.catchBlock == null ? 0 : 1);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		Key[] keys = keys();
		DumpTable table = new DumpTable("struct", "#9999ff", "#ccccff", "#000000");
		table.setTitle("Struct");
		maxlevel--;
		int maxkeys = dp.getMaxKeys();
		int index = 0;
		for (int i = 0; i < keys.length; i++) {
			Key key = keys[i];
			if (maxkeys <= index++) break;
			if (DumpUtil.keyValid(dp, maxlevel, key)) table.appendRow(1, new SimpleDumpData(key.getString()), DumpUtil.toDumpData(get(key, null), pageContext, maxlevel, dp));
		}
		return table;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new lucee.runtime.type.it.KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ValueIterator(this, keys());
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return ct.content.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return ct.content.castToBoolean(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return ct.content.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return ct.content.castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return ct.content.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return ct.content.castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return ct.content.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return ct.content.castToString(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return ct.content.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return ct.content.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return ct.content.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return ct.content.compareTo(dt);
	}

	private boolean isReadonly() {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc == null) return true;
		return pc.getThread() != ct;
	}

	private ApplicationException errorOutside() {
		return new ApplicationException("the thread scope cannot be modified from outside the owner thread");
	}

	private ApplicationException errorMeta(Key key) {
		return new ApplicationException("the metadata " + key.getString() + " of the thread scope are readonly");
	}
}
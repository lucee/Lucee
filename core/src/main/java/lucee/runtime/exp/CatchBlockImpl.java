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
package lucee.runtime.exp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.MemberUtil;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;
import lucee.runtime.util.PageContextUtil;

public class CatchBlockImpl extends StructImpl implements CatchBlock, Castable, Objects {

	private static final long serialVersionUID = -3680961614605720352L;

	public static final Key ERROR_CODE = KeyImpl.intern("ErrorCode");
	public static final Key CAUSE = KeyImpl.intern("Cause");
	public static final Key EXTENDEDINFO = KeyImpl.intern("ExtendedInfo");
	public static final Key EXTENDED_INFO = KeyImpl.intern("Extended_Info");
	public static final Key TAG_CONTEXT = KeyImpl.intern("TagContext");
	public static final Key STACK_TRACE = KeyImpl.intern("StackTrace");
	public static final Key ADDITIONAL = KeyImpl.intern("additional");

	private PageException exception;

	public CatchBlockImpl(PageException pe) {
		this(pe, 0);
	}

	private CatchBlockImpl(PageException pe, int level) {
		this.exception = pe;

		setEL(KeyConstants._Message, new SpecialItem(pe, KeyConstants._Message, level));
		setEL(KeyConstants._Detail, new SpecialItem(pe, KeyConstants._Detail, level));
		setEL(ERROR_CODE, new SpecialItem(pe, ERROR_CODE, level));
		setEL(EXTENDEDINFO, new SpecialItem(pe, EXTENDEDINFO, level));
		setEL(EXTENDED_INFO, new SpecialItem(pe, EXTENDED_INFO, level));
		setEL(ADDITIONAL, new SpecialItem(pe, ADDITIONAL, level));
		setEL(TAG_CONTEXT, new SpecialItem(pe, TAG_CONTEXT, level));
		setEL(KeyConstants._type, new SpecialItem(pe, KeyConstants._type, level));
		setEL(STACK_TRACE, new SpecialItem(pe, STACK_TRACE, level));
		setEL(CAUSE, new SpecialItem(pe, CAUSE, level));

		if (pe instanceof NativeException) {
			Throwable throwable = ((NativeException) pe).getException();
			Method[] mGetters = Reflector.getGetters(throwable.getClass());
			Method getter;
			Collection.Key key;
			if (!ArrayUtil.isEmpty(mGetters)) {
				for (int i = 0; i < mGetters.length; i++) {
					getter = mGetters[i];
					if (getter.getDeclaringClass() == Throwable.class) {
						continue;
					}
					key = KeyImpl.init(Reflector.removeGetterPrefix(getter.getName()));

					if (KeyConstants._Message.equalsIgnoreCase(key) || KeyConstants._Detail.equalsIgnoreCase(key)) {
						if (getter.getReturnType() != String.class) continue;
					}
					else if (STACK_TRACE.equalsIgnoreCase(key) || KeyConstants._type.equalsIgnoreCase(key) || CAUSE.equalsIgnoreCase(key)) continue;
					setEL(key, new Pair(throwable, key, getter, false));
				}
			}
		}
	}

	class SpecialItem {
		private static final int MAX = 10;
		private PageException pe;
		private Key key;
		private int level;

		public SpecialItem(PageException pe, Key key, int level) {
			this.pe = pe;
			this.key = key;
			this.level = level;
		}

		public Object get() {
			if (level < MAX) {
				if (key == CAUSE) return getCauseAsCatchBlock();
				if (key == ADDITIONAL) return pe.getAdditional();

			}
			if (key == KeyConstants._Message) return StringUtil.emptyIfNull(pe.getMessage());
			if (key == KeyConstants._Detail) return StringUtil.emptyIfNull(pe.getDetail());
			if (key == ERROR_CODE) return StringUtil.emptyIfNull(pe.getErrorCode());
			if (key == EXTENDEDINFO) return StringUtil.emptyIfNull(pe.getExtendedInfo());
			if (key == EXTENDED_INFO) return StringUtil.emptyIfNull(pe.getExtendedInfo());
			if (key == KeyConstants._type) return StringUtil.emptyIfNull(pe.getTypeAsString());
			if (key == STACK_TRACE) return StringUtil.emptyIfNull(pe.getStackTraceAsString());
			if (key == TAG_CONTEXT && pe instanceof PageExceptionImpl) return ((PageExceptionImpl) pe).getTagContext(ThreadLocalPageContext.getConfig());
			return null;
		}

		private CatchBlock getCauseAsCatchBlock() {
			Throwable cause = pe.getCause();
			if (cause == null || pe == cause) return null;
			if (pe instanceof NativeException && ((NativeException) pe).getException() == cause) return null;
			return new CatchBlockImpl(NativeException.newInstance(cause), level + 1);
		}

		public void set(Object o) {
			try {
				if (!(o instanceof Pair)) {
					if (key == KeyConstants._Detail) {
						pe.setDetail(Caster.toString(o));
						return;
					}
					else if (key == ERROR_CODE) {
						pe.setErrorCode(Caster.toString(o));
						return;
					}
					else if (key == EXTENDEDINFO || key == EXTENDED_INFO) {
						pe.setExtendedInfo(Caster.toString(o));
						return;
					}
					else if (key == STACK_TRACE) {
						if (o instanceof StackTraceElement[]) {
							pe.setStackTrace((StackTraceElement[]) o);
							return;
						}
						else if (Decision.isCastableToArray(o)) {
							Object[] arr = Caster.toNativeArray(o);
							StackTraceElement[] elements = new StackTraceElement[arr.length];
							for (int i = 0; i < arr.length; i++) {
								if (arr[i] instanceof StackTraceElement) elements[i] = (StackTraceElement) arr[i];
								else throw new CasterException(o, StackTraceElement[].class);
							}
							pe.setStackTrace(elements);
							return;

						}
					}
				}
			}
			catch (PageException pe) {}

			superSetEL(key, o);

		}

		public Object remove() {
			Object rtn = null;
			if (key == KeyConstants._Detail) {
				rtn = pe.getDetail();
				pe.setDetail("");
			}
			else if (key == ERROR_CODE) {
				rtn = pe.getErrorCode();
				pe.setErrorCode("0");
			}
			else if (key == EXTENDEDINFO || key == EXTENDED_INFO) {
				rtn = pe.getExtendedInfo();
				pe.setExtendedInfo(null);
			}
			return rtn;

		}
	}

	/**
	 * @return the pe
	 */
	@Override
	public PageException getPageException() {
		return exception;
	}

	@Override
	public String castToString() throws ExpressionException {
		return castToString(null);
	}

	@Override
	public String castToString(String defaultValue) {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc instanceof PageContextImpl) {
			try {
				return PageContextUtil.getHandlePageException((PageContextImpl) pc, exception);
			}
			catch (PageException e) {}
		}
		return exception.getClass().getName();
	}

	@Override
	public boolean containsValue(Object value) {
		Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if (get(keys[i], null) == value) return true;
		}
		return false;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImpl();
		StructUtil.copy(this, sct, true);
		return sct;
	}

	@Override
	public Set entrySet() {
		return StructUtil.entrySet(this);
	}

	@Override
	public void print(PageContext pc) {
		pc.handlePageException(exception);

	}

	@Override
	public Object get(Key key, Object defaultValue) {
		Object value = super.get(key, defaultValue);
		if (value instanceof SpecialItem) {
			return ((SpecialItem) value).get();
		}
		else if (value instanceof Pair) {
			Pair pair = (Pair) value;
			try {
				Object res = pair.getter.invoke(pair.throwable, new Object[] {});
				if (pair.doEmptyStringWhenNull && res == null) return "";
				return res;
			}
			catch (Exception e) {
				return defaultValue;
			}
		}
		return value;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		Object curr = super.get(key, null);
		if (curr instanceof SpecialItem) {
			((SpecialItem) curr).set(value);
			return value;
		}
		else if (curr instanceof Pair) {
			Pair pair = (Pair) curr;
			MethodInstance setter = Reflector.getSetter(pair.throwable, pair.name.getString(), value, null);
			if (setter != null) {
				try {
					setter.invoke(pair.throwable);
					return value;
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
		}

		return super.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		Object curr = super.get(key, null);
		if (curr instanceof SpecialItem) {
			((SpecialItem) curr).set(value);
			return value;
		}
		else if (curr instanceof Pair) {
			Pair pair = (Pair) curr;
			MethodInstance setter = Reflector.getSetter(pair.throwable, pair.name.getString(), value, null);
			if (setter != null) {
				try {
					setter.invoke(pair.throwable);
				}
				catch (Exception e) {}
				return value;
			}
		}
		return super.setEL(key, value);
	}

	private Object superSetEL(Key key, Object value) {
		return super.setEL(key, value);
	}

	@Override
	public int size() {
		return keys().length;
	}

	@Override
	public Key[] keys() {
		Key[] keys = super.keys();
		List<Key> list = new ArrayList<Key>();
		for (int i = 0; i < keys.length; i++) {
			if (get(keys[i], null) != null) list.add(keys[i]);
		}
		return list.toArray(new Key[list.size()]);
	}

	@Override
	public Object remove(Key key) throws PageException {
		Object curr = super.get(key, null);
		if (curr instanceof SpecialItem) {
			return ((SpecialItem) curr).remove();
		}
		else if (curr instanceof Pair) {
			Pair pair = (Pair) curr;
			MethodInstance setter = Reflector.getSetter(pair.throwable, pair.name.getString(), null, null);
			if (setter != null) {
				try {
					Object before = pair.getter.invoke(pair.throwable, new Object[0]);
					setter.invoke(pair.throwable);
					return before;
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
		}
		return super.remove(key);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
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
	public java.util.Collection values() {
		return StructUtil.values(this);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return StructUtil.toDumpTable(this, "Catch", pageContext, maxlevel, dp);
	}

	class Pair {
		Throwable throwable;
		Collection.Key name;
		Method getter;
		private boolean doEmptyStringWhenNull;

		public Pair(Throwable throwable, Key name, Method method, boolean doEmptyStringWhenNull) {
			this.throwable = throwable;
			this.name = name;
			this.getter = method;
			this.doEmptyStringWhenNull = doEmptyStringWhenNull;
		}

		public Pair(Throwable throwable, String name, Method method, boolean doEmptyStringWhenNull) {
			this(throwable, KeyImpl.init(name), method, doEmptyStringWhenNull);
		}

		@Override
		public String toString() {
			try {
				return Caster.toString(getter.invoke(throwable, new Object[] {}));
			}
			catch (Exception e) {
				throw new PageRuntimeException(Caster.toPageException(e));
			}
		}
	}

	public Object call(PageContext pc, String methodName, Object[] arguments) throws PageException {
		Object obj = exception;
		if (exception instanceof NativeException) obj = ((NativeException) exception).getException();
		if ("dump".equalsIgnoreCase(methodName)) {
			print(pc);
			return null;
		}

		return MemberUtil.call(pc, this, KeyImpl.init(methodName), arguments, new short[] { CFTypes.TYPE_STRUCT }, new String[] { "struct" });

		/*
		 * try{ return Reflector.callMethod(obj, methodName, arguments); } catch(PageException e){ return
		 * Reflector.callMethod(exception, methodName, arguments); }
		 */
	}

	public Object callWithNamedValues(PageContext pc, String methodName, Struct args) throws PageException {
		throw new ApplicationException("named arguments not supported");
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		throw new ApplicationException("named arguments not supported");
	}

	public boolean isInitalized() {
		return true;
	}

	public Object set(PageContext pc, String propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	public Object setEL(PageContext pc, String propertyName, Object value) {
		return setEL(propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	@Override
	public Object get(Key key) throws PageException {
		Object res = get(key, CollectionUtil.NULL);
		if (res != CollectionUtil.NULL) return res;
		throw StructSupport.invalidKey(null, this, key, "catch block");
	}

	public Object get(PageContext pc, String key, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

	public Object get(PageContext pc, String key) throws PageException {
		return get(key);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return get(key);
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		return call(pc, methodName.getString(), arguments);
	}

	/*
	 * public Object remove (String key) throws PageException { return remove(KeyImpl.init(key)); }
	 */
	@Override
	public Object removeEL(Key key) {
		try {
			return remove(key);
		}
		catch (PageException e) {
			return null;
		}
	}
}
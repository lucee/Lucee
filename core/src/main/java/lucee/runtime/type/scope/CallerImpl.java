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
package lucee.runtime.type.scope;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;

/**
 * caller scope
 */
public final class CallerImpl extends StructSupport implements Caller {

	private static final long serialVersionUID = -6228400815042475435L;

	private PageContext pc;
	private Variables variablesScope;
	private Local localScope;
	private Argument argumentsScope;
	private boolean checkArgs;

	@Override
	public Object get(Collection.Key key) throws PageException {
		return get(pc, key);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {

		char c = key.lowerCharAt(0);
		if ('a' == c) {
			if (KeyConstants._application.equalsIgnoreCase(key)) return pc.applicationScope();
			else if (checkArgs && KeyConstants._arguments.equalsIgnoreCase(key)) return argumentsScope;// pc.argumentsScope();
		}
		else if ('c' == c) {
			if (KeyConstants._cgi.equalsIgnoreCase(key)) return pc.cgiScope();
			if (KeyConstants._cookie.equalsIgnoreCase(key)) return pc.cookieScope();
			if (KeyConstants._client.equalsIgnoreCase(key)) return pc.clientScope();
			if (KeyConstants._cluster.equalsIgnoreCase(key)) return pc.clusterScope();
		}
		else if ('f' == c) {
			if (KeyConstants._form.equalsIgnoreCase(key)) return pc.formScope();
		}
		else if ('r' == c) {
			if (KeyConstants._request.equalsIgnoreCase(key)) return pc.requestScope();
		}
		else if ('l' == c) {
			if (KeyConstants._local.equalsIgnoreCase(key) && checkArgs) return localScope;// pc.localScope();
		}
		else if ('s' == c) {
			if (KeyConstants._session.equalsIgnoreCase(key)) return pc.sessionScope();
			if (KeyConstants._server.equalsIgnoreCase(key)) return pc.serverScope();
		}
		else if ('u' == c) {
			if (KeyConstants._url.equalsIgnoreCase(key)) return pc.urlScope();
		}
		else if ('v' == c) {
			if (KeyConstants._variables.equalsIgnoreCase(key)) return variablesScope;
		}

		// upper variable scope
		Object o;

		Object _null = NullSupportHelper.NULL(pc);
		if (checkArgs) {
			o = localScope.get(key, _null);
			if (o != _null) return o;
			o = argumentsScope.get(key, _null);
			if (o != _null) return o;
		}
		o = variablesScope.get(key, _null);
		if (o != _null) return o;

		// get from cascaded scopes
		o = ((UndefinedImpl) pc.undefinedScope()).getCascading(key, _null);
		if (o != _null) return o;

		/*
		 * // get scopes if(key.equalsIgnoreCase(VARIABLES)) { return variablesScope;//new
		 * StructImpl(getMap()); }
		 * 
		 * scope=VariableInterpreter.scopeKey2Int(key); if(scope!=Scope.SCOPE_UNDEFINED) return
		 * pc.scope(scope);
		 */
		throw new ExpressionException("[" + key.getString() + "] not found in caller scope");
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return get(pc, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {

		char c = key.lowerCharAt(0);
		if ('a' == c) {
			if (KeyConstants._application.equalsIgnoreCase(key)) {
				try {
					return pc.applicationScope();
				}
				catch (PageException e) {
				}
			}
			else if (checkArgs && KeyConstants._arguments.equalsIgnoreCase(key)) return argumentsScope;// pc.argumentsScope();
		}
		else if ('c' == c) {
			if (KeyConstants._cgi.equalsIgnoreCase(key)) return pc.cgiScope();
			if (KeyConstants._cookie.equalsIgnoreCase(key)) return pc.cookieScope();
			if (KeyConstants._client.equalsIgnoreCase(key)) {
				try {
					return pc.clientScope();
				}
				catch (PageException e) {
				}
			}
			if (KeyConstants._cluster.equalsIgnoreCase(key)) {
				try {
					return pc.clusterScope();
				}
				catch (PageException e) {
				}
			}
		}
		else if ('f' == c) {
			if (KeyConstants._form.equalsIgnoreCase(key)) return pc.formScope();
		}
		else if ('r' == c) {
			if (KeyConstants._request.equalsIgnoreCase(key)) return pc.requestScope();
		}
		else if ('l' == c) {
			if (checkArgs && KeyConstants._local.equalsIgnoreCase(key)) return localScope;// pc.localScope();
		}
		else if ('s' == c) {
			if (KeyConstants._session.equalsIgnoreCase(key)) {
				try {
					return pc.sessionScope();
				}
				catch (PageException e) {
				}
			}
			if (KeyConstants._server.equalsIgnoreCase(key)) {
				try {
					return pc.serverScope();
				}
				catch (PageException e) {
				}
			}
		}
		else if ('u' == c) {
			if (KeyConstants._url.equalsIgnoreCase(key)) return pc.urlScope();
		}
		else if ('v' == c) {
			if (KeyConstants._variables.equalsIgnoreCase(key)) return variablesScope;
		}

		Object _null = NullSupportHelper.NULL(pc);
		Object o;
		if (checkArgs) {
			o = localScope.get(key, _null);
			if (o != _null) return o;
			o = argumentsScope.get(key, _null);
			if (o != _null) return o;
		}
		o = variablesScope.get(key, _null);
		if (o != _null) return o;

		// get from cascaded scopes
		o = ((UndefinedImpl) pc.undefinedScope()).getCascading(key, _null);
		if (o != _null) return o;

		return defaultValue;
	}

	@Override
	public void initialize(PageContext pc) {
		this.pc = pc;
	}

	@Override
	public void setScope(Variables variablesScope, Local localScope, Argument argumentsScope, boolean checkArgs) {
		this.variablesScope = variablesScope;
		this.localScope = localScope;
		this.argumentsScope = argumentsScope;
		this.checkArgs = checkArgs;
	}

	@Override
	public boolean isInitalized() {
		return pc != null;
	}

	@Override
	public void release(PageContext pc) {
		this.pc = null;
	}

	@Override
	public int size() {
		return variablesScope.size();
	}

	@Override
	public Collection.Key[] keys() {
		return CollectionUtil.keys(this);
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		if (checkArgs && localScope.containsKey(key)) return localScope.remove(key);
		return variablesScope.remove(key);
	}

	@Override
	public Object removeEL(Collection.Key key) {
		if (checkArgs && localScope.containsKey(key)) return localScope.removeEL(key);
		return variablesScope.removeEL(key);
	}

	@Override
	public void clear() {
		variablesScope.clear();
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		if (checkArgs) {
			if (localScope.containsKey(key)) return localScope.set(key, value);
			if (argumentsScope.containsKey(key)) return argumentsScope.set(key, value);
		}
		return variablesScope.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		if (checkArgs) {
			if (localScope.containsKey(key)) return localScope.setEL(key, value);
			if (argumentsScope.containsKey(key)) return argumentsScope.setEL(key, value);
		}
		return variablesScope.setEL(key, value);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return variablesScope.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return variablesScope.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return variablesScope.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return variablesScope.valueIterator();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return (Collection) Duplicator.duplicate(variablesScope, deepCopy);
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return variablesScope.toDumpData(pageContext, --maxlevel, dp);
	}

	@Override
	public String castToString() throws PageException {
		return variablesScope.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return variablesScope.castToString(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return variablesScope.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return variablesScope.castToBoolean(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return variablesScope.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return variablesScope.castToDoubleValue(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return variablesScope.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return variablesScope.castToDateTime(defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return variablesScope.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return variablesScope.compareTo(dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return variablesScope.compareTo(d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return variablesScope.compareTo(str);
	}

	@Override
	public int getType() {
		return SCOPE_CALLER;
	}

	@Override
	public String getTypeAsString() {
		return "caller";
	}

	@Override
	public boolean containsValue(Object value) {
		return variablesScope.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return variablesScope.values();
	}

	@Override
	public Variables getVariablesScope() {
		return variablesScope;
	}

	@Override
	public Local getLocalScope() {
		return localScope;
	}

	@Override
	public Argument getArgumentsScope() {
		return argumentsScope;
	}
}
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
package lucee.runtime.interpreter;

import lucee.commons.lang.ParserString;
import lucee.commons.lang.StringList;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ref.VariableReference;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.CallerImpl;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.scope.UndefinedImpl;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * Class to check and interpret Variable Strings
 */
public final class VariableInterpreter {

	/**
	 * reads a subelement from a struct
	 * 
	 * @param pc
	 * @param collection
	 * @param var
	 * @return matching Object
	 * @throws PageException
	 */
	public static Object getVariable(PageContext pc, Collection collection, String var) throws PageException {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) throw new InterpreterException("invalid variable declaration [" + var + "]");

		while (list.hasNextNext()) {
			collection = Caster.toCollection(collection.get(KeyImpl.init(list.next())));
		}
		return collection.get(KeyImpl.init(list.next()));
	}

	public static String scopeInt2String(int type) {
		switch (type) {
		case Scope.SCOPE_APPLICATION:
			return "application";
		case Scope.SCOPE_ARGUMENTS:
			return "arguments";
		case Scope.SCOPE_CGI:
			return "cgi";
		case Scope.SCOPE_COOKIE:
			return "cookie";
		case Scope.SCOPE_CLIENT:
			return "client";
		case Scope.SCOPE_FORM:
			return "form";
		case Scope.SCOPE_REQUEST:
			return "request";
		case Scope.SCOPE_SESSION:
			return "session";
		case Scope.SCOPE_SERVER:
			return "server";
		case Scope.SCOPE_URL:
			return "url";
		case Scope.SCOPE_VARIABLES:
			return "variables";
		case Scope.SCOPE_CLUSTER:
			return "cluster";
		case Scope.SCOPE_LOCAL:
			return "local";
		}
		return null;
	}

	public static Object getVariableEL(PageContext pc, Collection collection, String var) {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) return null;

		while (list.hasNextNext()) {
			collection = Caster.toCollection(collection.get(list.next(), null), null);
			if (collection == null) return null;
		}
		return collection.get(list.next(), null);
	}

	/**
	 * get a variable from page context
	 * 
	 * @param pc Page Context
	 * @param var variable string to get value to
	 * @return the value
	 * @throws PageException
	 */
	public static Object getVariable(PageContext pc, String var) throws PageException {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) throw new InterpreterException("invalid variable declaration [" + var + "]");

		int scope = scopeString2Int(pc.ignoreScopes(), list.next());
		Object coll = null;
		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.undefinedScope().get(list.current());
		}
		else {
			coll = VariableInterpreter.scope(pc, scope, list.hasNext());
		}

		while (list.hasNext()) {
			coll = pc.getVariableUtil().get(pc, coll, list.next());
		}
		return coll;
	}

	public static Object getVariableAsCollection(PageContext pc, String var) throws PageException {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) throw new InterpreterException("invalid variable declaration [" + var + "]");

		int scope = scopeString2Int(pc.ignoreScopes(), list.next());
		Object coll = null;
		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.undefinedScope().getCollection(list.current());
		}
		else {
			coll = VariableInterpreter.scope(pc, scope, list.hasNext());
		}

		while (list.hasNext()) {
			coll = pc.getVariableUtil().getCollection(pc, coll, list.next());
		}
		return coll;
	}

	public static Object getVariable(PageContext pc, String str, Scope scope) throws PageException {
		return _variable(pc, str, CollectionUtil.NULL, scope);
	}

	public static Object setVariable(PageContext pc, String str, Object value, Scope scope) throws PageException {
		return _variable(pc, str, value, scope);
	}

	public static Object _variable(PageContext pc, String str, Object value, Scope scope) throws PageException {
		// define another enviroment for the function
		if (scope != null) {

			// Variables Scope
			Variables var = null;
			if (scope instanceof Variables) {
				var = (Variables) scope;
			}
			else if (scope instanceof CallerImpl) {
				var = ((CallerImpl) scope).getVariablesScope();
			}
			if (var != null) {
				Variables current = pc.variablesScope();
				pc.setVariablesScope(var);
				try {
					if (value != CollectionUtil.NULL) return setVariable(pc, str, value);
					return getVariable(pc, str);
				}
				finally {
					pc.setVariablesScope(current);
				}
			}

			// Undefined Scope
			else if (scope instanceof Undefined) {
				PageContextImpl pci = (PageContextImpl) pc;
				Undefined undefined = (Undefined) scope;

				boolean check = undefined.getCheckArguments();
				Variables orgVar = pc.variablesScope();
				Argument orgArgs = pc.argumentsScope();
				Local orgLocal = pc.localScope();

				pci.setVariablesScope(undefined.variablesScope());
				if (check) pci.setFunctionScopes(undefined.localScope(), undefined.argumentsScope());
				try {
					if (value != CollectionUtil.NULL) return setVariable(pc, str, value);
					return getVariable(pc, str);
				}
				finally {
					pc.setVariablesScope(orgVar);
					if (check) pci.setFunctionScopes(orgLocal, orgArgs);
				}
			}
		}
		if (value != CollectionUtil.NULL) return setVariable(pc, str, value);
		return getVariable(pc, str);
	}

	/**
	 * get a variable from page context
	 * 
	 * @param pc Page Context
	 * @param var variable string to get value to
	 * @param defaultValue value returnded if variable was not found
	 * @return the value or default value if not found
	 */
	public static Object getVariableEL(PageContext pc, String var, Object defaultValue) {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) return defaultValue;
		Object _null = NullSupportHelper.NULL(pc);

		int scope = scopeString2Int(pc.ignoreScopes(), list.next());
		Object coll = null;
		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.undefinedScope().get(KeyImpl.init(list.current()), _null);
			if (coll == _null) return defaultValue;
		}
		else {
			try {
				coll = VariableInterpreter.scope(pc, scope, list.hasNext());
				// coll=pc.scope(scope);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		while (list.hasNext()) {
			coll = pc.getVariableUtil().get(pc, coll, KeyImpl.init(list.next()), _null);
			if (coll == _null) return defaultValue;
		}
		return coll;
	}

	public static Object getVariableELAsCollection(PageContext pc, String var, Object defaultValue) {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) return defaultValue;

		int scope = scopeString2Int(pc.ignoreScopes(), list.next());
		Object coll = null;
		if (scope == Scope.SCOPE_UNDEFINED) {
			try {
				coll = pc.undefinedScope().getCollection(list.current());
			}
			catch (PageException e) {
				coll = null;
			}
			if (coll == null) return defaultValue;
		}
		else {
			try {
				coll = VariableInterpreter.scope(pc, scope, list.hasNext());
				// coll=pc.scope(scope);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		while (list.hasNext()) {
			coll = pc.getVariableUtil().getCollection(pc, coll, list.next(), null);
			if (coll == null) return defaultValue;
		}
		return coll;
	}

	/**
	 * return a variable reference by string syntax ("scopename.key.key" -> "url.name") a variable
	 * reference, references to variable, to modifed it, with global effect.
	 * 
	 * @param pc
	 * @param var variable name to get
	 * @return variable as Reference
	 * @throws PageException
	 */
	public static VariableReference getVariableReference(PageContext pc, String var) throws PageException {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) throw new InterpreterException("invalid variable declaration [" + var + "]");

		if (list.size() == 1) {
			return new VariableReference(pc.undefinedScope(), list.next());
		}
		int scope = scopeString2Int(pc.ignoreScopes(), list.next());

		Object coll;
		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.touch(pc.undefinedScope(), KeyImpl.init(list.current()));
		}
		else {
			coll = VariableInterpreter.scope(pc, scope, list.hasNext());
			// coll=pc.scope(scope);
		}

		while (list.hasNextNext()) {
			coll = pc.touch(coll, KeyImpl.init(list.next()));
		}

		if (!(coll instanceof Collection)) throw new InterpreterException("invalid variable [" + var + "]");
		return new VariableReference((Collection) coll, list.next());
	}

	public static VariableReference getVariableReference(PageContext pc, Collection.Key key, boolean keepScope) {
		if (keepScope) {
			Collection coll = ((UndefinedImpl) pc.undefinedScope()).getScopeFor(key, null);
			if (coll != null) return new VariableReference(coll, key);
		}
		return new VariableReference(pc.undefinedScope(), key);
	}

	public static VariableReference getVariableReference(PageContext pc, Collection.Key[] keys, boolean keepScope) throws PageException {
		if (keys.length == 1) {
			if (keepScope) {
				Collection coll = ((UndefinedImpl) pc.undefinedScope()).getScopeFor(keys[0], null);
				if (coll != null) return new VariableReference(coll, keys[0]);
			}
			return new VariableReference(pc.undefinedScope(), keys[0]);
		}
		int scope = scopeKey2Int(keys[0]);

		Object coll;

		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.touch(pc.undefinedScope(), keys[0]);
		}
		else {
			coll = VariableInterpreter.scope(pc, scope, keys.length > 1);
		}

		for (int i = 1; i < (keys.length - 1); i++) {
			coll = pc.touch(coll, keys[i]);
		}

		if (!(coll instanceof Collection)) throw new InterpreterException("invalid variable [" + ListUtil.arrayToList(keys, ".") + "]");
		return new VariableReference((Collection) coll, keys[keys.length - 1]);
	}

	/**
	 * sets a variable to page Context
	 * 
	 * @param pc pagecontext of the new variable
	 * @param var String of variable definition
	 * @param value value to set to variable
	 * @return value setted
	 * @throws PageException
	 */
	public static Object setVariable(PageContext pc, String var, Object value) throws PageException {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) throw new InterpreterException("invalid variable name declaration [" + var + "]");

		if (list.size() == 1) {
			return pc.undefinedScope().set(list.next(), value);
		}

		// min 2 elements
		int scope = scopeString2Int(pc.ignoreScopes(), list.next());
		Object coll;
		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.touch(pc.undefinedScope(), KeyImpl.init(list.current()));
		}
		else {
			coll = VariableInterpreter.scope(pc, scope, true);
			// coll=pc.scope(scope);
		}

		while (list.hasNextNext()) {
			coll = pc.touch(coll, KeyImpl.init(list.next()));
		}
		return pc.set(coll, KeyImpl.init(list.next()), value);
	}

	/**
	 * removes a variable eith matching name from page context
	 * 
	 * @param pc
	 * @param var
	 * @return has removed or not
	 * @throws PageException
	 */
	public static Object removeVariable(PageContext pc, String var) throws PageException {
		// print.ln("var:"+var);
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) throw new InterpreterException("invalid variable declaration [" + var + "]");

		if (list.size() == 1) {
			return pc.undefinedScope().remove(KeyImpl.init(list.next()));
		}

		int scope = scopeString2Int(pc.ignoreScopes(), list.next());

		Object coll;
		if (scope == Scope.SCOPE_UNDEFINED) {
			coll = pc.undefinedScope().get(list.current());
		}
		else {
			coll = VariableInterpreter.scope(pc, scope, true);
			// coll=pc.scope(scope);
		}

		while (list.hasNextNext()) {
			coll = pc.get(coll, list.next());
		}
		return Caster.toCollection(coll).remove(KeyImpl.init(list.next()));
	}

	/**
	 * check if a variable is defined in Page Context
	 * 
	 * @param pc PageContext to check
	 * @param var variable String
	 * @return exists or not
	 */
	public static boolean isDefined(PageContext pc, String var) {
		StringList list = parse(pc, new ParserString(var), false);
		if (list == null) return false;
		try {
			int scope = scopeString2Int(pc.ignoreScopes(), list.next());
			Object coll = CollectionUtil.NULL;
			if (scope == Scope.SCOPE_UNDEFINED) {
				coll = pc.undefinedScope().get(list.current(), null);
				if (coll == null) return false;
			}
			else {
				coll = VariableInterpreter.scope(pc, scope, list.hasNext());
				// coll=pc.scope(scope);
			}

			while (list.hasNext()) {
				coll = pc.getVariableUtil().getCollection(pc, coll, list.next(), null);
				if (coll == null) return false;
			}
		}
		catch (PageException e) {
			return false;
		}
		return true;
	}

	/*
	 * public static boolean isDefined(PageContext pc,String var) { StringList list = parse(pc,new
	 * ParserString(var)); if(list==null) return false;
	 * 
	 * int scope=scopeString2Int(list.next()); Object coll =NULL; if(scope==Scope.SCOPE_UNDEFINED) {
	 * coll=pc.undefinedScope().get(list.current(),NULL); if(coll==NULL) return false; } else { try {
	 * coll=pc.scope(scope); } catch (PageException e) { return false; } }
	 * 
	 * while(list.hasNext()) { coll=pc.getVariableUtil().get(pc,coll,list.next(),NULL);
	 * //print.out(coll); if(coll==NULL) return false; }
	 * 
	 * return true; }
	 */

	/**
	 * parse a Literal variable String and return result as String List
	 * 
	 * @param pc Page Context
	 * @param ps ParserString to read
	 * @return Variable Definition in a String List
	 */
	private static StringList parse(PageContext pc, ParserString ps, boolean doLowerCase) {
		String id = readIdentifier(ps, doLowerCase);
		if (id == null) return null;
		StringList list = new StringList(id);
		CFMLExpressionInterpreter interpreter = null;

		while (true) {
			if (ps.forwardIfCurrent('.')) {
				id = readIdentifier(ps, doLowerCase);
				if (id == null) return null;
				list.add(id);
			}
			else if (ps.forwardIfCurrent('[')) {
				if (interpreter == null) interpreter = new CFMLExpressionInterpreter(false);
				try {
					list.add(Caster.toString(interpreter.interpretPart(pc, ps)));
				}
				catch (PageException e) {
					return null;
				}
				if (!ps.forwardIfCurrent(']')) return null;
				ps.removeSpace();
			}
			else break;
		}
		if (ps.isValidIndex()) return null;
		list.reset();
		return list;
	}

	public static StringList parse(String var, boolean doLowerCase) {
		ParserString ps = new ParserString(var);
		String id = readIdentifier(ps, doLowerCase);
		if (id == null) return null;
		StringList list = new StringList(id);

		while (true) {
			if (ps.forwardIfCurrent('.')) {
				id = readIdentifier(ps, doLowerCase);
				if (id == null) return null;
				list.add(id);
			}
			else break;
		}
		if (ps.isValidIndex()) return null;
		list.reset();
		return list;
	}

	/**
	 * translate a string type definition to its int representation
	 * 
	 * @param type type to translate
	 * @return int representation matching to given string
	 */
	public static int scopeString2Int(boolean ignoreScope, String type) {
		type = StringUtil.toLowerCase(type);
		char c = type.charAt(0);

		// ignore scope only handles only reconize local,arguments as scope, the rest is ignored
		if (ignoreScope) {
			if ('a' == c) {
				if ("arguments".equals(type)) return Scope.SCOPE_ARGUMENTS;
			}
			else if ('l' == c) {
				if ("local".equals(type)) return Scope.SCOPE_LOCAL;// LLL
			}

			else if ('r' == c) {
				if ("request".equals(type)) return Scope.SCOPE_REQUEST;
			}
			else if ('v' == c) {
				if ("variables".equals(type)) return Scope.SCOPE_VARIABLES;
			}
			else if ('s' == c) {
				if ("server".equals(type)) return Scope.SCOPE_SERVER;
			}
			return Scope.SCOPE_UNDEFINED;
		}

		if ('a' == c) {
			if ("application".equals(type)) return Scope.SCOPE_APPLICATION;
			else if ("arguments".equals(type)) return Scope.SCOPE_ARGUMENTS;
		}
		else if ('c' == c) {
			if ("cgi".equals(type)) return Scope.SCOPE_CGI;
			if ("cookie".equals(type)) return Scope.SCOPE_COOKIE;
			if ("client".equals(type)) return Scope.SCOPE_CLIENT;
			if ("cluster".equals(type)) return Scope.SCOPE_CLUSTER;
		}
		else if ('f' == c) {
			if ("form".equals(type)) return Scope.SCOPE_FORM;
		}
		else if ('l' == c) {
			if ("local".equals(type)) return Scope.SCOPE_LOCAL;// LLL
		}
		else if ('r' == c) {
			if ("request".equals(type)) return Scope.SCOPE_REQUEST;
		}
		else if ('s' == c) {
			if ("session".equals(type)) return Scope.SCOPE_SESSION;
			if ("server".equals(type)) return Scope.SCOPE_SERVER;
		}
		else if ('u' == c) {
			if ("url".equals(type)) return Scope.SCOPE_URL;
		}
		else if ('v' == c) {
			if ("variables".equals(type)) return Scope.SCOPE_VARIABLES;
		}
		return Scope.SCOPE_UNDEFINED;
	}

	public static int scopeKey2Int(Collection.Key type) {
		char c = type.lowerCharAt(0);
		if ('a' == c) {
			if (KeyConstants._application.equalsIgnoreCase(type)) return Scope.SCOPE_APPLICATION;
			else if (KeyConstants._arguments.equalsIgnoreCase(type)) return Scope.SCOPE_ARGUMENTS;
		}
		else if ('c' == c) {
			if (KeyConstants._cgi.equalsIgnoreCase(type)) return Scope.SCOPE_CGI;
			if (KeyConstants._cookie.equalsIgnoreCase(type)) return Scope.SCOPE_COOKIE;
			if (KeyConstants._client.equalsIgnoreCase(type)) return Scope.SCOPE_CLIENT;
			if (KeyConstants._cluster.equalsIgnoreCase(type)) return Scope.SCOPE_CLUSTER;
		}
		else if ('f' == c) {
			if (KeyConstants._form.equalsIgnoreCase(type)) return Scope.SCOPE_FORM;
		}
		else if ('r' == c) {
			if (KeyConstants._request.equalsIgnoreCase(type)) return Scope.SCOPE_REQUEST;
		}
		else if ('s' == c) {
			if (KeyConstants._session.equalsIgnoreCase(type)) return Scope.SCOPE_SESSION;
			if (KeyConstants._server.equalsIgnoreCase(type)) return Scope.SCOPE_SERVER;
		}
		else if ('u' == c) {
			if (KeyConstants._url.equalsIgnoreCase(type)) return Scope.SCOPE_URL;
		}
		else if ('v' == c) {
			if (KeyConstants._variables.equalsIgnoreCase(type)) return Scope.SCOPE_VARIABLES;
		}
		return Scope.SCOPE_UNDEFINED;
	}

	private static String readIdentifier(ParserString ps, boolean doLowerCase) {

		ps.removeSpace();
		if (ps.isAfterLast()) return null;
		int start = ps.getPos();
		if (!isFirstVarLetter(ps.getCurrentLower())) return null;
		ps.next();

		while (ps.isValidIndex()) {
			if (isVarLetter(ps.getCurrentLower())) ps.next();
			else break;
		}
		ps.removeSpace();
		return doLowerCase ? ps.substringLower(start, ps.getPos() - start) : ps.substring(start, ps.getPos() - start);
	}

	private static boolean isFirstVarLetter(char c) {
		return (c >= 'a' && c <= 'z') || c == '_' || c == '$';
	}

	private static boolean isVarLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '$';
	}

	public static Object scope(PageContext pc, int scope, boolean touch) throws PageException {
		switch (scope) {
		case Scope.SCOPE_UNDEFINED:
			return pc.undefinedScope();
		case Scope.SCOPE_URL:
			return pc.urlScope();
		case Scope.SCOPE_FORM:
			return pc.formScope();
		case Scope.SCOPE_VARIABLES:
			return pc.variablesScope();
		case Scope.SCOPE_REQUEST:
			return pc.requestScope();
		case Scope.SCOPE_CGI:
			return pc.cgiScope();
		case Scope.SCOPE_APPLICATION:
			return pc.applicationScope();
		case Scope.SCOPE_ARGUMENTS:
			return pc.argumentsScope();
		case Scope.SCOPE_SESSION:
			return pc.sessionScope();
		case Scope.SCOPE_SERVER:
			return pc.serverScope();
		case Scope.SCOPE_COOKIE:
			return pc.cookieScope();
		case Scope.SCOPE_CLIENT:
			return pc.clientScope();
		case Scope.SCOPE_VAR:
			return pc.localScope();
		case Scope.SCOPE_CLUSTER:
			return pc.clusterScope();

		case Scope.SCOPE_LOCAL:
			if (touch) return ((PageContextImpl) pc).localTouch();
			return ((PageContextImpl) pc).localGet();
		}
		return pc.variablesScope();
	}

}
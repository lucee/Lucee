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
package lucee.transformer.bytecode.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.CGI;
import lucee.runtime.type.scope.Client;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.Cookie;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.Request;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeSupport;
import lucee.runtime.type.scope.Server;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.URL;
import lucee.runtime.type.scope.Undefined;

public final class TypeScope {

	public static int SCOPE_UNDEFINED_LOCAL = 16;

	public final static Type SCOPE = Type.getType(Scope.class);
	public final static Type[] SCOPES = new Type[ScopeSupport.SCOPE_COUNT];
	static {
		SCOPES[Scope.SCOPE_APPLICATION] = Type.getType(Application.class);
		SCOPES[Scope.SCOPE_ARGUMENTS] = Type.getType(lucee.runtime.type.scope.Argument.class);
		SCOPES[Scope.SCOPE_CGI] = Type.getType(CGI.class);
		SCOPES[Scope.SCOPE_CLIENT] = Type.getType(Client.class);
		SCOPES[Scope.SCOPE_COOKIE] = Type.getType(Cookie.class);
		SCOPES[Scope.SCOPE_FORM] = Type.getType(Form.class);
		SCOPES[Scope.SCOPE_LOCAL] = Type.getType(Local.class);
		SCOPES[Scope.SCOPE_REQUEST] = Type.getType(Request.class);
		SCOPES[Scope.SCOPE_SERVER] = Type.getType(Server.class);
		SCOPES[Scope.SCOPE_SESSION] = Type.getType(Session.class);
		SCOPES[Scope.SCOPE_UNDEFINED] = Type.getType(Undefined.class);
		SCOPES[Scope.SCOPE_URL] = Type.getType(URL.class);
		SCOPES[Scope.SCOPE_VARIABLES] = Types.VARIABLES;
		SCOPES[Scope.SCOPE_CLUSTER] = Type.getType(Cluster.class);
		SCOPES[Scope.SCOPE_VAR] = SCOPES[Scope.SCOPE_LOCAL];
		// SCOPES[SCOPE_UNDEFINED_LOCAL]= SCOPES[Scope.SCOPE_LOCAL];
	}

	public final static Method[] METHODS = new Method[ScopeSupport.SCOPE_COUNT + 1];
	static {
		METHODS[Scope.SCOPE_APPLICATION] = new Method("applicationScope", SCOPES[Scope.SCOPE_APPLICATION], new Type[] {});
		METHODS[Scope.SCOPE_ARGUMENTS] = new Method("argumentsScope", SCOPES[Scope.SCOPE_ARGUMENTS], new Type[] {});
		METHODS[Scope.SCOPE_CGI] = new Method("cgiScope", SCOPES[Scope.SCOPE_CGI], new Type[] {});
		METHODS[Scope.SCOPE_CLIENT] = new Method("clientScope", SCOPES[Scope.SCOPE_CLIENT], new Type[] {});
		METHODS[Scope.SCOPE_COOKIE] = new Method("cookieScope", SCOPES[Scope.SCOPE_COOKIE], new Type[] {});
		METHODS[Scope.SCOPE_FORM] = new Method("formScope", SCOPES[Scope.SCOPE_FORM], new Type[] {});
		METHODS[Scope.SCOPE_LOCAL] = new Method("localGet", Types.OBJECT, new Type[] {});
		METHODS[Scope.SCOPE_REQUEST] = new Method("requestScope", SCOPES[Scope.SCOPE_REQUEST], new Type[] {});
		METHODS[Scope.SCOPE_SERVER] = new Method("serverScope", SCOPES[Scope.SCOPE_SERVER], new Type[] {});
		METHODS[Scope.SCOPE_SESSION] = new Method("sessionScope", SCOPES[Scope.SCOPE_SESSION], new Type[] {});
		METHODS[Scope.SCOPE_UNDEFINED] = new Method("us", SCOPES[Scope.SCOPE_UNDEFINED], new Type[] {});
		METHODS[Scope.SCOPE_URL] = new Method("urlScope", SCOPES[Scope.SCOPE_URL], new Type[] {});
		METHODS[Scope.SCOPE_VARIABLES] = new Method("variablesScope", SCOPES[Scope.SCOPE_VARIABLES], new Type[] {});
		METHODS[Scope.SCOPE_CLUSTER] = new Method("clusterScope", SCOPES[Scope.SCOPE_CLUSTER], new Type[] {});
		METHODS[Scope.SCOPE_VAR] = new Method("localScope", SCOPES[Scope.SCOPE_VAR], new Type[] {});
		METHODS[SCOPE_UNDEFINED_LOCAL] = new Method("usl", SCOPE, new Type[] {});
	}
	// Argument argumentsScope (boolean)
	public final static Method METHOD_ARGUMENT_BIND = new Method("argumentsScope", SCOPES[Scope.SCOPE_ARGUMENTS], new Type[] { Types.BOOLEAN_VALUE });
	public final static Method METHOD_VAR_BIND = new Method("localScope", SCOPES[ScopeSupport.SCOPE_VAR], new Type[] { Types.BOOLEAN_VALUE });

	public final static Method METHOD_LOCAL_EL = new Method("localGet", Types.OBJECT, new Type[] { Types.BOOLEAN_VALUE, Types.OBJECT });
	public final static Method METHOD_LOCAL_BIND = new Method("localGet", Types.OBJECT, new Type[] { Types.BOOLEAN_VALUE });
	public final static Method METHOD_LOCAL_TOUCH = new Method("localTouch", Types.OBJECT, new Type[] {});

	// public final static Method METHOD_THIS_BINDX=new Method("thisGet",Types.OBJECT,new
	// Type[]{Types.BOOLEAN_VALUE});
	// public final static Method METHOD_THIS_TOUCHX=new Method("thisTouch", Types.OBJECT,new Type[]{});

	public final static Type SCOPE_ARGUMENT = Type.getType(Argument.class);

	public static Type invokeScope(GeneratorAdapter adapter, int scope) {
		if (scope == SCOPE_UNDEFINED_LOCAL) {
			adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
			return invokeScope(adapter, TypeScope.METHODS[scope], Types.PAGE_CONTEXT_IMPL);
		}
		else return invokeScope(adapter, TypeScope.METHODS[scope], Types.PAGE_CONTEXT);
	}

	public static Type invokeScope(GeneratorAdapter adapter, Method m, Type type) {
		if (type == null) type = Types.PAGE_CONTEXT;
		adapter.invokeVirtual(type, m);
		return m.getReturnType();
	}

}
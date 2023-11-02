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
package lucee.runtime.tag;

import lucee.runtime.ext.tag.AppendixTag;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Caller;
import lucee.runtime.type.scope.Scope;

public abstract class CustomTag extends BodyTagTryCatchFinallyImpl implements DynamicAttributes, AppendixTag {

	protected static final Collection.Key ON_ERROR = KeyImpl.intern("onError");
	protected static final Collection.Key ON_FINALLY = KeyImpl.intern("onFinally");
	protected static final Collection.Key ON_START_TAG = KeyImpl.intern("onStartTag");
	protected static final Collection.Key ON_END_TAG = KeyImpl.intern("onEndTag");
	protected static final Collection.Key INIT = KeyImpl.intern("init");
	protected static final Collection.Key GENERATED_CONTENT = KeyImpl.intern("GENERATEDCONTENT");

	protected static final Collection.Key EXECUTION_MODE = KeyImpl.intern("EXECUTIONMODE");
	protected static final Collection.Key EXECUTE_BODY = KeyImpl.intern("EXECUTEBODY");
	protected static final Collection.Key HAS_END_TAG = KeyImpl.intern("HASENDTAG");

	protected static final Collection.Key ATTRIBUTES = KeyImpl.intern("ATTRIBUTES");
	protected static final Collection.Key CALLER = KeyImpl.intern("CALLER");
	protected static final Collection.Key THIS_TAG = KeyImpl.intern("THISTAG");

	protected StructImpl attributesScope;
	protected Caller callerScope;

	@Override
	public void doInitBody() {}

	@Override
	public final void setDynamicAttribute(String uri, String name, Object value) {
		TagUtil.setDynamicAttribute(attributesScope, KeyImpl.init(name), value, TagUtil.UPPER_CASE);
	}

	/**
	 * @return return thistag scope
	 */
	public abstract Struct getThisTagScope();

	/**
	 * @return return the caller scope
	 */
	public abstract Struct getCallerScope();

	/**
	 * @return return attributes scope
	 */
	public abstract Struct getAttributesScope();

	/**
	 * @return the variables scope
	 */
	public abstract Scope getVariablesScope();

}
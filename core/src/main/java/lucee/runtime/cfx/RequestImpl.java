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
package lucee.runtime.cfx;

import com.allaire.cfx.Query;
import com.allaire.cfx.Request;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;

/**
 * Implementation of the CFX Request Interface
 */
public final class RequestImpl implements Request {

	private static final Collection.Key QUERY = KeyConstants._query;
	private static final Collection.Key DEBUG = KeyConstants._debug;
	private Struct attributes;
	private Struct settings;
	private Query query;

	/**
	 * constructor of the class
	 * 
	 * @param pc
	 * @param attributes
	 * @throws PageException
	 */
	public RequestImpl(PageContext pc, Struct attributes) throws PageException {
		this.attributes = attributes;
		Object o = attributes.get(QUERY, null);
		String varName = Caster.toString(o, null);

		if (o != null) {
			if (varName != null) {
				this.query = new QueryWrap(Caster.toQuery(pc.getVariable(varName)));
				attributes.removeEL(QUERY);
			}
			else if (Decision.isQuery(o)) {
				this.query = new QueryWrap(Caster.toQuery(o));
				attributes.removeEL(QUERY);
			}
			else {
				throw new ApplicationException("Attribute query doesn't contain a Query or a Name of a Query");
			}
		}
	}

	/**
	 * constructor of the class
	 * 
	 * @param attributes
	 * @param query
	 * @param settings
	 */
	public RequestImpl(Struct attributes, Query query, Struct settings) {
		this.attributes = attributes;
		this.query = query;
		this.settings = settings;
	}

	@Override
	public boolean attributeExists(String key) {
		return attributes.get(key, null) != null;
	}

	@Override
	public boolean debug() {
		Object o = attributes.get(DEBUG, Boolean.FALSE);
		if (o == null) return false;
		return Caster.toBooleanValue(o, false);
	}

	@Override
	public String getAttribute(String key) {
		return getAttribute(key, "");
	}

	@Override
	public String getAttribute(String key, String defaultValue) {
		return Caster.toString(attributes.get(key, defaultValue), defaultValue);
	}

	@Override
	public String[] getAttributeList() {
		return CollectionUtil.keysAsString(attributes);
	}

	@Override
	public int getIntAttribute(String key) throws NumberFormatException {
		return getIntAttribute(key, -1);
	}

	@Override
	public int getIntAttribute(String key, int defaultValue) {
		Object o = attributes.get(key, null);
		if (o == null) return defaultValue;
		try {
			return Caster.toIntValue(o);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public Query getQuery() {
		return query;
	}

	@Override
	public String getSetting(String key) {
		return settings == null ? "" : Caster.toString(settings.get(key, ""), "");
	}

}
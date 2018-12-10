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
package com.allaire.cfx;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

/**
 * Implementation of the Debug Request
 */
public class DebugRequest implements Request {

    private final Struct attributes;
    private final Query query;
    private final Struct settings;

    /**
     * constructor of the class
     * 
     * @param attributes
     */
    public DebugRequest(final Hashtable attributes) {
	this(attributes, null, null);
    }

    /**
     * constructor of the class
     * 
     * @param attributes
     * @param query
     */
    public DebugRequest(final Hashtable attributes, final Query query) {
	this(attributes, query, null);
    }

    /**
     * constructor of the class
     * 
     * @param attributes
     * @param query
     * @param settings
     */
    public DebugRequest(final Hashtable attributes, final Query query, final Hashtable settings) {
	this.attributes = toStruct(attributes);
	this.query = query;
	this.settings = toStruct(settings);

    }

    /**
     * @see com.allaire.cfx.Request#attributeExists(java.lang.String)
     */
    @Override
    public boolean attributeExists(final String key) {
	return attributes.containsKey(key);
    }

    /**
     * @see com.allaire.cfx.Request#debug()
     */
    @Override
    public boolean debug() {
	final Object o = attributes.get("debug", Boolean.FALSE);
	return CFMLEngineFactory.getInstance().getCastUtil().toBooleanValue(o, false);
    }

    /**
     * @see com.allaire.cfx.Request#getAttribute(java.lang.String, java.lang.String)
     */
    @Override
    public String getAttribute(final String key, final String defaultValue) {
	return CFMLEngineFactory.getInstance().getCastUtil().toString(attributes.get(key, defaultValue), defaultValue);
    }

    /**
     * @see com.allaire.cfx.Request#getAttribute(java.lang.String)
     */
    @Override
    public String getAttribute(final String key) {
	return getAttribute(key, "");
    }

    /**
     * @see com.allaire.cfx.Request#getAttributeList()
     */
    @Override
    public String[] getAttributeList() {
	final Iterator<Key> it = attributes.keyIterator();
	final List<String> arr = new ArrayList<String>();
	while (it.hasNext())
	    arr.add(it.next().getString());
	return arr.toArray(new String[arr.size()]);
    }

    /**
     * @see com.allaire.cfx.Request#getIntAttribute(java.lang.String, int)
     */
    @Override
    public int getIntAttribute(final String key, final int defaultValue) {
	final Object o = attributes.get(key, null);
	if (o == null) return defaultValue;
	return (int) CFMLEngineFactory.getInstance().getCastUtil().toDoubleValue(o, defaultValue);
    }

    /**
     * @see com.allaire.cfx.Request#getIntAttribute(java.lang.String)
     */
    @Override
    public int getIntAttribute(final String key) throws NumberFormatException {
	return getIntAttribute(key, -1);
    }

    /**
     * @see com.allaire.cfx.Request#getQuery()
     */
    @Override
    public Query getQuery() {
	return query;
    }

    /**
     * @see com.allaire.cfx.Request#getSetting(java.lang.String)
     */
    @Override
    public String getSetting(final String key) {
	return settings == null ? "" : CFMLEngineFactory.getInstance().getCastUtil().toString(settings.get(key, ""), "");
    }

    /**
     * @param hashTable a Hashtable to a Struct
     * @return casted struct
     */
    private static Struct toStruct(final Hashtable hashTable) {
	if (hashTable == null) return null;

	final Enumeration e = hashTable.keys();
	final Struct sct = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
	while (e.hasMoreElements()) {
	    final Object key = e.nextElement();
	    sct.setEL(key.toString(), hashTable.get(key));
	}
	return sct;
    }
}
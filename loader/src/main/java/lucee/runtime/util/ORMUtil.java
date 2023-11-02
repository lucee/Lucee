/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.type.Struct;

public interface ORMUtil {

	/**
	 * 
	 * @param pc
	 * @param force if set to false the engine is on loaded when the configuration has changed
	 * @throws PageException
	 */
	public void resetEngine(PageContext pc, boolean force) throws PageException;

	public Property[] getIds(Property[] props);

	public Object getPropertyValue(Component cfc, String name, Object defaultValue);

	public boolean isRelated(Property prop);

	public Struct convertToSimpleMap(String paramsStr);

	public DataSource getDefaultDataSource(PageContext pc) throws PageException;

	public DataSource getDefaultDataSource(PageContext pc, DataSource defaultValue);

	public DataSource getDataSource(PageContext pc, String dsn, DataSource defaultValue);

	public DataSource getDataSource(PageContext pc, String dsn) throws PageException;

	/**
	 * if the given component has defined a datasource in the meta data, lucee is returning this
	 * datasource, otherwise the default orm datasource is returned
	 * 
	 * @param pc
	 * @param cfc
	 * @param defaultValue
	 */
	public DataSource getDataSource(PageContext pc, Component cfc, DataSource defaultValue);

	/**
	 * if the given component has defined a datasource in the meta data, lucee is returning this
	 * datasource, otherwise the default orm datasource is returned
	 * 
	 * @param pc
	 * @param cfc
	 * @return
	 * @throws PageException
	 */
	public DataSource getDataSource(PageContext pc, Component cfc) throws PageException;

	public String getDataSourceName(PageContext pc, Component cfc) throws PageException;

	public String getDataSourceName(PageContext pc, Component cfc, String defaultValue);

	public boolean equals(Component l, Component r);

	public PageException createException(ORMSession session, Component cfc, Throwable t);

	public PageException createException(ORMSession session, Component cfc, String message, String detail);
}
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
import lucee.runtime.orm.ORMExceptionUtil;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.type.Struct;

public class ORMUtilImpl implements ORMUtil {

	@Override
	public void resetEngine(PageContext pc, boolean force) throws PageException {
		lucee.runtime.orm.ORMUtil.resetEngine(pc, force);
	}

	@Override
	public Property[] getIds(Property[] props) {
		return lucee.runtime.orm.ORMUtil.getIds(props);
	}

	@Override
	public Object getPropertyValue(Component cfc, String name, Object defaultValue) {
		return lucee.runtime.orm.ORMUtil.getPropertyValue(cfc, name, defaultValue);
	}

	@Override
	public boolean isRelated(Property prop) {
		return lucee.runtime.orm.ORMUtil.isRelated(prop);
	}

	@Override
	public Struct convertToSimpleMap(String paramsStr) {
		return lucee.runtime.orm.ORMUtil.convertToSimpleMap(paramsStr);
	}

	@Override
	public DataSource getDefaultDataSource(PageContext pc) throws PageException {
		return lucee.runtime.orm.ORMUtil.getDefaultDataSource(pc);
	}

	@Override
	public DataSource getDefaultDataSource(PageContext pc, DataSource defaultValue) {
		return lucee.runtime.orm.ORMUtil.getDefaultDataSource(pc, defaultValue);
	}

	@Override
	public DataSource getDataSource(PageContext pc, String dsn, DataSource defaultValue) {
		return lucee.runtime.orm.ORMUtil.getDataSource(pc, dsn, defaultValue);
	}

	@Override
	public DataSource getDataSource(PageContext pc, String dsn) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource getDataSource(PageContext pc, Component cfc, DataSource defaultValue) {
		return lucee.runtime.orm.ORMUtil.getDataSource(pc, cfc, defaultValue);
	}

	@Override
	public DataSource getDataSource(PageContext pc, Component cfc) throws PageException {
		return lucee.runtime.orm.ORMUtil.getDataSource(pc, cfc);
	}

	@Override
	public String getDataSourceName(PageContext pc, Component cfc) throws PageException {
		return lucee.runtime.orm.ORMUtil.getDataSourceName(pc, cfc);
	}

	@Override
	public String getDataSourceName(PageContext pc, Component cfc, String defaultValue) {
		return lucee.runtime.orm.ORMUtil.getDataSourceName(pc, cfc, defaultValue);
	}

	@Override
	public boolean equals(Component l, Component r) {
		return lucee.runtime.orm.ORMUtil.equals(l, r);
	}

	@Override
	public PageException createException(ORMSession session, Component cfc, Throwable t) {
		return ORMExceptionUtil.createException(session, cfc, t);
	}

	@Override
	public PageException createException(ORMSession session, Component cfc, String message, String detail) {
		return ORMExceptionUtil.createException(session, cfc, message, detail);
	}

}
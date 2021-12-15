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
package lucee.runtime;

import lucee.runtime.dump.Dumpable;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFProperties;

public interface Interface extends Dumpable, CIObject {

	public boolean instanceOf(String type);

	public String getCallPath();

	public PageSource getPageSource();

	@Deprecated
	public Struct getMetaData(PageContext pc) throws PageException;

	public Struct getMetaData(PageContext pc, boolean ignoreCache) throws PageException;

	public Interface[] getExtends();

	public void registerUDF(Collection.Key key, UDF udf) throws PageException;

	public void registerUDF(Collection.Key key, UDFProperties props) throws PageException;

}
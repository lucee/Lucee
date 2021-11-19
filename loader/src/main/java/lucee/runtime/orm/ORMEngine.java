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
package lucee.runtime.orm;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public interface ORMEngine {

	// (CFML Compatibility Mode) is not so strict in input interpretation
	public static final int MODE_LAZY = 0;
	// more strict in input interpretation
	public static final int MODE_STRICT = 1;

	/**
	 * @return returns the label of the ORM Engine
	 */
	public String getLabel();

	public int getMode();

	public ORMSession createSession(PageContext pc) throws PageException;

	// public Object getSessionFactory(PageContext pc) throws PageException;

	public void init(PageContext pc) throws PageException;

	public ORMConfiguration getConfiguration(PageContext pc);

	public boolean reload(PageContext pc, boolean force) throws PageException;

}
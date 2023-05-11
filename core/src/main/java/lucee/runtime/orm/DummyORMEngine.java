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
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;

public class DummyORMEngine implements ORMEngine {

	private static final String HIBERNATE = "FAD1E8CB-4F45-4184-86359145767C29DE";
	private static boolean tryToInstall = true;

	@Override
	public String getLabel() {
		return "No ORM Engine Installed";
	}

	@Override
	public int getMode() {
		return ORMEngine.MODE_STRICT;
	}

	@Override
	public ORMSession createSession(PageContext pc) throws PageException {
		throw notInstalledEL();
	}

	@Override
	public void init(PageContext pc) throws PageException {
	}

	@Override
	public ORMConfiguration getConfiguration(PageContext pc) {
		throw notInstalledEL();
	}

	@Override
	public boolean reload(PageContext pc, boolean force) throws PageException {
		throw notInstalledEL();
	}

	private PageException notInstalled(PageContext pc) {

		return new ApplicationException("No ORM Engine installed!", "Check out the Extension Store in the Lucee Administrator for \"ORM\".");
	}

	private PageRuntimeException notInstalledEL() {
		return new PageRuntimeException(notInstalled(null));
	}

}
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
package coldfusion.server;

import java.util.Map;

public interface ArchiveDeployService extends Service {

	public abstract Map getArchives();

	public abstract Map getSettings();

	public abstract Map getArchive(String arg0);

	public abstract String getWorkingDirectory();

	public abstract void setWorkingDirectory(String arg0);

	public abstract void archive(String arg0, String arg1) throws ServiceException;

	// public abstract void deploy(Archive arg0) throws ServiceException;
}
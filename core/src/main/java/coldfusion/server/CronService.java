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

import java.util.Date;
import java.util.List;

public interface CronService extends Service {

	public abstract void updateTask(String arg0, String arg1, String arg2, String arg3, String arg4, Date arg5, Date arg6, Date arg7, Date arg8, String arg9, boolean arg10,
			String arg11, String arg12, String arg13, String arg14, String arg15, String arg16, boolean arg17, String arg18, String arg19) throws ServiceException;

	public abstract List listAll();

	public abstract String list();

	// public abstract CronTabEntry findTask(String arg0);

	public abstract void deleteTask(String arg0) throws ServiceException;

	public abstract void runCall(String arg0) throws ServiceException;

	public abstract void setLogFlag(boolean arg0) throws ServiceException;

	public abstract boolean getLogFlag();

	// public abstract void updateTasks(ConfigMap arg0) throws ServiceException;

	public abstract void saveCronEntries();

}
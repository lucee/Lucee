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

public interface Service {

	public static final int UNINITALIZED = 1;
	public static final int STARTING = 2;
	public static final int STARTED = 4;
	public static final int STOPPING = 8;
	public static final int STOOPED = 16;

	public abstract void start() throws ServiceException;

	public abstract void stop() throws ServiceException;

	public abstract void restart() throws ServiceException;

	public abstract int getStatus();

	public abstract ServiceMetaData getMetaData();

	public abstract Object getProperty(String arg0);

	public abstract void setProperty(String arg0, Object arg1);

	public abstract Map getResourceBundle();

}
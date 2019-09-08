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
package lucee.runtime.monitor;

import lucee.runtime.config.ConfigServer;

public interface Monitor {

	public static final short TYPE_INTERVAL = 1;
	public static final short TYPE_REQUEST = 2;
	public static final short TYPE_ACTION = 4;

	@Deprecated
	public static final short TYPE_INTERVALL = TYPE_INTERVAL;

	public void init(ConfigServer configServer, String name, boolean logEnabled);

	public short getType();

	public String getName();

	@SuppressWarnings("rawtypes")
	public Class getClazz();

	public boolean isLogEnabled();
}
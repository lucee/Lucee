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

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;

public interface GraphingService extends Service {

	public abstract Map getSettings();

	public abstract int getCacheType();

	public abstract String getCachePath();

	public abstract int getCacheSize();

	public abstract int getMaxEngines();

	public abstract String generateGraph(String arg0, int arg1, int arg2, String arg3, String arg4, String arg5, String arg6, boolean arg7);

	public abstract String generateGraph(String arg0, int arg1, int arg2, String arg3, String arg4, String arg5, String arg6);

	public abstract byte[] generateBytes(String arg0, int arg1, int arg2, String arg3, String arg4, String arg5) throws IOException;

	public abstract byte[] generateBytes(String arg0, int arg1, int arg2, String arg3, String arg4, String arg5, boolean arg6) throws IOException;

	public abstract byte[] getGraphData(String arg0, ServletContext arg1, boolean arg2) throws IOException;

	public abstract void initializeEngine(ServletContext arg0);

	public abstract void setUpWatermark();

}
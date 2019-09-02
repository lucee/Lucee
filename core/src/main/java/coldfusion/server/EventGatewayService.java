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
import java.util.Vector;

public interface EventGatewayService extends Service {

	// public abstract EventRequestDispatcher getEventRequestDispatcher();

	// public abstract EventRequestHandler getEventRequestHandler();

	// public abstract Logger getLogger();

	// public abstract Logger getLogger(String arg0);

	public abstract void startEventGateway(String arg0);

	public abstract void stopEventGateway(String arg0);

	public abstract void restartEventGateway(String arg0);

	public abstract int getEventGatewayStatus(String arg0);

	public abstract void removeGateway(String arg0);

	public abstract void registerGateway(String arg0, String arg1, String arg2, String[] arg3, String arg4);

	public abstract Vector getGateways();

	public abstract Map getGatewayInfo(String arg0);

	public abstract void removeGatewayType(String arg0);

	public abstract void registerGatewayType(String arg0, String arg1, String arg2, int arg3, boolean arg4);

	public abstract Vector getGatewayTypes();

	public abstract Map getGatewayTypeInfo(String arg0);

	public abstract void setCFCListeners(String arg0, String[] arg1);

	public abstract String[] getGatewayCFCListeners(String arg0);

	// public abstract Gateway getGateway(String arg0);

	public abstract void incrementEventsIn(String arg0);

	public abstract long getEventsIn(String arg0);

	public abstract void resetEventsIn(String arg0);

	public abstract void incrementEventsOut(String arg0);

	public abstract long getEventsOut(String arg0);

	public abstract void resetEventsOut(String arg0);

	// public abstract GatewayInfo getGatewayStats(String arg0);

}
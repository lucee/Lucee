/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.gateway;

import java.io.IOException;
import java.util.Map;

public interface Gateway {

	public static final int STARTING = 1;
	public static final int RUNNING = 2;
	public static final int STOPPING = 3;
	public static final int STOPPED = 4;
	public static final int FAILED = 5;

	/**
	 * method to initialize the gateway
	 * 
	 * @param engine the gateway engine
	 * @param id the id of the gateway
	 * @param cfcPath the path to the listener component
	 * @param config the configuration as map
	 */
	public void init(GatewayEngine engine, String id, String cfcPath, Map<String, String> config) throws IOException;

	/**
	 * returns the id of the gateway
	 * 
	 * @return the id of the gateway
	 */
	public String getId();

	/**
	 * sends a message based on given data
	 * 
	 * @param data
	 * @return answer from gateway
	 */
	public String sendMessage(Map<?, ?> data) throws IOException;

	/**
	 * return helper object
	 * 
	 * @return helper object
	 */
	public Object getHelper();

	/**
	 * starts the gateway
	 * 
	 * @throws IOException
	 */
	public void doStart() throws IOException;

	/**
	 * stop the gateway
	 * 
	 * @throws IOException
	 */
	public void doStop() throws IOException;

	/**
	 * restart the gateway
	 * 
	 * @throws IOException
	 */
	public void doRestart() throws IOException;

	/**
	 * returns a string that is used by the event gateway administrator to display status
	 * 
	 * @return status (STARTING, RSTOPPING, STOPPED, FAILED)
	 */
	public int getState();
}
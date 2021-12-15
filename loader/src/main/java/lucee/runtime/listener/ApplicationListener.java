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
package lucee.runtime.listener;

import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;

/**
 * interface for PageContext to interact with CFML
 * 
 */
public interface ApplicationListener {

	public static final int MODE_CURRENT2ROOT = 0;
	public static final int MODE_CURRENT = 1;
	public static final int MODE_ROOT = 2;
	public static final int MODE_CURRENT_OR_ROOT = 4;

	public static final int TYPE_NONE = 0;
	public static final int TYPE_CLASSIC = 1;
	public static final int TYPE_MODERN = 2;
	public static final int TYPE_MIXED = 4;

	public static final String CFC_EXTENSION = "cfc";

	public void setMode(int mode);

	public int getMode();

	/**
	 * @return the type
	 */
	public String getType();

	/**
	 * this method will be called the application self
	 * 
	 * @param pc
	 * @param requestedPage
	 * @throws PageException
	 */
	public void onRequest(PageContext pc, PageSource requestedPage, RequestListener rl) throws PageException;

	/**
	 * this method will be called when a new session starts
	 * 
	 * @throws PageException
	 */
	public void onSessionStart(PageContext pc) throws PageException;

	/**
	 * this method will be called when a session ends
	 * 
	 * @param cfmlFactory
	 * @param applicationName
	 * @param cfid
	 * @throws PageException
	 */
	public void onSessionEnd(CFMLFactory cfmlFactory, String applicationName, String cfid) throws PageException;

	/**
	 * this method will be called when a new application context starts
	 * 
	 * @throws PageException
	 */
	public boolean onApplicationStart(PageContext pc) throws PageException;

	/**
	 * this method will be called when an application scope ends
	 * 
	 * @throws PageException
	 */
	public void onApplicationEnd(CFMLFactory cfmlFactory, String applicationName) throws PageException;

	/**
	 * this method will be called when a server starts
	 * 
	 * @throws PageException
	 */
	public void onServerStart() throws PageException;

	/**
	 * this method will be called when the server shutdown correctly (no crashes)
	 * 
	 * @throws PageException
	 */
	public void onServerEnd() throws PageException;

	/**
	 * this method will be called if server has an error (exception) not thrown by a try-catch block
	 * 
	 * @param pe PageException Exception that has been thrown
	 */
	public void onError(PageContext pc, PageException pe);

	/**
	 * called after "onRequestEnd" to generate debugging output, will only be called when debugging is
	 * enabled
	 * 
	 * @throws PageException
	 */
	public void onDebug(PageContext pc) throws PageException;

	/**
	 * will be called when server is runs into a timeout
	 */
	public void onTimeout(PageContext pc);

	public boolean hasOnApplicationStart();

	public boolean hasOnSessionStart(PageContext pc);
}

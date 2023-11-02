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
package lucee.commons.io.log;

/**
 * Log Interface
 */
public interface Log {

	public static final int LEVEL_TRACE = 0;
	/**
	 * Field <code>LEVEL_INFO</code>
	 */
	public static final int LEVEL_INFO = 1;
	/**
	 * Field <code>LEVEL_DEBUG</code>
	 */
	public static final int LEVEL_DEBUG = 2;
	/**
	 * Field <code>LEVEL_WARN</code>
	 */
	public static final int LEVEL_WARN = 3;
	/**
	 * Field <code>LEVEL_ERROR</code>
	 */
	public static final int LEVEL_ERROR = 4;
	/**
	 * Field <code>LEVEL_FATAL</code>
	 */
	public static final int LEVEL_FATAL = 5;

	/**
	 * log one line
	 * 
	 * @param level level to log (Log.LEVEL_DEBUG, Log.LEVEL_WARN, Log.LEVEL_ERROR)
	 * @param application application name
	 * @param message message to log
	 */
	public void log(int level, String application, String message);

	public void log(int level, String application, String message, Throwable t);

	public void log(int level, String application, Throwable t);

	/**
	 * log level trace
	 * 
	 * @param application application name
	 * @param message message to log
	 */
	public void trace(String application, String message);

	/**
	 * log level info
	 * 
	 * @param application application name
	 * @param message message to log
	 */
	public void info(String application, String message);

	/**
	 * log level debug
	 * 
	 * @param application application name
	 * @param message message to log
	 */
	public void debug(String application, String message);

	/**
	 * log level warn
	 * 
	 * @param application application name
	 * @param message message to log
	 */
	public void warn(String application, String message);

	/**
	 * log level error
	 * 
	 * @param application application name
	 * @param message message to log
	 */
	public void error(String application, String message);

	public void error(String application, Throwable t);

	public void error(String application, String message, Throwable t);

	/**
	 * log level fatal
	 * 
	 * @param application application name
	 * @param message message to log
	 */
	public void fatal(String application, String message);

	/**
	 * @return returns the log level of the log
	 */
	public int getLogLevel();

	/**
	 * @param level sets the log level of the log
	 */
	public void setLogLevel(int level);
}
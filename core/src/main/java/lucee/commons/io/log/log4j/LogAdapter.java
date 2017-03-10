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
package lucee.commons.io.log.log4j;

import java.lang.reflect.InvocationTargetException;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

import org.apache.log4j.Logger;

public class LogAdapter implements Log {
	
	private Logger logger;

	public LogAdapter(Logger logger){
		this.logger=logger;
	}

	@Override
	public void log(int level, String application, String message) {
		logger.log(Log4jUtil.toLevel(level), application+"->"+message);
		
	}

	@Override
	public void log(int level, String application, String message, Throwable t) {
		if(StringUtil.isEmpty(message))logger.log(Log4jUtil.toLevel(level), application,t);
		else logger.log(Log4jUtil.toLevel(level), application+"->"+message,t);
	}


	@Override
	public void log(int level, String application, Throwable t) {
		logger.log(Log4jUtil.toLevel(level), application,toThrowable(t));
	}

	@Override
	public void trace(String application, String message) {
		log(Log.LEVEL_TRACE,application,message);
	}

	@Override
	public void info(String application, String message) {
		log(Log.LEVEL_INFO,application,message);
	}

	@Override
	public void debug(String application, String message) {
		log(Log.LEVEL_DEBUG,application,message);
	}

	@Override
	public void warn(String application, String message) {
		log(Log.LEVEL_WARN,application,message);
	}

	@Override
	public void error(String application, String message) {
		log(Log.LEVEL_ERROR,application,message);
	}

	@Override
	public void fatal(String application, String message) {
		log(Log.LEVEL_FATAL,application,message);
	}

	@Override
	public void error(String application, Throwable t) {
		log(LEVEL_ERROR, application, t);
	}

	@Override
	public void error(String application, String message, Throwable t) {
		log(LEVEL_ERROR, application, message, t);
	}

	@Override
	public int getLogLevel() {
		return Log4jUtil.toLevel(logger.getLevel());
	}

	@Override
	public void setLogLevel(int level) {
		logger.setLevel(Log4jUtil.toLevel(level));
	}

	public Logger getLogger() {
		return logger;
	}

	private Throwable toThrowable(Throwable t) {
		ExceptionUtil.rethrowIfNecessary(t);
		if(t instanceof InvocationTargetException) return ((InvocationTargetException)t).getTargetException();
		return t;
	}
}
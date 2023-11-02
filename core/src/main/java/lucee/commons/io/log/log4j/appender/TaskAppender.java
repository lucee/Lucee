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
package lucee.commons.io.log.log4j.appender;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import lucee.commons.io.log.log4j.appender.task.Task;
import lucee.runtime.config.Config;
import lucee.runtime.spooler.SpoolerEngine;

public class TaskAppender implements Appender, AppenderState {

	private Appender appender;
	private SpoolerEngine spoolerEngine;
	private boolean closed;

	public TaskAppender(Config config, Appender appender) {
		if (appender instanceof AppenderState) closed = ((AppenderState) appender).isClosed();
		this.appender = appender;
		spoolerEngine = config.getSpoolerEngine();

	}

	@Override
	public void doAppend(LoggingEvent le) {
		spoolerEngine.add(new Task(appender, le));
	}

	@Override
	public void addFilter(Filter arg0) {
		appender.addFilter(arg0);
	}

	@Override
	public void clearFilters() {
		appender.clearFilters();
	}

	@Override
	public void close() {
		closed = true;
		appender.close();
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return appender.getErrorHandler();
	}

	@Override
	public Filter getFilter() {
		return appender.getFilter();
	}

	@Override
	public Layout getLayout() {
		return appender.getLayout();
	}

	@Override
	public String getName() {
		return appender.getName();
	}

	@Override
	public boolean requiresLayout() {
		return appender.requiresLayout();
	}

	@Override
	public void setErrorHandler(ErrorHandler arg0) {
		appender.setErrorHandler(arg0);
	}

	@Override
	public void setLayout(Layout arg0) {
		appender.setLayout(arg0);
	}

	@Override
	public void setName(String arg0) {
		appender.setName(arg0);
	}

	@Override
	public boolean isClosed() {
		return closed;
	}
}
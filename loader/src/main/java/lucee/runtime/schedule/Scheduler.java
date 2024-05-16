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
package lucee.runtime.schedule;

import java.io.IOException;

/**
 * Scheduler interface
 */
public interface Scheduler {

	/**
	 * returns a schedule tasks matching given name or throws an exception
	 * 
	 * @param name Task name of Schedule Task to get
	 * @return matching task
	 * @throws ScheduleException Schedule Exception
	 */
	public abstract ScheduleTask getScheduleTask(String name) throws ScheduleException;

	/**
	 * returns all schedule tasks valid or not
	 * 
	 * @return all tasks
	 */
	public abstract ScheduleTask[] getAllScheduleTasks();

	/**
	 * returns a schedule tasks matching given name or null
	 * 
	 * @param name Task name of Schedule Task to get
	 * @param defaultValue default value
	 * @return matching task
	 */
	public abstract ScheduleTask getScheduleTask(String name, ScheduleTask defaultValue);

	/**
	 * Adds a Task to the scheduler
	 * 
	 * @param task task
	 * @param allowOverwrite allow overwrite
	 * @throws ScheduleException Schedule Exception
	 * @throws IOException IO Exception
	 */
	public abstract void addScheduleTask(ScheduleTask task, boolean allowOverwrite) throws ScheduleException, IOException;

	/**
	 * pause the scheduler task
	 * 
	 * @param name name of the task to pause
	 * @param pause pause
	 * @param throwWhenNotExist define if method throws an exception if task doesn't exist
	 * @throws ScheduleException Schedule Exception
	 * @throws IOException IO Exception
	 */
	public void pauseScheduleTask(String name, boolean pause, boolean throwWhenNotExist) throws ScheduleException, IOException;

	/**
	 * removes a task from scheduler
	 * 
	 * @param name name of the task to remove
	 * @param throwWhenNotExist define if method throws an exception if task doesn't exist
	 * @throws IOException IO Exception
	 * @throws ScheduleException Schedule Exception
	 */
	public abstract void removeScheduleTask(String name, boolean throwWhenNotExist) throws IOException, ScheduleException;

	/**
	 * runs a scheduler task
	 * 
	 * @param name name of task to run
	 * @param throwWhenNotExist define if method throws an exception if task doesn't exist
	 * @throws IOException IO Exception
	 * @throws ScheduleException Schedule Exception
	 */
	public abstract void runScheduleTask(String name, boolean throwWhenNotExist) throws IOException, ScheduleException;
}
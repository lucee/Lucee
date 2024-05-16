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
package lucee.runtime.spooler;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;

public interface SpoolerEngine {

	/**
	 * return the label of the engine
	 * 
	 * @return the label
	 */
	public String getLabel();

	/**
	 * adds a task to the engine
	 * 
	 * @param task task
	 */
	public void add(SpoolerTask task);

	/**
	 * remove that task from Spooler
	 * 
	 * @param task task
	 */
	public void remove(SpoolerTask task);

	/**
	 * remove a task that match given id
	 * 
	 * @param id task id
	 */
	public void remove(String id);

	/**
	 * execute task by id and return error thrown by task
	 * 
	 * @param id task id
	 * @return Exception thrown by task
	 */
	public PageException execute(String id);

	/**
	 * execute task and return error thrown by task
	 * 
	 * @param task task
	 * @return Exception thrown by task
	 */
	public PageException execute(SpoolerTask task);

	public Query getOpenTasksAsQuery(int startrow, int maxrow) throws PageException;

	public Query getClosedTasksAsQuery(int startrow, int maxrow) throws PageException;

	public Query getAllTasksAsQuery(int startrow, int maxrow) throws PageException;

	public int getOpenTaskCount();

	public int getClosedTaskCount();

	// public void setLabel(String label);
	// public void setPersisDirectory(Resource persisDirectory);
	// public void setLog(Log log);
	// public void setConfig(Config config);
}
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

import java.io.Serializable;

import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public interface SpoolerTask extends Serializable {

	public String getId();

	public void setId(String id);

	/**
	 * execute Task
	 * 
	 * @param config
	 * @throws PageException
	 */
	public Object execute(Config config) throws PageException;

	/**
	 * returns a short info to the task
	 * 
	 * @return Task subject
	 */
	public String subject();

	/**
	 * returns task type as String
	 * 
	 * @return Task subject
	 */
	public String getType();

	/**
	 * returns advanced info to the task
	 * 
	 * @return Task detail
	 */
	public Struct detail();

	/**
	 * return last execution of this task
	 * 
	 * @return last execution
	 */
	public long lastExecution();

	public void setNextExecution(long nextExecution);

	public long nextExecution();

	/**
	 * returns how many tries to send are already done
	 * 
	 * @return tries
	 */
	public int tries();

	/**
	 * @return the exceptions
	 */
	public Array getExceptions();

	public void setClosed(boolean closed);

	public boolean closed();

	/**
	 * @return the plans
	 */
	public ExecutionPlan[] getPlans();

	/**
	 * @return the creation
	 */
	public long getCreation();

	public void setLastExecution(long lastExecution);

}
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
package lucee.runtime.exp;

import lucee.runtime.config.Config;

/**
 * 
 */
public final class LockException extends PageExceptionImpl {

	/**
	 * Field <code>OPERATION_TIMEOUT</code>
	 */
	public static final String OPERATION_TIMEOUT = "Timeout";
	/**
	 * Field <code>OPERATION_MUTEX</code>
	 */
	public static final String OPERATION_MUTEX = "Mutex";
	/**
	 * Field <code>OPERATION_CREATE</code>
	 */
	public static final String OPERATION_CREATE = "Create";
	/**
	 * Field <code>OPERATION_UNKNOW</code>
	 */
	public static final String OPERATION_UNKNOW = "Unknown";

	private String lockName = "";
	private String lockOperation = "Unknown";

	/**
	 * Class Constuctor
	 * 
	 * @param operation
	 * @param name
	 * @param message error message
	 */
	public LockException(String operation, String name, String message) {
		super(message, "lock");
		this.lockName = name;
		this.lockOperation = operation;
	}

	/**
	 * Class Constuctor
	 * 
	 * @param operation
	 * @param name
	 * @param message error message
	 * @param detail detailed error message
	 */
	public LockException(String operation, String name, String message, String detail) {
		super(message, "lock");
		this.lockName = name;
		this.lockOperation = operation;
		setDetail(detail);
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		CatchBlock sct = super.getCatchBlock(config);
		sct.setEL("LockName", lockName);
		sct.setEL("LockOperation", lockOperation);
		return sct;
	}
}
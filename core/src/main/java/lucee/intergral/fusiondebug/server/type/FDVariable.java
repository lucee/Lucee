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
package lucee.intergral.fusiondebug.server.type;

import com.intergral.fusiondebug.server.IFDStackFrame;
import com.intergral.fusiondebug.server.IFDValue;
import com.intergral.fusiondebug.server.IFDVariable;

import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;

public class FDVariable implements IFDVariable {

	private Collection.Key name;
	private IFDValue value;
	private IFDStackFrame frame;

	public FDVariable(IFDStackFrame frame, String name, IFDValue value) {
		this(frame, KeyImpl.getInstance(name), value);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param value
	 * @param frame
	 */
	public FDVariable(IFDStackFrame frame, Collection.Key name, IFDValue value) {
		this.name = name;
		this.value = value;
		this.frame = frame;
	}

	@Override
	public String getName() {
		return name.getString();
	}

	@Override
	public IFDStackFrame getStackFrame() {
		return frame;
	}

	@Override
	public IFDValue getValue() {
		return value;
	}
}
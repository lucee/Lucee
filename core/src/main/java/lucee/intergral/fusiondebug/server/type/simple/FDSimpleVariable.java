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
package lucee.intergral.fusiondebug.server.type.simple;

import java.util.List;

import com.intergral.fusiondebug.server.IFDStackFrame;
import com.intergral.fusiondebug.server.IFDValue;
import com.intergral.fusiondebug.server.IFDVariable;

public class FDSimpleVariable implements IFDVariable {

	private String name;
	private IFDValue value;
	private IFDStackFrame frame;

	/**
	 * Constructor of the class
	 * 
	 * @param frame
	 * @param name
	 * @param value
	 * @param children
	 */
	public FDSimpleVariable(IFDStackFrame frame, String name, IFDValue value) {
		this.frame = frame;
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param value
	 * @param children
	 */
	public FDSimpleVariable(IFDStackFrame frame, String name, String value, List children) {
		this(frame, name, new FDSimpleValue(children, value));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IFDValue getValue() {
		return value;
	}

	@Override
	public IFDStackFrame getStackFrame() {
		return frame;
	}

}
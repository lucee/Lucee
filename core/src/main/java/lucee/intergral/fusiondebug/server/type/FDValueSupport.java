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

import java.util.List;

import com.intergral.fusiondebug.server.IFDStackFrame;
import com.intergral.fusiondebug.server.IFDValue;

import lucee.intergral.fusiondebug.server.util.FDCaster;
import lucee.runtime.op.Decision;

public abstract class FDValueSupport implements IFDValue {

	protected boolean isSimpleValue(Object value) {
		return Decision.isSimpleValue(value);
	}

	public boolean hasChildren(Object value) {
		return !isSimpleValue(value);
	}

	public List getChildren(IFDStackFrame frame, String name, Object value) {
		if (isSimpleValue(value)) return null;
		return FDCaster.toFDValue(frame, name, value).getChildren();
	}

	public abstract String getName();
}
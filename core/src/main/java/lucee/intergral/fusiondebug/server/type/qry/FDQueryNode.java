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
package lucee.intergral.fusiondebug.server.type.qry;

import com.intergral.fusiondebug.server.FDLanguageException;
import com.intergral.fusiondebug.server.FDMutabilityException;
import com.intergral.fusiondebug.server.IFDStackFrame;

import lucee.intergral.fusiondebug.server.type.FDNodeValueSupport;
import lucee.intergral.fusiondebug.server.util.FDCaster;
import lucee.runtime.type.Query;

public class FDQueryNode extends FDNodeValueSupport {

	private Query qry;
	private int row;
	private String column;

	public FDQueryNode(IFDStackFrame frame, Query qry, int row, String column) {
		super(frame);
		this.qry = qry;
		this.row = row;
		this.column = column;
	}

	@Override
	public String getName() {
		return column;
	}

	@Override
	protected Object getRawValue() {
		return qry.getAt(column, row, null);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public void set(String value) throws FDMutabilityException, FDLanguageException {
		qry.setAtEL(column, row, FDCaster.unserialize(value));
	}
}
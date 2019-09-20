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
package lucee.intergral.fusiondebug.server.type.coll;

import com.intergral.fusiondebug.server.FDMutabilityException;
import com.intergral.fusiondebug.server.IFDStackFrame;

import lucee.intergral.fusiondebug.server.type.FDNodeValueSupport;
import lucee.intergral.fusiondebug.server.util.FDCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

public class FDCollectionNode extends FDNodeValueSupport {

	private Collection coll;
	private Key key;

	/**
	 * Constructor of the class
	 * 
	 * @param coll
	 * @param key
	 */
	public FDCollectionNode(IFDStackFrame frame, Collection coll, Key key) {
		super(frame);
		this.coll = coll;
		this.key = key;
	}

	@Override
	public String getName() {
		if (coll instanceof Array) return "[" + key.getString() + "]";
		return key.getString();
	}

	@Override
	protected Object getRawValue() {
		return coll.get(key, null);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public void set(String value) throws FDMutabilityException {
		coll.setEL(key, FDCaster.unserialize(value));
	}
}
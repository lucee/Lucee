/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public class Lambda extends UDFImpl {

	public Lambda(){
		super();
	}

	public Lambda(UDFProperties properties) {
		super(properties);
	}

	@Override
	public UDF duplicate(Component c) {
		Lambda l = new Lambda(properties);
		l.ownerComponent=c;
		l.setAccess(getAccess());
		return l;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel,DumpProperties dp) {
		return UDFUtil.toDumpData(pageContext, maxlevel, dp,this,UDFUtil.TYPE_LAMBDA);
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		Struct meta = ComponentUtil.getMetaData(pc, properties);
		meta.setEL(KeyConstants._closure, Boolean.TRUE);// MUST move this to class UDFProperties
		meta.setEL("ANONYMOUSLAMBDA", Boolean.TRUE);// MUST move this to class UDFProperties
		
		return meta;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// access
		setAccess(in.readInt());
		
		// properties
		properties=(UDFPropertiesImpl) in.readObject();
	}


	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// access
		out.writeInt(getAccess());
		
		// properties
		out.writeObject(properties);
	}
}
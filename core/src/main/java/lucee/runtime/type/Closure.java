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
package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.scope.ClosureScope;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public class Closure extends UDFImpl {
	
	

	private static final long serialVersionUID = -7200106903813254844L; // do not change
	
	private Variables variables;


	public Closure(){
		super();
	}

	public Closure(UDFProperties properties) {
		super(properties);
		PageContext pc = ThreadLocalPageContext.get();
		if(pc.undefinedScope().getCheckArguments())
			this.variables=new ClosureScope(pc,pc.argumentsScope(),pc.localScope(),pc.variablesScope());
		else{
			this.variables=pc.variablesScope();
			variables.setBind(true);
		}
	}
	
	public Closure(UDFProperties properties, Variables variables) {
		super(properties);
		this.variables=variables;
		
	}

	@Override
	public UDF duplicate(Component c) {
		Closure clo = new Closure(properties,variables);// TODO duplicate variables as well?
		clo.ownerComponent=c;
		clo.setAccess(getAccess());
		return clo;
	}

	@Override
	public Object callWithNamedValues(PageContext pc,Collection.Key calledName, Struct values, boolean doIncludePath) throws PageException {
		Variables parent=pc.variablesScope();
        try{
        	pc.setVariablesScope(variables);
        	return super.callWithNamedValues(pc, calledName,values, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}
	
	@Override
	public Object callWithNamedValues(PageContext pc, Struct values, boolean doIncludePath) throws PageException {
		Variables parent=pc.variablesScope();
        try{
        	pc.setVariablesScope(variables);
        	return super.callWithNamedValues(pc, values, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public Object call(PageContext pc,Collection.Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		Variables parent=pc.variablesScope();
		try{
        	pc.setVariablesScope(variables);
			return super.call(pc, calledName, args, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public Object call(PageContext pc, Object[] args, boolean doIncludePath) throws PageException {
		Variables parent=pc.variablesScope();
		try{
        	pc.setVariablesScope(variables);
			return super.call(pc, args, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel,DumpProperties dp) {
		return UDFUtil.toDumpData(pageContext, maxlevel, dp,this,UDFUtil.TYPE_CLOSURE);
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		Struct meta = ComponentUtil.getMetaData(pc, properties);
		meta.setEL(KeyConstants._closure, Boolean.TRUE);// MUST move this to class UDFProperties
		meta.setEL("ANONYMOUSCLOSURE", Boolean.TRUE);// MUST move this to class UDFProperties
		
		return meta;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// access
		setAccess(in.readInt());
		
		// properties
		properties=(UDFPropertiesBase) in.readObject();
	}


	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// access
		out.writeInt(getAccess());
		
		// properties
		out.writeObject(properties);
	}
}
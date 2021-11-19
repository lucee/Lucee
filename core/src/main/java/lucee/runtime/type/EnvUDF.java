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

public abstract class EnvUDF extends UDFImpl {

	private static final long serialVersionUID = -7200106903813254844L; // do not change

	protected Variables variables;

	public EnvUDF() {// needed for externalize
		super();
	}

	EnvUDF(UDFProperties properties) {
		super(properties);
		PageContext pc = ThreadLocalPageContext.get();
		if (pc.undefinedScope().getCheckArguments()) {
			this.variables = new ClosureScope(pc, pc.argumentsScope(), pc.localScope(), pc.variablesScope());
		}
		else {
			this.variables = pc.variablesScope();
			variables.setBind(true);
		}
	}

	EnvUDF(UDFProperties properties, Variables variables) {
		super(properties);
		this.variables = variables;
	}

	@Override
	public UDF duplicate(Component c) {
		return _duplicate(c);
	}

	public abstract UDF _duplicate(Component c);

	@Override
	public Object callWithNamedValues(PageContext pc, Collection.Key calledName, Struct values, boolean doIncludePath) throws PageException {
		Variables parent = pc.variablesScope();
		try {
			pc.setVariablesScope(variables);
			return super.callWithNamedValues(pc, calledName, values, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Struct values, boolean doIncludePath) throws PageException {
		Variables parent = pc.variablesScope();
		try {
			pc.setVariablesScope(variables);
			return super.callWithNamedValues(pc, values, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public Object call(PageContext pc, Collection.Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		Variables parent = pc.variablesScope();
		try {
			pc.setVariablesScope(variables);
			return super.call(pc, calledName, args, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public Object call(PageContext pc, Object[] args, boolean doIncludePath) throws PageException {
		Variables parent = pc.variablesScope();
		try {
			pc.setVariablesScope(variables);
			return super.call(pc, args, doIncludePath);
		}
		finally {
			pc.setVariablesScope(parent);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		variables = (Variables) in.readObject();
		super.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(ClosureScope.prepare(variables));
		super.writeExternal(out);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return _toDumpData(pageContext, maxlevel, dp);
	}

	public abstract DumpData _toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp);

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return _getMetaData(pc);
	}

	public abstract Struct _getMetaData(PageContext pc) throws PageException;
}
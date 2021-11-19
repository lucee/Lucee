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

import java.util.ArrayList;
import java.util.List;

import com.intergral.fusiondebug.server.IFDStackFrame;

import lucee.commons.lang.StringUtil;
import lucee.intergral.fusiondebug.server.type.FDValueNotMutability;
import lucee.intergral.fusiondebug.server.type.simple.FDSimpleVariable;
import lucee.runtime.op.Caster;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.UDFUtil;

public class FDUDF extends FDValueNotMutability {

	private ArrayList children = new ArrayList();
	private String name;
	private UDF udf;

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param coll
	 */
	public FDUDF(IFDStackFrame frame, String name, UDF udf) {
		this.name = name;
		this.udf = udf;

		// meta
		List<FDSimpleVariable> list = new ArrayList<FDSimpleVariable>();
		children.add(new FDSimpleVariable(frame, "Meta Data", "", list));
		list.add(new FDSimpleVariable(frame, "Function Name", udf.getFunctionName(), null));
		if (!StringUtil.isEmpty(udf.getDisplayName())) list.add(new FDSimpleVariable(frame, "Display Name", udf.getDisplayName(), null));
		if (!StringUtil.isEmpty(udf.getDescription())) list.add(new FDSimpleVariable(frame, "Description", udf.getDescription(), null));
		if (!StringUtil.isEmpty(udf.getHint())) list.add(new FDSimpleVariable(frame, "Hint", udf.getHint(), null));
		list.add(new FDSimpleVariable(frame, "Return Type", udf.getReturnTypeAsString(), null));
		list.add(new FDSimpleVariable(frame, "Return Format", UDFUtil.toReturnFormat(udf.getReturnFormat(), "plain"), null));
		list.add(new FDSimpleVariable(frame, "Source", Caster.toString(udf.getSource()), null));
		list.add(new FDSimpleVariable(frame, "Secure Json", Caster.toString(udf.getSecureJson(), ""), null));
		list.add(new FDSimpleVariable(frame, "Verify Client", Caster.toString(udf.getVerifyClient(), ""), null));

		// arguments
		list = new ArrayList();
		List el;
		children.add(new FDSimpleVariable(frame, "Arguments", "", list));
		FunctionArgument[] args = udf.getFunctionArguments();
		for (int i = 0; i < args.length; i++) {
			el = new ArrayList();
			list.add(new FDSimpleVariable(frame, "[" + (i + 1) + "]", "", el));
			el.add(new FDSimpleVariable(frame, "Name", args[i].getName().getString(), null));
			el.add(new FDSimpleVariable(frame, "Type", args[i].getTypeAsString(), null));
			el.add(new FDSimpleVariable(frame, "Required", Caster.toString(args[i].isRequired()), null));

			if (!StringUtil.isEmpty(args[i].getDisplayName())) el.add(new FDSimpleVariable(frame, "Display Name", args[i].getDisplayName(), null));
			if (!StringUtil.isEmpty(args[i].getHint())) el.add(new FDSimpleVariable(frame, "Hint", args[i].getHint(), null));
		}

		// return
		children.add(new FDSimpleVariable(frame, "return", udf.getReturnTypeAsString(), null));
	}

	@Override
	public List getChildren() {
		return children;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public String toString() {
		return toString(udf);
	}

	public static String toString(UDF udf) {
		FunctionArgument[] args = udf.getFunctionArguments();
		StringBuffer sb = new StringBuffer("function ");
		sb.append(udf.getFunctionName());
		sb.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(args[i].getTypeAsString());
			sb.append(" ");
			sb.append(args[i].getName());
		}
		sb.append("):");
		sb.append(udf.getReturnTypeAsString());

		return sb.toString();
	}
}
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
package lucee.runtime.concurrency;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class UDFCaller extends CallerResponseStreamResult {

	private UDF udf;
	private boolean doIncludePath;
	private Object[] arguments;
	private Struct namedArguments;

	public UDFCaller(PageContext parent, UDF udf, Object[] arguments, boolean doIncludePath) {
		super(parent);
		this.udf=udf;
		this.arguments=arguments;
		this.doIncludePath=doIncludePath;
	}
	public UDFCaller(PageContext parent, UDF udf,Struct namedArguments, boolean doIncludePath) {
		super(parent);
		this.udf=udf;
		this.namedArguments=namedArguments;
		this.doIncludePath=doIncludePath;
	}

	@Override
	public void _call(PageContext parent,PageContext pc) throws PageException {
		if(namedArguments!=null) udf.callWithNamedValues(pc, namedArguments, doIncludePath);
		else udf.call(pc, arguments, doIncludePath);
	}

}
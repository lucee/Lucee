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
package lucee.transformer.bytecode.expression.var;

import lucee.runtime.db.ClassDefinition;
import lucee.transformer.Factory;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.library.function.FunctionLibFunction;

public final class BIF extends FunctionMember {

	private static String ANY = "any";

	// private ExprString nameq;
	private int argType;
	private ClassDefinition cd;
	private String returnType = ANY;
	private FunctionLibFunction flf;

	private final Factory factory;

	public final TransfomerSettings ts;

	public BIF(Factory factory, TransfomerSettings ts, FunctionLibFunction flf) {
		this.ts = ts;
		// this.name=name;
		this.flf = flf;
		this.factory = factory;// name.getFactory();
	}

	public Factory getFactory() {
		return factory;
	}

	public void setArgType(int argType) {
		this.argType = argType;
	}

	public void setClassDefinition(ClassDefinition cd) {
		this.cd = cd;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the argType
	 */
	public int getArgType() {
		return argType;
	}

	/**
	 * @return the class
	 */
	public ClassDefinition getClassDefinition() {
		return cd;
	}

	/**
	 * @return the name
	 * 
	 *         public ExprString getNameX() { return name; }
	 */

	/**
	 * @return the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @return the flf
	 */
	public FunctionLibFunction getFlf() {
		return flf;
	}

	/**
	 * @param flf the flf to set
	 */
	public void setFlf(FunctionLibFunction flf) {
		this.flf = flf;
	}
}
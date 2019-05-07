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

import java.io.Externalizable;
import java.io.Serializable;

public interface UDFProperties extends Serializable, Externalizable {

	/*
	 * public String functionName; public int returnType; public String strReturnType; public boolean
	 * output; public String hint; public String displayName; //public Page page; public PageSource
	 * pageSource; public int index; public FunctionArgument[] arguments; public Struct meta; public
	 * String description; public Boolean secureJson; public Boolean verifyClient; public boolean async;
	 * public String strReturnFormat; public int returnFormat; public Set<Collection.Key> argumentsSet;
	 * public int access;
	 */

	public int getAccess();

	public int getModifier();

}
/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.component.Member;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

/**
 * a user defined function
 * 
 */
public interface UDF extends Function, Dumpable, Member, Cloneable {

	public static final int RETURN_FORMAT_WDDX = 0;
	public static final int RETURN_FORMAT_JSON = 1;
	public static final int RETURN_FORMAT_PLAIN = 2;
	public static final int RETURN_FORMAT_SERIALIZE = 3;
	public static final int RETURN_FORMAT_XML = 4;
	public static final int RETURN_FORMAT_JAVA = 5;

	/**
	 * abstract method for the function Body
	 * 
	 * @param pageContext
	 * @throws Throwable
	 */
	public abstract Object implementation(PageContext pageContext) throws Throwable;

	/**
	 * return all function arguments of this UDF
	 * 
	 * @return the arguments.
	 */
	public abstract FunctionArgument[] getFunctionArguments();

	/**
	 * @deprecated use instead
	 *             <code> getDefaultValue(PageContext pc, int index, Object defaultValue)</code>
	 * @param pc
	 * @param index
	 * @return default value
	 * @throws PageException
	 */
	@Deprecated
	public Object getDefaultValue(PageContext pc, int index) throws PageException;

	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException;

	public int getIndex();

	/**
	 * @return Returns the functionName.
	 */
	public abstract String getFunctionName();

	/**
	 * @return Returns the output.
	 */
	public abstract boolean getOutput();

	/**
	 * @return Returns the returnType.
	 */
	public int getReturnType();

	public boolean getBufferOutput(PageContext pc);

	/**
	 * 
	 * @deprecated use instead
	 */
	@Deprecated
	public int getReturnFormat();

	public int getReturnFormat(int defaultFormat);

	/**
	 * returns null when not defined
	 * 
	 * @return value of attribute securejson
	 */
	public abstract Boolean getSecureJson();

	/**
	 * returns null when not defined
	 * 
	 * @return value of attribute verifyclient
	 */
	public abstract Boolean getVerifyClient();

	/**
	 * @return Returns the returnType.
	 */
	public abstract String getReturnTypeAsString();

	public abstract String getDescription();

	/**
	 * call user defined Function with a hashmap of named values
	 * 
	 * @param pageContext
	 * @param values named values
	 * @param doIncludePath
	 * @return return value of the function
	 * @throws PageException
	 */
	public abstract Object callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException;

	/**
	 * call user defined Function with parameters as Object Array
	 * 
	 * @param pageContext
	 * @param args parameters for the function
	 * @param doIncludePath
	 * @return return value of the function
	 * @throws PageException
	 */
	public abstract Object call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException;

	/**
	 * @return Returns the displayName.
	 */
	public abstract String getDisplayName();

	/**
	 * @return Returns the hint.
	 */
	public abstract String getHint();

	// public abstract PageSource getPageSource();

	public abstract String getSource();

	public abstract Struct getMetaData(PageContext pc) throws PageException;

	public UDF duplicate();

	/**
	 * it is the component in which this udf is constructed, must not be the same as active udf
	 * 
	 * @return owner component
	 * @deprecated
	 */
	@Deprecated
	public Component getOwnerComponent();

	/**
	 * call user defined Function with a struct
	 * 
	 * @param pageContext
	 * @param values named values
	 * @param doIncludePath
	 * @return return value of the function
	 * @throws PageException
	 */
	public abstract Object callWithNamedValues(PageContext pageContext, Collection.Key calledName, Struct values, boolean doIncludePath) throws PageException;

	/**
	 * call user defined Function with parameters as Object Array
	 * 
	 * @param pageContext
	 * @param args parameters for the function
	 * @param doIncludePath
	 * @return return value of the function
	 * @throws PageException
	 */
	public abstract Object call(PageContext pageContext, Collection.Key calledName, Object[] args, boolean doIncludePath) throws PageException;

	/**
	 * unique identifier for the function
	 * 
	 * @return
	 */
	public abstract String id();

	public PageSource getPageSource();

	// public abstract Page getPage(PageContext pc);
}
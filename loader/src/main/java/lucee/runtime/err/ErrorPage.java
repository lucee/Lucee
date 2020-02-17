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
package lucee.runtime.err;

import lucee.runtime.PageSource;

/**
 * represent an Error Page
 */
public interface ErrorPage {

	public static final short TYPE_EXCEPTION = 1;
	public static final short TYPE_REQUEST = 2;
	public static final short TYPE_VALIDATION = 4;

	/**
	 * sets the mailto attribute
	 * 
	 * @param mailto
	 */
	public abstract void setMailto(String mailto);

	/**
	 * sets the template attribute
	 * 
	 * @param template
	 */
	public abstract void setTemplate(PageSource template);

	/**
	 * sets the exception attribute
	 * 
	 * @param exception
	 * @deprecated use instead <code>setException(String exception);</code>
	 */
	@Deprecated
	public abstract void setTypeAsString(String exception);

	/**
	 * sets the exception attribute
	 * 
	 * @param exception
	 */
	public abstract void setException(String exception);

	/**
	 * @return Returns the mailto.
	 */
	public abstract String getMailto();

	/**
	 * @return Returns the template.
	 */
	public abstract PageSource getTemplate();

	/**
	 * @return Returns the exception type.
	 * @deprecated use instead <code>getException();</code>
	 */
	@Deprecated
	public abstract String getTypeAsString();

	/**
	 * @return Returns the exception type.
	 */
	public abstract String getException();

	public void setType(short type);

	public short getType();

}
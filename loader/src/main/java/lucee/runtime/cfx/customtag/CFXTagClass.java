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
package lucee.runtime.cfx.customtag;

import com.allaire.cfx.CustomTag;

import lucee.runtime.cfx.CFXTagException;

/**
 * interface for a CustomTag Class, a CustomTag Class is Base to generate a Custom Tag
 */
public interface CFXTagClass {

	/**
	 * @return return a New Instance
	 * @throws CFXTagException CFX Tag Exception
	 */
	public CustomTag newInstance() throws CFXTagException;

	/**
	 * @return returns if Tag is readOnly (for Admin)
	 */
	public boolean isReadOnly();

	/**
	 * @return returns a readonly copy of the tag
	 */
	public CFXTagClass cloneReadOnly();

	/**
	 * @return returns Type of the CFX Tag as String
	 */
	public String getDisplayType();

	/**
	 * @return returns the Source Name as String
	 */
	public String getSourceName();

	/**
	 * @return returns if tag is ok
	 */
	public boolean isValid();
}
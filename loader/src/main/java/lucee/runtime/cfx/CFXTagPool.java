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
package lucee.runtime.cfx;

import java.util.Map;

import com.allaire.cfx.CustomTag;

import lucee.runtime.cfx.customtag.CFXTagClass;

/**
 * Pool for cfx tags
 */
public interface CFXTagPool {

	/**
	 * @return Returns the classes.
	 */
	public abstract Map<String, CFXTagClass> getClasses();

	/**
	 * return custom tag that match the name
	 * 
	 * @param name custom tag name
	 * @return matching tag
	 * @throws CFXTagException CFX Tag Exception
	 */
	public CustomTag getCustomTag(String name) throws CFXTagException;

	public CFXTagClass getCFXTagClass(String name) throws CFXTagException;

	/**
	 * realese custom tag
	 * 
	 * @param ct Custom Tag
	 */
	public void releaseCustomTag(CustomTag ct);

	public void releaseTag(Object tag);

}
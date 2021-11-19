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
package lucee.runtime.exp;

import lucee.commons.lang.StringUtil;

/**
 * 
 */
public final class DeprecatedException extends ApplicationException {

	public DeprecatedException(String tagName, String attrName) {
		super(StringUtil.isEmpty(attrName) ? "the tag [" + tagName + "] is longer supported"
				: "the attribute [" + attrName + "] of the tag [" + tagName + "] is no longer supported");
	}

	public DeprecatedException(String msg) {
		super(msg);
	}
}
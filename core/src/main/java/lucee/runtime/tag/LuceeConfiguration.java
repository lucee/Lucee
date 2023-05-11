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
package lucee.runtime.tag;

import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.type.Collection;

public final class LuceeConfiguration extends BodyTagImpl implements DynamicAttributes {

	public void setDynamicAttribute(String uri, Collection.Key localName, Object value) {
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) {
	}

	@Override
	public int doEndTag() throws PageException {
		// disable debug output
		pageContext.getDebugger().setOutput(false);

		// set 404
		pageContext.getHttpServletResponse().setStatus(404);

		// reset response buffer
		pageContext.clear();

		return SKIP_PAGE;
	}
}
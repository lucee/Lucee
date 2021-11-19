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
package lucee.runtime.converter;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public final class WDDXEntityResolver implements EntityResolver {
	/**
	 * constant for the DTD File for WDDX
	 */
	public final static String WDDX = "/lucee/runtime/converter/wddx.dtd";

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(getClass().getResourceAsStream(WDDX));
	}

}
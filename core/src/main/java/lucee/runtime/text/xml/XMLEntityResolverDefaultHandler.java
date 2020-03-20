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
package lucee.runtime.text.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.net.HTTPUtil;

public class XMLEntityResolverDefaultHandler extends DefaultHandler {

	private InputSource entityRes;

	public XMLEntityResolverDefaultHandler(InputSource entityRes) {
		this.entityRes = entityRes;
	}

	@Override
	public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
		// if(entityRes!=null)print.out("resolveEntity("+(entityRes!=null)+"):"+publicID+":"+systemID);
		if (entityRes != null) return entityRes;
		try {
			// TODO user resources
			return new InputSource(IOUtil.toBufferedInputStream(HTTPUtil.toURL(systemID, HTTPUtil.ENCODED_AUTO).openStream()));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return null;
		}
	}

}
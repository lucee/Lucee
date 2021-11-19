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
package lucee.runtime.text.feed;

import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;

public class DummyDTDHandler implements DTDHandler {

	@Override
	public void notationDecl(String arg0, String arg1, String arg2) throws SAXException {
	}

	@Override
	public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3) throws SAXException {
	}

}
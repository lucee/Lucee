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
package lucee.runtime.text.xml;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EmptyNamedNodeMap implements NamedNodeMap {

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public Node getNamedItem(String name) {
		return null;
	}

	@Override
	public Node getNamedItemNS(String namespaceURI, String name) {
		return null;
	}

	@Override
	public Node item(int arg0) {
		return null;
	}

	@Override
	public Node removeNamedItem(String key) throws DOMException {
		throw new DOMException(DOMException.NOT_FOUND_ERR, "NodeMap is empty");
	}

	@Override
	public Node removeNamedItemNS(String arg0, String arg1) throws DOMException {
		throw new DOMException(DOMException.NOT_FOUND_ERR, "NodeMap is empty");
	}

	@Override
	public Node setNamedItem(Node arg0) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "NodeMap is read-only");
	}

	@Override
	public Node setNamedItemNS(Node arg0) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "NodeMap is read-only");
	}

}
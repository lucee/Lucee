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

import java.util.ArrayList;

import org.w3c.dom.Node;

/**
 * 
 */
public final class ArrayNodeList extends ArrayList<Node> implements org.w3c.dom.NodeList {

	private static final long serialVersionUID = 8355573954254967533L;

	public ArrayNodeList() {
	}

	@Override
	public int getLength() {
		return size();
	}

	@Override
	public Node item(int index) {
		return get(index);
	}
}
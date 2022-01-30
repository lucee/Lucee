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

import lucee.runtime.type.Collection;
import lucee.runtime.type.StructImpl;

public class FeedStruct extends StructImpl {

	private boolean hasAttribute;
	private String path;
	private Key inside;

	private StringBuilder content;
	private String uri;

	public FeedStruct(String path, Key inside, String uri) {
		this.path = path;
		this.inside = inside;
		this.uri = uri;
	}

	public FeedStruct() {
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param hasAttribute the hasAttribute to set
	 */
	public void setHasAttribute(boolean hasAttribute) {
		this.hasAttribute = hasAttribute;
	}

	public boolean hasAttribute() {
		return hasAttribute || !isEmpty();
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the inside
	 */
	public Key getInside() {
		return inside;
	}

	public void append(String str) {
		if (content == null) content = new StringBuilder();
		content.append(str);
	}

	public String getString() {
		if (content == null) return "";
		return content.toString();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		FeedStruct trg = new FeedStruct(path, inside, uri);
		trg.hasAttribute = hasAttribute;
		copy(this, trg, deepCopy);
		return trg;
	}

}
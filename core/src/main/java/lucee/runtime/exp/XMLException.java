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

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;


/**
 * XmL Exception
 */
public final class XMLException extends ExpressionException {

	/**
	 * constructor of the class
	 * @param message
	 */
	public XMLException(String message) {
		super(message);
	}

	/**
	 * constructor of the class
	 * @param message
	 * @param detail
	 */
	public XMLException(String message, String detail) {
		super(message, detail);
		
	}
	
    /**
     * @param e
     */
    public XMLException(SAXException e) {
		super(e.getMessage());
		this.setStackTrace(e.getStackTrace());
    }

    /**
     * @param e
     */
    public XMLException(DOMException e) {
		super(e.getMessage());
		this.setStackTrace(e.getStackTrace());
    }
}
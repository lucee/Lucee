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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ThrowingErrorHandler implements ErrorHandler {

	private boolean throwFatalError;
	private boolean throwError;
	private boolean throwWarning;

	public ThrowingErrorHandler(boolean throwFatalError, boolean throwError, boolean throwWarning) {
		this.throwFatalError = throwFatalError;
		this.throwError = throwError;
		this.throwWarning = throwWarning;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		if (throwError) throw new SAXException(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		if (throwFatalError) throw new SAXException(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (throwWarning) throw new SAXException(e);
	}

}
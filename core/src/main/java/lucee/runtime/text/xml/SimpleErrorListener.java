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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class SimpleErrorListener implements ErrorListener {

	public static final ErrorListener THROW_FATAL = new SimpleErrorListener(false, true, true);
	public static final ErrorListener THROW_ERROR = new SimpleErrorListener(false, false, true);
	public static final ErrorListener THROW_WARNING = new SimpleErrorListener(false, false, false);
	private boolean ignoreFatal;
	private boolean ignoreError;
	private boolean ignoreWarning;

	public SimpleErrorListener(boolean ignoreFatal, boolean ignoreError, boolean ignoreWarning) {
		this.ignoreFatal = ignoreFatal;
		this.ignoreError = ignoreError;
		this.ignoreWarning = ignoreWarning;
	}

	@Override
	public void error(TransformerException te) throws TransformerException {
		if (!ignoreError) throw te;
	}

	@Override
	public void fatalError(TransformerException te) throws TransformerException {
		if (!ignoreFatal) throw te;
	}

	@Override
	public void warning(TransformerException te) throws TransformerException {
		if (!ignoreWarning) throw te;
	}

}
/*
 * Copyright (c) 1999-2004 Sourceforge JACOB Project.
 * All rights reserved. Originator: Dan Adler (http://danadler.com).
 * Get more information about JACOB at http://sourceforge.net/projects/jacob-project
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
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package lucee.commons.jacob.com;

/**
 * Standard exception thrown by com jni code when there is a problem
 */
public abstract class ComException extends JacobException {

	/**
	 * COM code initializes this filed with an appropriate return code that was returned by the
	 * underlying com code
	 */
	protected int hr;
	/**
	 * No documentation is available at this time. Someone should document this field
	 */
	protected int m_helpContext;
	/**
	 * No documentation is available at this time. Someone should document this field
	 */
	protected String m_helpFile;
	/**
	 * No documentation is available at this time. Someone should document this field
	 */
	protected String m_source;

	/**
	 * constructor
	 * 
	 */
	public ComException() {
		super();
	}

	/**
	 * constructor with error code?
	 * 
	 * @param newHr ??
	 */
	public ComException(int newHr) {
		super();
		this.hr = newHr;
	}

	/**
	 * @param newHr
	 * @param description
	 */
	public ComException(int newHr, String description) {
		super(description);
		this.hr = newHr;
	}

	/**
	 * @param newHr
	 * @param source
	 * @param helpFile
	 * @param helpContext
	 */
	public ComException(int newHr, String source, String helpFile, int helpContext) {
		super();
		this.hr = newHr;
		m_source = source;
		m_helpFile = helpFile;
		m_helpContext = helpContext;
	}

	/**
	 * @param newHr
	 * @param description
	 * @param source
	 * @param helpFile
	 * @param helpContext
	 */
	public ComException(int newHr, String description, String source, String helpFile, int helpContext) {
		super(description);
		this.hr = newHr;
		m_source = source;
		m_helpFile = helpFile;
		m_helpContext = helpContext;
	}

	/**
	 * @param description
	 */
	public ComException(String description) {
		super(description);
	}

	/**
	 * @return int representation of the help context
	 */
	// Methods
	public int getHelpContext() {
		return m_helpContext;
	}

	/**
	 * @return String ??? help file
	 */
	public String getHelpFile() {
		return m_helpFile;
	}

	/**
	 * @return int hr result ??
	 */
	public int getHResult() {
		return hr;
	}

	/**
	 * @return String source ??
	 */
	public String getSource() {
		return m_source;
	}
}
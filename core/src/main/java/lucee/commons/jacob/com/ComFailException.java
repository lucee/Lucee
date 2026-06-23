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
 * COM Fail Exception class raised when there is a problem
 */
public class ComFailException extends ComException {
	/**
	 * eclipse generated to get rid of a wanring
	 */
	private static final long serialVersionUID = -266047261992987700L;

	/**
	 * Constructor
	 * 
	 * @param hrNew
	 */
	public ComFailException(int hrNew) {
		super(hrNew);
	}

	/**
	 * Constructor
	 * 
	 * @param hrNew
	 * @param message
	 */
	public ComFailException(int hrNew, String message) {
		super(hrNew, message);
	}

	/**
	 * @param hrNew
	 * @param source
	 * @param helpFile
	 * @param helpContext
	 */
	public ComFailException(int hrNew, String source, String helpFile, int helpContext) {
		super(hrNew, source, helpFile, helpContext);
	}

	/**
	 * Constructor
	 * 
	 * @param hrNew
	 * @param description
	 * @param source
	 * @param helpFile
	 * @param helpContext
	 */
	public ComFailException(int hrNew, String description, String source, String helpFile, int helpContext) {
		super(hrNew, description, source, helpFile, helpContext);
	}

	/**
	 * No argument Constructor
	 */
	public ComFailException() {
		super();
	}

	/**
	 * @param message
	 */
	public ComFailException(String message) {
		super(message);
	}
}
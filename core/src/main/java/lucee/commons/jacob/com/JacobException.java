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
 * The parent class of all Jacob exceptions. They all used to be based off of RuntimeException or
 * ComException but it was decided to base them all off of one owned by this project.
 */
public class JacobException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1637125318746002715L;

	/**
	 * Default constructor. Calls super with a "No Message Provided" string
	 */
	public JacobException() {
		super("No Message Provided");
	}

	/**
	 * standard constructor
	 * 
	 * @param message
	 */
	public JacobException(String message) {
		super(message);
	}
}

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
 * Thrown by java APIs that are not implemented either because they were never implemented or
 * because they are being deprecated This is a subclass of ComException so callers can still just
 * catch ComException.
 */
public class NotImplementedException extends JacobException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9169900832852356445L;

	/**
	 * @param description
	 */
	public NotImplementedException(String description) {
		super(description);
	}

}

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
package lucee.transformer.library.tag;

import java.io.IOException;

/**
 * Execption Klasse, welche durch die verschiedenen Klassen dieses Package geworfen werden kann.
 */
public final class TagLibException extends IOException {

	/*
	 * * Standart Konstruktor fuer die Klasse TagLibException.
	 * 
	 * @param message Fehlermeldungstext. / public TagLibException(String message) { super(message); }
	 */

	/**
	 * Standart Konstruktor fuer die Klasse TagLibException.
	 * 
	 * @param t Throwable
	 */
	public TagLibException(Throwable t) {
		initCause(t);
	}

	public TagLibException(String message) {
		super(message);
	}

}
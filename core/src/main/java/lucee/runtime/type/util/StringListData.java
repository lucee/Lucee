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
package lucee.runtime.type.util;

public class StringListData {

	public final String list;
	public final String delimiter;
	public final boolean includeEmptyFieldsx;
	public final boolean multiCharacterDelimiter;

	public StringListData(String list, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter) {
		this.list = list;
		this.delimiter = delimiter;
		this.includeEmptyFieldsx = includeEmptyFields;
		this.multiCharacterDelimiter = multiCharacterDelimiter;
	}

}
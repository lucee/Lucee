/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.transformer;

public class Position {

	public final int line;
	public final int column;
	public final int pos;

	public Position(int line, int column, int position) {
		// print.e(line+":"+column+":"+position);
		this.line = line;
		this.column = column;
		this.pos = position;
	}

	@Override
	public String toString() {
		return new StringBuilder("line:").append(line).append(";column:").append(column).append(";pos:").append(pos).toString();
	}
}
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
package lucee.runtime.sql.old;

public final class Token {

	public Token() {
	}

	@Override
	public final String toString() {
		return image;
	}

	public static final Token newToken(int i) {
		switch (i) {
		default:
			return new Token();
		}
	}

	public int kind;
	public int beginLine;
	public int beginColumn;
	public int endLine;
	public int endColumn;
	public String image;
	public Token next;
	public Token specialToken;
}
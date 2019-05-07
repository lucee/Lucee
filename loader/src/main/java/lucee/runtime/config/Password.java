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
package lucee.runtime.config;

public interface Password {
	public static final int HASHED = 1;
	public static final int HASHED_SALTED = 2;

	public static final int ORIGIN_ENCRYPTED = 3;
	public static final int ORIGIN_HASHED = 4;
	public static final int ORIGIN_HASHED_SALTED = 5;
	public static final int ORIGIN_UNKNOW = 6;

	public String getPassword();

	public String getSalt();

	public int getType();

	public int getOrigin();

	public Password isEqual(Config config, String otherPassword);
}
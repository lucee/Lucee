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
package lucee.commons.io;

import java.io.IOException;

public final class ModeUtil {

	public static final int PERM_READ = 04;
	public static final int PERM_WRITE = 02;
	public static final int PERM_EXECUTE = 01;

	public static final int ROLE_OWNER = 0100;
	public static final int ROLE_GROUP = 010;
	public static final int ROLE_WORLD = 01;

	/**
	 * translate a string mode (777 or drwxrwxrwx to an octal value)
	 * 
	 * @param strMode
	 * @return
	 */
	public static int toOctalMode(String strMode) throws IOException {
		strMode = strMode.trim().toLowerCase();
		if (strMode.length() == 9 || strMode.length() == 10) return _toOctalMode(strMode);
		if (strMode.length() <= 4 && strMode.length() > 0) return Integer.parseInt(strMode, 8);
		throw new IOException("can't translate [" + strMode + "] to a mode value");
	}

	private static int _toOctalMode(String strMode) {
		int index;
		strMode = strMode.trim().toLowerCase();
		if (strMode.length() == 9) index = 0;
		else index = 1;

		int mode = 0;

		// owner
		if ("r".equals(strMode.substring(index++, index))) mode += 0400;
		if ("w".equals(strMode.substring(index++, index))) mode += 0200;
		if ("x".equals(strMode.substring(index++, index))) mode += 0100;
		// group
		if ("r".equals(strMode.substring(index++, index))) mode += 040;
		if ("w".equals(strMode.substring(index++, index))) mode += 020;
		if ("x".equals(strMode.substring(index++, index))) mode += 010;
		// world
		if ("r".equals(strMode.substring(index++, index))) mode += 04;
		if ("w".equals(strMode.substring(index++, index))) mode += 02;
		if ("x".equals(strMode.substring(index++, index))) mode += 01;
		return mode;
	}

	/**
	 * translate an octal mode value (73) to a string representation ("111")
	 * 
	 * @param strMode
	 * @return
	 */
	public static String toStringMode(int octalMode) {
		String str = Integer.toString(octalMode, 8);
		while (str.length() < 3)
			str = "0" + str;
		return str;
	}

	/**
	 * update a string mode with another (111+222=333 or 333+111=333 or 113+202=313)
	 * 
	 * @param existing
	 * @param update
	 * @return
	 * @throws IOException
	 */
	public static String updateMode(String existing, String update) throws IOException {
		return toStringMode(updateMode(toOctalMode(existing), toOctalMode(update)));
	}

	/**
	 * update octal mode with another
	 * 
	 * @param existingOctal
	 * @param updateOctal
	 * @return
	 */
	public static int updateMode(int existingOctal, int updateOctal) {
		int tmp = existingOctal & updateOctal;
		return (existingOctal - tmp) + updateOctal;
	}

	/**
	 * check mode for a specific permission
	 * 
	 * @param role
	 * @param permission
	 * @param mode
	 * @return
	 */
	public static boolean hasPermission(int role, int permission, int mode) {
		return (mode & (role * permission)) > 0;
	}

	/**
	 * check if mode is readable for owner
	 * 
	 * @param octalMode
	 * @return
	 */
	public static boolean isReadable(int octalMode) {
		return hasPermission(ROLE_OWNER, PERM_READ, octalMode);
	}

	/**
	 * check if mode is writeable for owner
	 * 
	 * @param octalMode
	 * @return
	 */
	public static boolean isWritable(int octalMode) {
		return hasPermission(ROLE_OWNER, PERM_WRITE, octalMode);
	}

	/**
	 * check if mode is executable for owner
	 * 
	 * @param octalMode
	 * @return
	 */
	public static boolean isExecutable(int octalMode) {
		return hasPermission(ROLE_OWNER, PERM_EXECUTE, octalMode);
	}

	public static int setReadable(int octalMode, boolean value) {
		int tmp = octalMode & 0444;
		if (value) return (octalMode - tmp) + 0444;
		return octalMode - tmp;
	}

	public static int setWritable(int octalMode, boolean value) {
		int tmp = octalMode & 0222;
		if (value) return (octalMode - tmp) + 0222;
		return octalMode - tmp;
	}

	public static int setExecutable(int octalMode, boolean value) {
		int tmp = octalMode & 0111;
		if (value) return (octalMode - tmp) + 0111;
		return octalMode - tmp;
	}
}
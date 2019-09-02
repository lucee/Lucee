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
package lucee.runtime.net.ftp;

import org.apache.commons.net.ftp.FTPFile;

/**
 * 
 */
public final class FTPConstant {

	/**
	 * Field <code>TRANSFER_MODE_AUTO</code>
	 */
	public static final short TRANSFER_MODE_AUTO = 0;
	/**
	 * Field <code>TRANSFER_MODE_BINARY</code>
	 */
	public static final short TRANSFER_MODE_BINARY = 1;
	/**
	 * Field <code>TRANSFER_MODE_ASCCI</code>
	 */
	public static final short TRANSFER_MODE_ASCCI = 2;

	/**
	 * Field <code>PERMISSION_READ</code>
	 */
	public static final short PERMISSION_READ = 4;
	/**
	 * Field <code>PERMISSION_WRITE</code>
	 */
	public static final short PERMISSION_WRITE = 2;

	/**
	 * Field <code>PERMISSION_EXECUTE</code>
	 */
	public static final short PERMISSION_EXECUTE = 1;

	/**
	 * Field <code>ACCESS_WORLD</code>
	 */
	public static final short ACCESS_WORLD = 1;
	/**
	 * Field <code>ACCESS_GROUP</code>
	 */
	public static final short ACCESS_GROUP = 10;
	/**
	 * Field <code>ACCESS_USER</code>
	 */
	public static final short ACCESS_USER = 100;

	/**
	 * @param type
	 * @return file type as String
	 */
	public static String getTypeAsString(int type) {
		if (type == FTPFile.DIRECTORY_TYPE) return "directory";
		else if (type == FTPFile.SYMBOLIC_LINK_TYPE) return "link";
		else if (type == FTPFile.UNKNOWN_TYPE) return "unknown";
		else if (type == FTPFile.FILE_TYPE) return "file";

		return "unknown";
	}

	/**
	 * @param file
	 * @return permission as integer
	 */
	public static Integer getPermissionASInteger(FTPFile file) {
		int rtn = 0;
		// world
		if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION)) rtn += ACCESS_WORLD * PERMISSION_READ;
		if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION)) rtn += ACCESS_WORLD * PERMISSION_WRITE;
		if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)) rtn += ACCESS_WORLD * PERMISSION_EXECUTE;

		// group
		if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) rtn += ACCESS_GROUP * PERMISSION_READ;
		if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION)) rtn += ACCESS_GROUP * PERMISSION_WRITE;
		if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)) rtn += ACCESS_GROUP * PERMISSION_EXECUTE;

		// user
		if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) rtn += ACCESS_USER * PERMISSION_READ;
		if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) rtn += ACCESS_USER * PERMISSION_WRITE;
		if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)) rtn += ACCESS_USER * PERMISSION_EXECUTE;

		return Integer.valueOf(rtn);
	}

	public static void setPermission(FTPFile file, int mode) {

		// world
		file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, (1 & mode) > 0);
		file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, (2 & mode) > 0);
		file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, (4 & mode) > 0);

		// group
		file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, (8 & mode) > 0);
		file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, (16 & mode) > 0);
		file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, (32 & mode) > 0);

		// user
		file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, (64 & mode) > 0);
		file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, (128 & mode) > 0);
		file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, (256 & mode) > 0);
	}

	public static void setPermissionOld(FTPFile file, int mode) {
		int mu = mode / 100;
		mode = mode - mu * 100;

		int mg = mode / 10;
		mode = mode - mg * 10;

		// print.e(mu+"-"+mg+"-"+mode);

		// world
		file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, ((PERMISSION_READ) & mode) > 0);
		file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, ((PERMISSION_WRITE) & mode) > 0);
		file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, ((PERMISSION_EXECUTE) & mode) > 0);

		// group
		file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, ((PERMISSION_READ) & mg) > 0);
		file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, ((PERMISSION_WRITE) & mg) > 0);
		file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, ((PERMISSION_EXECUTE) & mg) > 0);

		// user
		file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, ((PERMISSION_READ) & mu) > 0);
		file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, ((PERMISSION_WRITE) & mu) > 0);
		file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, ((PERMISSION_EXECUTE) & mu) > 0);
	}
}
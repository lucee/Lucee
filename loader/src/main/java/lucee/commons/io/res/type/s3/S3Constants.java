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
package lucee.commons.io.res.type.s3;

public interface S3Constants {

	public static final int ACL_PUBLIC_READ = 0; //"public-read";
	public static final int ACL_PRIVATE = 1; //private
	public static final int ACL_PUBLIC_READ_WRITE = 2; //public-read-write
	public static final int ACL_AUTH_READ = 3; //authenticated-read

	public static final int STORAGE_EU = 0;//
	public static final int STORAGE_US = 1;//
	public static final int STORAGE_US_WEST = 2;//
	public static final int STORAGE_UNKNOW = -1;
	public static final String HOST = "s3.amazonaws.com";
}
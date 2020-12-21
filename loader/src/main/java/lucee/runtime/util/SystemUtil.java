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
package lucee.runtime.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;

public interface SystemUtil {

	public final int MEMORY_TYPE_ALL = 0;
	public final int MEMORY_TYPE_HEAP = 1;
	public final int MEMORY_TYPE_NON_HEAP = 2;

	public final int ARCH_UNKNOW = 0;
	public final int ARCH_32 = 32;
	public final int ARCH_64 = 64;

	public final char CHAR_DOLLAR = (char) 36;
	public final char CHAR_POUND = (char) 163;
	public final char CHAR_EURO = (char) 8364;

	public final int JAVA_VERSION_1_0 = 0;
	public final int JAVA_VERSION_1_1 = 1;
	public final int JAVA_VERSION_1_2 = 2;
	public final int JAVA_VERSION_1_3 = 3;
	public final int JAVA_VERSION_1_4 = 4;
	public final int JAVA_VERSION_1_5 = 5;
	public final int JAVA_VERSION_1_6 = 6;
	public final int JAVA_VERSION_1_7 = 7;
	public final int JAVA_VERSION_1_8 = 8;
	public final int JAVA_VERSION_1_9 = 9;
	/*
	 * FUTURE public final int JAVA_VERSION_1_10 = 10; public final int JAVA_VERSION_1_11 = 11; public
	 * final int JAVA_VERSION_1_12 = 12; public final int JAVA_VERSION_1_13 = 13; public final int
	 * JAVA_VERSION_1_14 = 14;
	 * 
	 * public final int JAVA_VERSION_9 = JAVA_VERSION_1_9; public final int JAVA_VERSION_10 =
	 * JAVA_VERSION_1_10; public final int JAVA_VERSION_11 = JAVA_VERSION_1_11; public final int
	 * JAVA_VERSION_12 = JAVA_VERSION_1_12; public final int JAVA_VERSION_13 = JAVA_VERSION_1_13; public
	 * final int JAVA_VERSION_14 = JAVA_VERSION_1_14;
	 */

	public final int OUT = 0;
	public final int ERR = 1;

	/**
	 * returns if the file system case sensitive or not
	 * 
	 * @return is the file system case sensitive or not
	 */
	public boolean isFSCaseSensitive();

	/**
	 * @return is local machine a Windows Machine
	 */
	public boolean isWindows();

	/**
	 * @return is local machine a Linux Machine
	 */
	public boolean isLinux();

	/**
	 * @return is local machine a Solaris Machine
	 */
	public boolean isSolaris();

	/**
	 * @return is local machine a Solaris Machine
	 */
	public boolean isMacOSX();

	/**
	 * @return is local machine a Unix Machine
	 */
	public boolean isUnix();

	/**
	 * @return return System directory
	 */
	public Resource getSystemDirectory();

	/**
	 * @return return running context root
	 */
	public Resource getRuningContextRoot();

	/**
	 * returns the Temp Directory of the System
	 * 
	 * @return temp directory
	 * @throws IOException
	 */
	public Resource getTempDirectory() throws IOException;

	/**
	 * returns a unique temp file (with no auto delete)
	 * 
	 * @param extension
	 * @return temp directory
	 * @throws IOException
	 */
	public Resource getTempFile(String extension, boolean touch) throws IOException;

	/**
	 * returns the Home Directory of the System
	 * 
	 * @return home directory
	 */
	public Resource getHomeDirectory();

	/**
	 * replace path placeholder with the real path, placeholders are
	 * [{temp-directory},{system-directory},{home-directory}]
	 * 
	 * @param path
	 * @return updated path
	 */
	public String parsePlaceHolder(String path);

	public String hash64b(String str);

	public String hashMd5(String str) throws IOException;

	public String hash(ServletContext sc);

	public Charset getCharset();

	public void setCharset(Charset charset);

	public String getOSSpecificLineSeparator();

	/**
	 * return the operating system architecture
	 * 
	 * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
	 */
	public int getOSArch();

	/**
	 * return the JRE (Java Runtime Engine) architecture, this can be different from the operating
	 * system architecture
	 * 
	 * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
	 */
	public int getJREArch();

	public int getAddressSize();

	public long getFreePermGenSpaceSize();

	public int getPermGenFreeSpaceAsAPercentageOfAvailable();

	public int getFreePermGenSpacePromille();

	public Query getMemoryUsageAsQuery(int type) throws PageException;

	public Struct getMemoryUsageAsStruct(int type);

	public Struct getMemoryUsageCompact(int type);

	public long getFreeBytes() throws PageException;

	public long getTotalBytes() throws PageException;

	public double getCpuUsage(long time) throws PageException;

	/**
	 * set the printer writer for System.out or System.err
	 */
	public void setPrintWriter(int type, PrintWriter pw);

	/**
	 * get the printer writer for System.out or System.err
	 * 
	 * @param type OUT or ERR
	 * @return
	 */
	public PrintWriter getPrintWriter(int type);

	public double getLoaderVersion();

	public void stop(Thread thread);

	public void stop(PageContext pc, Throwable t, Log log); // FUTURE deprecated
	// public void stop(PageContext pc, Log log); // FUTURE add

	public String getMacAddress();

	public URL getResource(Bundle bundle, String path);

	/**
	 * add resource to "java.library.path"
	 * 
	 * @param res
	 */
	public void addLibraryPath(Resource res);

}
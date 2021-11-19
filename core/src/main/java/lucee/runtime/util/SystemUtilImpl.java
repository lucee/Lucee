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
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;

import lucee.commons.digest.Hash;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;

public class SystemUtilImpl implements SystemUtil {

	@Override
	public boolean isFSCaseSensitive() {
		return lucee.commons.io.SystemUtil.isFSCaseSensitive();
	}

	@Override
	public boolean isWindows() {
		return lucee.commons.io.SystemUtil.isWindows();
	}

	@Override
	public boolean isLinux() {
		return lucee.commons.io.SystemUtil.isLinux();
	}

	@Override
	public boolean isSolaris() {
		return lucee.commons.io.SystemUtil.isSolaris();
	}

	@Override
	public boolean isMacOSX() {
		return lucee.commons.io.SystemUtil.isMacOSX();
	}

	@Override
	public boolean isUnix() {
		return lucee.commons.io.SystemUtil.isUnix();
	}

	@Override
	public Resource getSystemDirectory() {
		return lucee.commons.io.SystemUtil.getSystemDirectory();
	}

	@Override
	public Resource getRuningContextRoot() {
		return lucee.commons.io.SystemUtil.getRuningContextRoot();
	}

	@Override
	public Resource getTempDirectory() throws IOException {
		return lucee.commons.io.SystemUtil.getTempDirectory();
	}

	@Override
	public Resource getTempFile(String extension, boolean touch) throws IOException {
		return lucee.commons.io.SystemUtil.getTempFile(extension, touch);
	}

	@Override
	public Resource getHomeDirectory() {
		return lucee.commons.io.SystemUtil.getHomeDirectory();
	}

	@Override
	public String parsePlaceHolder(String path) {
		return lucee.commons.io.SystemUtil.parsePlaceHolder(path);
	}

	@Override
	public String hash64b(String str) {
		return HashUtil.create64BitHashAsString(str);
	}

	@Override
	public String hashMd5(String str) throws IOException {
		try {
			return Hash.md5(str);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String hash(ServletContext sc) {
		return lucee.commons.io.SystemUtil.hash(sc);
	}

	@Override
	public Charset getCharset() {
		return lucee.commons.io.SystemUtil.getCharset();
	}

	@Override
	public void setCharset(Charset charset) {
		lucee.commons.io.SystemUtil.setCharset(charset);
	}

	@Override
	public String getOSSpecificLineSeparator() {
		return lucee.commons.io.SystemUtil.getOSSpecificLineSeparator();
	}

	@Override
	public int getOSArch() {
		return lucee.commons.io.SystemUtil.getOSArch();
	}

	@Override
	public int getJREArch() {
		return lucee.commons.io.SystemUtil.getJREArch();
	}

	@Override
	public int getAddressSize() {
		return lucee.commons.io.SystemUtil.getAddressSize();
	}

	@Override
	public long getFreePermGenSpaceSize() {
		return lucee.commons.io.SystemUtil.getFreePermGenSpaceSize();
	}

	@Override
	public int getPermGenFreeSpaceAsAPercentageOfAvailable() {
		return lucee.commons.io.SystemUtil.getPermGenFreeSpaceAsAPercentageOfAvailable();
	}

	@Override
	public int getFreePermGenSpacePromille() {
		return lucee.commons.io.SystemUtil.getFreePermGenSpacePromille();
	}

	@Override
	public Query getMemoryUsageAsQuery(int type) throws PageException {
		return lucee.commons.io.SystemUtil.getMemoryUsageAsQuery(type);
	}

	@Override
	public Struct getMemoryUsageAsStruct(int type) {
		return lucee.commons.io.SystemUtil.getMemoryUsageAsStruct(type);
	}

	@Override
	public Struct getMemoryUsageCompact(int type) {
		return lucee.commons.io.SystemUtil.getMemoryUsageCompact(type);
	}

	@Override
	public long getFreeBytes() throws PageException {
		return lucee.commons.io.SystemUtil.getFreeBytes();
	}

	@Override
	public long getTotalBytes() throws PageException {
		return lucee.commons.io.SystemUtil.getTotalBytes();
	}

	@Override
	public double getCpuUsage(long time) throws PageException {
		return lucee.commons.io.SystemUtil.getCpuUsage(time);
	}

	@Override
	public void setPrintWriter(int type, PrintWriter pw) {
		lucee.commons.io.SystemUtil.setPrintWriter(type, pw);
	}

	@Override
	public PrintWriter getPrintWriter(int type) {
		return lucee.commons.io.SystemUtil.getPrintWriter(type);
	}

	@Override
	public double getLoaderVersion() {
		return lucee.commons.io.SystemUtil.getLoaderVersion();
	}

	@Override
	public void stop(Thread thread) {
		lucee.commons.io.SystemUtil.stop(thread);
	}

	@Override
	public void stop(PageContext pc, Throwable t, Log log) {
		// FUTURE remove argument Throwable t
		lucee.commons.io.SystemUtil.stop(pc, true);
	}

	@Override
	public String getMacAddress() {
		return lucee.commons.io.SystemUtil.getMacAddress(null);
	}

	@Override
	public URL getResource(Bundle bundle, String path) {
		return lucee.commons.io.SystemUtil.getResource(bundle, path);
	}

	@Override
	public void addLibraryPath(Resource res) {
		lucee.commons.io.SystemUtil.addLibraryPathIfNoExist(res, null);
	}

}
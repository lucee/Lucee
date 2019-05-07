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
package lucee.commons.io.res.type.ftp;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import lucee.commons.collection.MapFactory;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;

public final class FTPResourceClient extends FTPClient {

	private String workingDirectory = null;

	private final FTPConnectionData ftpConnectionData;
	private long lastAccess;
	private final Object token = new SerializableObject();
	private final Object sync = new SerializableObject();

	private final Map<String, FTPFileWrap> files = MapFactory.<String, FTPFileWrap>getConcurrentMap();
	private final int cacheTimeout;

	public FTPResourceClient(FTPConnectionData ftpConnectionData, int cacheTimeout) {
		this.ftpConnectionData = ftpConnectionData;
		this.cacheTimeout = cacheTimeout;
	}

	/**
	 * @return the ftpConnectionData
	 */
	public FTPConnectionData getFtpConnectionData() {
		return ftpConnectionData;
	}

	public void touch() {
		this.lastAccess = System.currentTimeMillis();
	}

	/**
	 * @return the lastAccess
	 */
	public long getLastAccess() {
		return lastAccess;
	}

	public Object getToken() {
		return token;
	}

	@Override
	public boolean changeWorkingDirectory(String pathname) throws IOException {
		if (StringUtil.endsWith(pathname, '/') && pathname.length() != 1) pathname = pathname.substring(0, pathname.length() - 1);

		if (pathname.equals(workingDirectory)) return true;
		workingDirectory = pathname;
		return super.changeWorkingDirectory(pathname);
	}

	public String id() {
		return ftpConnectionData.toString();
	}

	@Override
	public boolean equals(Object obj) {

		return ((FTPResourceClient) obj).id().equals(id());
	}

	public FTPFile getFTPFile(FTPResource res) throws IOException {
		String path = res.getInnerPath();
		FTPFileWrap fw = files.get(path);

		if (fw == null) {
			return createFTPFile(res);
		}
		if (fw.time + cacheTimeout < System.currentTimeMillis()) {
			files.remove(path);
			return createFTPFile(res);
		}
		return fw.file;
	}

	public void registerFTPFile(FTPResource res, FTPFile file) {
		files.put(res.getInnerPath(), new FTPFileWrap(file));
	}

	public void unregisterFTPFile(FTPResource res) {
		files.remove(res.getInnerPath());
	}

	private FTPFile createFTPFile(FTPResource res) throws IOException {
		FTPFile[] children = null;
		boolean isRoot = res.isRoot();
		String path = isRoot ? res.getInnerPath() : res.getInnerParent();

		synchronized (sync) {
			changeWorkingDirectory(path);
			children = listFiles();
		}

		if (children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				if (isRoot) {
					if (children[i].getName().equals(".")) {
						registerFTPFile(res, children[i]);
						return children[i];
					}
				}
				else {
					if (children[i].getName().equals(res.getName())) {
						registerFTPFile(res, children[i]);
						return children[i];
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean deleteFile(String pathname) throws IOException {
		files.remove(pathname);
		return super.deleteFile(pathname);
	}

	class FTPFileWrap {

		private FTPFile file;
		private long time;

		public FTPFileWrap(FTPFile file) {
			this.file = file;
			this.time = System.currentTimeMillis();
		}

	}
}
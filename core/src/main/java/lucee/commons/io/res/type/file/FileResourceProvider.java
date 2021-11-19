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
package lucee.commons.io.res.type.file;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;

public final class FileResourceProvider implements ResourceProviderPro {

	private String scheme = "file";

	private long lockTimeout = 10000;
	private boolean caseSensitive = SystemUtil.isFSCaseSensitive();
	private final ResourceLockImpl lock = new ResourceLockImpl(lockTimeout, caseSensitive);
	private Map arguments;

	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;
		this.arguments = arguments;
		if (arguments != null) {
			// lock-timeout
			String strTimeout = (String) arguments.get("lock-timeout");
			if (strTimeout != null) {
				lockTimeout = Caster.toLongValue(arguments.get("lock-timeout"), lockTimeout);
			}
		}
		lock.setLockTimeout(lockTimeout);

		return this;
	}

	/**
	 * Constructor of the class
	 */
	public FileResourceProvider() {
	}

	@Override
	public Resource getResource(String path) {
		return new FileResource(this, ResourceUtil.removeScheme("file", path));
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public void setResources(Resources resources) {
		// this.resources=resources;
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	@Override
	public boolean isAttributesSupported() {
		return SystemUtil.isWindows();
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isModeSupported() {
		return false;// SystemUtil.isUnix(); FUTURE add again
	}

	@Override
	public Map getArguments() {
		return arguments;
	}

	@Override
	public char getSeparator() {
		return File.separatorChar;
	}
}
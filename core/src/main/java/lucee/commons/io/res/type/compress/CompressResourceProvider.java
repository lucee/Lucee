/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.io.res.type.compress;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

public abstract class CompressResourceProvider implements ResourceProviderPro {

	private static final long serialVersionUID = 5930090603192203086L;

	private Resources resources;
	protected String scheme = null;
	protected boolean caseSensitive = true;
	boolean async = true;
	private long lockTimeout = 10000;
	private final ResourceLockImpl lock = new ResourceLockImpl(lockTimeout, caseSensitive);
	private Map arguments;

	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;
		if (arguments != null) {
			this.arguments = arguments;
			// case-sensitive
			String strCaseSensitive = (String) arguments.get("case-sensitive");
			if (strCaseSensitive != null) {
				caseSensitive = Caster.toBooleanValue(strCaseSensitive, true);
			}

			// sync
			String strASync = (String) arguments.get("asynchronus");
			if (strASync == null) strASync = (String) arguments.get("async");
			if (strASync != null) {
				async = Caster.toBooleanValue(strASync, true);
			}

			// lock-timeout
			String strTimeout = (String) arguments.get("lock-timeout");
			if (strTimeout != null) {
				lockTimeout = Caster.toLongValue(arguments.get("lock-timeout"), lockTimeout);
			}
		}
		lock.setLockTimeout(lockTimeout);
		lock.setCaseSensitive(caseSensitive);

		return this;
	}

	public ResourceProvider init(String scheme, boolean caseSensitive, boolean async) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;
		this.caseSensitive = caseSensitive;
		this.async = async;
		return this;
	}

	@Override
	public Resource getResource(String path) {
		path = ResourceUtil.removeScheme(scheme, path);
		int index = path.lastIndexOf('!');
		if (index != -1) {
			Resource file = toResource(path.substring(0, index));// resources.getResource(path.substring(0,index));
			try {
				return new CompressResource(this, getCompress(file), path.substring(index + 1), caseSensitive);
			}
			catch (IOException e) {
				throw ExceptionUtil.toRuntimeException(e);
			}
		}
		Resource file = toResource(path);// resources.getResource(path);
		try {
			return new CompressResource(this, getCompress(file), "/", caseSensitive);
		}
		catch (IOException e) {
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	private Resource toResource(String path) {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) {
			return ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path, true, false);
		}
		return resources.getResource(path);
	}

	public abstract Compress getCompress(Resource file) throws IOException;

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public void setResources(Resources resources) {
		this.resources = resources;
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
	public Map getArguments() {
		return arguments;
	}

	@Override
	public char getSeparator() {
		return '/';
	}
}
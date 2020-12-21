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
package lucee.commons.io.res.type.ram;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Resource Provider for ram resource
 */
public final class RamResourceProviderOld implements ResourceProviderPro {

	private String scheme = "ram";
	private RamResourceCore root;

	boolean caseSensitive = true;
	// private Resources resources;
	private long lockTimeout = 1000;
	private ResourceLockImpl lock = new ResourceLockImpl(lockTimeout, caseSensitive);
	private Map arguments;

	/**
	 * initialize ram resource
	 * 
	 * @param scheme
	 * @param arguments
	 * @return RamResource
	 */
	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;

		if (arguments != null) {
			this.arguments = arguments;
			Object oCaseSensitive = arguments.get("case-sensitive");
			if (oCaseSensitive != null) {
				caseSensitive = Caster.toBooleanValue(oCaseSensitive, true);
			}

			// lock-timeout
			Object oTimeout = arguments.get("lock-timeout");
			if (oTimeout != null) {
				lockTimeout = Caster.toLongValue(oTimeout, lockTimeout);
			}
		}
		lock.setLockTimeout(lockTimeout);
		lock.setCaseSensitive(caseSensitive);

		root = new RamResourceCore(null, RamResourceCore.TYPE_DIRECTORY, "");
		return this;
	}

	@Override
	public Resource getResource(String path) {
		path = ResourceUtil.removeScheme(scheme, path);
		return new RamResource(this, path);
	}

	/**
	 * returns core for this path if exists, otherwise return null
	 * 
	 * @param path
	 * @return core or null
	 */
	RamResourceCore getCore(String path) {
		String[] names = ListUtil.listToStringArray(path, '/');

		RamResourceCore rrc = root;
		for (int i = 0; i < names.length; i++) {
			rrc = rrc.getChild(names[i], caseSensitive);
			if (rrc == null) return null;
		}
		return rrc;
	}

	/**
	 * create a new core
	 * 
	 * @param path
	 * @param type
	 * @return created core
	 * @throws IOException
	 */
	RamResourceCore createCore(String path, int type) throws IOException {
		String[] names = ListUtil.listToStringArray(path, '/');
		RamResourceCore rrc = root;
		for (int i = 0; i < names.length - 1; i++) {
			rrc = rrc.getChild(names[i], caseSensitive);
			if (rrc == null) throw new IOException("Can't create resource [" + path + "], missing parent resource");
		}
		rrc = new RamResourceCore(rrc, type, names[names.length - 1]);
		return rrc;
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
		return true;
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isModeSupported() {
		return true;
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
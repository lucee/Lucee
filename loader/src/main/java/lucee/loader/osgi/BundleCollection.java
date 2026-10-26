/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
package lucee.loader.osgi;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;

public class BundleCollection {
	public final Bundle core;
	private final List<Bundle> slaves;
	public final Framework felix;
	private File felixCacheParentDirectory;
	private Version coreVersion;
	private Dictionary<String, String> coreHeaders;

	public BundleCollection(final Framework felix, final Bundle master, final List<Bundle> slaves) {
		this.felix = felix;
		this.core = master;
		this.slaves = new ArrayList<Bundle>();
		if (slaves != null) for (final Bundle slave: slaves)
			if (!slave.equals(master)) this.slaves.add(slave);
		this.felixCacheParentDirectory = felixCacheParentDirectory;
	}

	/**
	 * Lucee runs as one jar, the core is not a bundle of its own, the system bundle (felix) is the core.
	 * The version and the headers (Minor-Name, Built-Date, Require-Extension ...) come from the manifest of the jar.
	 */
	public BundleCollection(final Framework felix, final List<Bundle> slaves, final Version coreVersion, final Dictionary<String, String> coreHeaders) {
		this(felix, felix, slaves);
		this.coreVersion = coreVersion;
		this.coreHeaders = coreHeaders;
	}

	public Version getCoreVersion() {
		return coreVersion != null ? coreVersion : core.getVersion();
	}

	public Dictionary<String, String> getCoreHeaders() {
		return coreHeaders != null ? coreHeaders : core.getHeaders();
	}

	public Iterator<Bundle> getSlaves() {
		return slaves.iterator();
	}

	public int getSlaveCount() {
		return slaves.size();
	}

	public BundleContext getBundleContext() {
		return felix.getBundleContext();
	}
}
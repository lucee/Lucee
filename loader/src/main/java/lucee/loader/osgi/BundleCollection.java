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
package lucee.loader.osgi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class BundleCollection {
	public final Bundle core;
	private final List<Bundle> slaves;
	public final Felix felix;

	public BundleCollection(final Felix felix, final Bundle master, final List<Bundle> slaves) {
		this.felix = felix;
		this.core = master;
		this.slaves = new ArrayList<Bundle>();
		if (slaves != null) for (final Bundle slave: slaves)
			if (!slave.equals(master)) this.slaves.add(slave);
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
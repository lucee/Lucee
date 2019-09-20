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
package lucee.runtime.engine;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import lucee.loader.engine.CFMLEngine;

public class CFMLEngineActivator {

	private ServiceRegistration<?> registration;

	// @Override
	public void start(BundleContext bundleContext) throws Exception {
		registration = bundleContext.registerService(CFMLEngine.class.getName(), CFMLEngineImpl.getInstance(), null);
	}

	// @Override
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}
}
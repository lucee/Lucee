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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BundleActivatorImpl implements BundleActivator {

	@Override
	public void start(final BundleContext bc) throws Exception {
		System.out.println("++++++++++ start +++++++++");
		System.out.println(bc);
		System.out.println(bc.getBundle().getSymbolicName());
	}

	@Override
	public void stop(final BundleContext bc) throws Exception {
		System.out.println("++++++++++ stop +++++++++");
		System.out.println(bc);
		System.out.println(bc.getBundle().getSymbolicName());
	}

}
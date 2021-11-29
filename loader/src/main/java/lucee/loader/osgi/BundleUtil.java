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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.loader.util.Util;

public class BundleUtil {
	/*
	 * public static Bundle addBundlex(BundleContext context,File bundle, boolean start) throws
	 * IOException, BundleException { return addBundle(context,bundle.getAbsolutePath(),bundle,start); }
	 */

	public static Bundle addBundle(final CFMLEngineFactory factory, final BundleContext context, final File bundle, final Log log) throws IOException, BundleException {

		return addBundle(factory, context, bundle.getAbsolutePath(), new FileInputStream(bundle), true, log);
	}

	public static Bundle addBundle(final CFMLEngineFactory factory, final BundleContext context, final Resource bundle, final Log log) throws IOException, BundleException {
		return addBundle(factory, context, bundle.getAbsolutePath(), bundle.getInputStream(), true, log);
	}

	public static Bundle addBundle(final CFMLEngineFactory factory, final BundleContext context, final String path, final InputStream is, final boolean closeIS, final Log log)
			throws IOException, BundleException {

		// if possible use that feature from core, it is smarter (can also load relations)
		/*
		 * we no longer use this code, because it cause problem when the core is restarted ClassUtil cu =
		 * null; try { cu = CFMLEngineFactory.getInstance().getClassUtil(); } catch (final Throwable t) {}
		 * if (cu != null) return cu.addBundle(context, is, closeIS, true);
		 */
		if (log != null) log.debug("OSGI", "add bundle:" + path);
		else {
			// factory.log(Log.LEVEL_INFO, "add_bundle:" + bundle);
		}
		try {
			return installBundle(context, path, is);
		}
		finally {
			if (closeIS) CFMLEngineFactorySupport.closeEL(is);
		}
	}

	public static Bundle installBundle(final BundleContext context, final String path, final InputStream is) throws BundleException {
		return context.installBundle(path, is);
	}

	public static void start(final CFMLEngineFactory factory, final List<Bundle> bundles) throws BundleException {
		if (bundles == null || bundles.isEmpty()) return;

		final Iterator<Bundle> it = bundles.iterator();
		while (it.hasNext())
			start(factory, it.next());
	}

	public static void start(final CFMLEngineFactory factory, final Bundle bundle) throws BundleException {

		/*
		 * we no longer use this code, because it cause problem when the core is restarted ClassUtil cu =
		 * null; try { cu = CFMLEngineFactory.getInstance().getClassUtil(); } catch (final Throwable t) { }
		 * if (cu != null) { cu.start(bundle); return; }
		 */

		final String fh = bundle.getHeaders().get("Fragment-Host");
		if (!Util.isEmpty(fh)) {
			factory.log(Logger.LOG_INFO, "do not start [" + bundle.getSymbolicName() + "], because this is a fragment bundle for [" + fh + "]");
			return;
		}

		factory.log(Logger.LOG_INFO, "start bundle:" + bundle.getSymbolicName() + ":" + bundle.getVersion().toString());

		start(bundle, false);
	}

	@Deprecated
	public static void start(final Bundle bundle) throws BundleException {
		start(bundle, false);
	}

	public static void start(final Bundle bundle, boolean async) throws BundleException {
		bundle.start();
		if (!async) waitFor(bundle, Bundle.STARTING, Bundle.RESOLVED, Bundle.INSTALLED, 60000L);
	}

	public static void stop(final Bundle bundle, boolean async) throws BundleException {
		bundle.stop();
		if (!async) waitFor(bundle, Bundle.STOPPING, Bundle.ACTIVE, Bundle.ACTIVE, 60000L);
	}

	private static void waitFor(Bundle bundle, int action1, int action2, int action3, long timeout) throws BundleException {
		// we poll because opening a new thread is an overhead
		long start = System.currentTimeMillis();
		while (bundle.getState() == action1 || bundle.getState() == action2 || bundle.getState() == action3) {
			if ((start + timeout) < System.currentTimeMillis()) throw new BundleException("timeout [" + timeout + "] reached for action ["
					+ (action1 == Bundle.STARTING ? "starting" : "stopping") + "], bundle is still in [" + bundle.getState() + "]");
			try {
				Thread.sleep(1);
			}
			catch (InterruptedException e) {
			} // take a nap, before trying again
		}
	}

	public static void startIfNecessary(final CFMLEngineFactory factory, final Bundle bundle) throws BundleException {
		if (bundle.getState() == Bundle.ACTIVE) return;
		start(factory, bundle);
	}

	public static String bundleState(final int state, final String defaultValue) {
		switch (state) {
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.ACTIVE:
			return "ACTIVE";
		}

		return defaultValue;
	}

	public static String toFrameworkBundleParent(String str) throws BundleException {
		if (str != null) {
			str = str.trim();
			if (Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK;
			if (Constants.FRAMEWORK_BUNDLE_PARENT_APP.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_APP;
			if (Constants.FRAMEWORK_BUNDLE_PARENT_BOOT.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_BOOT;
			if (Constants.FRAMEWORK_BUNDLE_PARENT_EXT.equalsIgnoreCase(str)) return Constants.FRAMEWORK_BUNDLE_PARENT_EXT;
		}
		throw new BundleException(
				"value [" + str + "] for [" + Constants.FRAMEWORK_BUNDLE_PARENT + "] definition is invalid, " + "valid values are [" + Constants.FRAMEWORK_BUNDLE_PARENT_APP + ", "
						+ Constants.FRAMEWORK_BUNDLE_PARENT_BOOT + ", " + Constants.FRAMEWORK_BUNDLE_PARENT_EXT + ", " + Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK + "]");
	}

	public static boolean isSystemBundle(final Bundle bundle) {
		// TODO make a better implementation for this, independent of felix
		return bundle.getSymbolicName().equals("org.apache.felix.framework");
	}
}
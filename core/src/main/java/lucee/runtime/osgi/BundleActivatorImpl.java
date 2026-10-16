package lucee.runtime.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

/**
 * This class implements a simple bundle that utilizes the OSGi framework's event mechanism to
 * listen for service events. Upon receiving a service event, it prints out the event's details.
 **/
public final class BundleActivatorImpl implements BundleActivator {
	/**
	 * Implements BundleActivator.start(). Prints a message and adds itself to the bundle context as a
	 * service listener.
	 * 
	 * @param context the framework context for the bundle.
	 **/
	@Override
	public void start(BundleContext context) {
		// context.addServiceListener(this);
	}

	/**
	 * Implements BundleActivator.stop(). Prints a message and removes itself from the bundle context as
	 * a service listener.
	 * 
	 * @param context the framework context for the bundle.
	 **/
	@Override
	public void stop(BundleContext context) {
		// context.removeServiceListener(this);
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		// if (engine != null) engine.reset();
	}
}

package lucee.runtime.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import lucee.runtime.osgi.OSGiUtil.BundleDefinition;

public class StartFailedException extends Exception {

	private static final long serialVersionUID = -6268178595687225586L;

	public final BundleException bundleException;
	public final Bundle bundle;

	private BundleDefinition bd;

	public StartFailedException(BundleException bundleException, Bundle bundle) {
		this.bundleException = bundleException;
		this.bundle = bundle;
	}

	public void setBundleDefinition(BundleDefinition bd) {
		this.bd = bd;
	}

	public BundleDefinition getBundleDefinition() {
		return bd;
	}

}

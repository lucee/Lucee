package lucee.commons.net.http;

import java.io.IOException;

/**
 * the request asks for a feature the active HTTP engine does not support, for example a feature only
 * the HTTP extension provides when the built-in engine is active. This is an error in the request, not
 * a connection failure, cfhttp throws it also with throwOnError=false.
 */
public final class HTTPFeatureNotSupportedException extends IOException {

	private static final long serialVersionUID = 1L;

	private final String feature;

	public HTTPFeatureNotSupportedException(String feature) {
		super("the built-in HTTP engine does not support " + feature + ", install the HTTP extension to use it");
		this.feature = feature;
	}

	/**
	 * @return the feature that is not supported
	 */
	public String getFeature() {
		return feature;
	}
}

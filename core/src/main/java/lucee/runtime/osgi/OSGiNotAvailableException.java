package lucee.runtime.osgi;

import java.io.IOException;

/**
 * Something asks for an OSGi bundle (a class definition with bundleName/bundleVersion, an extension
 * that defines such a class, ...) but OSGi is not installed. Nothing is downloaded or resolved.
 */
public final class OSGiNotAvailableException extends IOException {

	private static final long serialVersionUID = 5170404532462235017L;

	public OSGiNotAvailableException(String message) {
		super(message);
	}
}

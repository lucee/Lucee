package lucee.commons.io.log.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * LDEV-6664 SLF4J 2.x provider routing to Lucee logging. Only ever loaded by a Maven classloader
 * (see {@link SLF4JBridge}), never by the core classloader.
 */
public final class LuceeSLF4JServiceProvider implements SLF4JServiceProvider {

	private ILoggerFactory loggerFactory;
	private IMarkerFactory markerFactory;
	private MDCAdapter mdcAdapter;

	@Override
	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	@Override
	public IMarkerFactory getMarkerFactory() {
		return markerFactory;
	}

	@Override
	public MDCAdapter getMDCAdapter() {
		return mdcAdapter;
	}

	@Override
	public String getRequestedApiVersion() {
		return "2.0.99";
	}

	@Override
	public void initialize() {
		loggerFactory = new LuceeSLF4JLoggerFactory();
		markerFactory = new BasicMarkerFactory();
		mdcAdapter = new BasicMDCAdapter();
	}
}

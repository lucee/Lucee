package lucee.commons.io.log.slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * LDEV-6664 see {@link SLF4JBridge}
 */
public final class LuceeSLF4JLoggerFactory implements ILoggerFactory {

	private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();

	@Override
	public Logger getLogger(String name) {
		return loggers.computeIfAbsent(name, LuceeSLF4JLogger::new);
	}
}

package lucee.commons.io.log;

import java.util.List;

/**
 * Holds several appenders of one logger (LDEV-6792). The list travels through the existing
 * {@code Object appender} argument of the {@link LogEngine}, so the interface does not change: a
 * logger with a single appender never gets a list, the engine only receives its own appender
 * object as before. An engine that gets an AppenderList from {@link LogEngine#getLogger} adds every
 * appender to the logger, and stops every appender in {@link LogEngine#closeAppender}.
 */
public final class AppenderList {

	private final Object[] appenders;

	/**
	 * @param appenders the appenders created by the log engine, at least one
	 */
	public AppenderList(List<Object> appenders) {
		if (appenders == null || appenders.isEmpty()) throw new IllegalArgumentException("an appender list needs at least one appender");
		this.appenders = appenders.toArray(new Object[appenders.size()]);
	}

	/**
	 * @return a copy of the appenders, in the order they are defined in the configuration
	 */
	public Object[] getAppenders() {
		return appenders.clone();
	}

	public int size() {
		return appenders.length;
	}

	/**
	 * @return the appenders of the given object, which is the list itself or a single appender
	 */
	public static Object[] unpack(Object appender) {
		if (appender instanceof AppenderList) return ((AppenderList) appender).appenders.clone();
		return new Object[] { appender };
	}
}

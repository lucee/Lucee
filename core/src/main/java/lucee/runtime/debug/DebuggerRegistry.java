package lucee.runtime.debug;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.config.ConfigImpl;

/**
 * Static registry for the debugger listener.
 * Only one debugger listener is supported at a time.
 *
 * External debugger extensions can access this via reflection:
 *
 *   Class<?> registryClass = Class.forName("lucee.runtime.debug.DebuggerRegistry");
 *   Method setListener = registryClass.getMethod("setListener", Class.forName("lucee.runtime.debug.DebuggerListener"), String.class);
 *   setListener.invoke(null, myListener, secret);
 */
public final class DebuggerRegistry {

	/** Minimum listener API version accepted. Extensions reporting below this are rejected at register time.
	 *  Set to 0 in this commit to land the mechanism without enforcement; bump to 1 when ready to reject pre-3.0.0.8 extensions. */
	public static final int MIN_LISTENER_API_VERSION = 0;

	private static volatile DebuggerListener listener;

	private DebuggerRegistry() {
		// Static utility class
	}

	/**
	 * Set the debugger listener. Requires the correct secret from LUCEE_DAP_SECRET.
	 * Only one listener is supported - setting a new one replaces the old.
	 *
	 * @param l The listener to register, or null to unregister
	 * @param secret The secret that must match LUCEE_DAP_SECRET
	 * @return true if registration succeeded, false on any failure (reason is logged but not returned)
	 * @deprecated Use {@link #register(DebuggerListener, String)} which returns the rejection reason
	 *             so the extension can surface it to the IDE. Kept for pre-3.0.0.8 extensions still
	 *             using the boolean-return reflection lookup.
	 */
	@Deprecated
	public static boolean setListener(DebuggerListener l, String secret) {
		return register(l, secret) == null;
	}

	/**
	 * Register the debugger listener. Requires the correct secret from LUCEE_DAP_SECRET.
	 * Only one listener is supported - registering a new one replaces the old.
	 *
	 * @param l The listener to register, or null to unregister
	 * @param secret The secret that must match LUCEE_DAP_SECRET
	 * @return null on success; an error message string on failure (suitable for surfacing to the IDE via DAP attach)
	 */
	public static String register(DebuggerListener l, String secret) {
		String expectedSecret = ConfigImpl.DEBUGGER_SECRET;
		if (expectedSecret == null) {
			String msg = "LUCEE_DAP_SECRET not configured on server";
			LogUtil.log(Log.LEVEL_WARN, "application", "Debugger registration rejected - " + msg);
			return msg;
		}
		if (!expectedSecret.equals(secret)) {
			String msg = "invalid secret";
			LogUtil.log(Log.LEVEL_WARN, "application", "Debugger registration rejected - " + msg);
			return msg;
		}
		if (l == null) {
			listener = null;
			LogUtil.log(Log.LEVEL_INFO, "application", "External debugger unregistered");
			return null;
		}
		// version gate: pre-3.0.0.8 extensions have no getApiVersion case in their proxy switch,
		// proxy returns null, JVM NPEs on int unbox -> treat as v0 and reject.
		int apiVersion;
		try {
			apiVersion = l.getApiVersion();
		}
		catch (NullPointerException npe) {
			apiVersion = 0;
		}
		if (apiVersion < MIN_LISTENER_API_VERSION) {
			String msg = "debugger extension reports API v" + apiVersion
				+ ", needs >= " + MIN_LISTENER_API_VERSION + " - update the extension";
			LogUtil.log(Log.LEVEL_WARN, "application", "Debugger registration rejected - " + msg);
			return msg;
		}
		listener = l;
		LogUtil.log(Log.LEVEL_INFO, "application",
			"External debugger registered [" + l.getName() + "] (API v" + apiVersion + ")");
		return null;
	}

	/**
	 * Get the current debugger listener.
	 *
	 * @return The registered listener, or null if none
	 */
	public static DebuggerListener getListener() {
		return listener;
	}

	/**
	 * Check if a debugger client is connected and ready to handle breakpoints.
	 * Returns false if no listener is registered, or if the listener is registered
	 * but no debugger client is attached.
	 *
	 * @return true if a debugger client is connected and ready
	 */
	public static boolean isClientConnected() {
		DebuggerListener l = listener;
		return l != null && l.isClientConnected();
	}
}

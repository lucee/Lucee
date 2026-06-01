package lucee.runtime.debug;

import lucee.runtime.PageContext;

/**
 * Listener interface for external debugger extensions (e.g., VS Code DAP).
 * Allows debuggers to be notified of suspend/resume events and to register breakpoints.
 *
 * Only one debugger listener is supported at a time.
 */
public interface DebuggerListener {

	/**
	 * Get the name of this debugger for logging purposes.
	 *
	 * @return A human-readable name (e.g., "debugger")
	 */
	String getName();

	/**
	 * Check if a debugger client is connected and ready to handle breakpoints.
	 * Returns false if the listener is registered but no debugger client is attached.
	 * Used by breakpoint() BIF to avoid suspending when no client can resume.
	 *
	 * @return true if a debugger client is connected and ready
	 */
	boolean isClientConnected();

	/**
	 * Called when a thread is about to suspend (before blocking).
	 * This is called on the suspended thread's stack, so the debugger should
	 * quickly record state and return - the thread will block after this returns.
	 *
	 * @param pc The PageContext that is suspending
	 * @param file The source file path (from PageSource.getDisplayPath())
	 * @param line The current line number
	 * @param label Optional label from breakpoint() BIF, may be null
	 */
	void onSuspend(PageContext pc, String file, int line, String label);

	/**
	 * Called when a thread resumes after suspension.
	 *
	 * @param pc The PageContext that is resuming
	 */
	void onResume(PageContext pc);

	/**
	 * Called when a request is about to be released (PageContext.release()).
	 * Fires near the top of release(), before scopes are nulled, so the listener
	 * can still read pc state (e.g. getRequestId() for cleanup keyed by request lifetime).
	 *
	 * @param pc The PageContext being released
	 */
	default void onRequestEnd(PageContext pc) {
		// default: no-op
	}

	/**
	 * Check if execution should suspend at the given location.
	 * Called by DebuggerExecutionLog.start() on every line execution.
	 * Must be fast - avoid synchronization if possible.
	 *
	 * The debugger can return true for breakpoints, stepping, or any other reason.
	 *
	 * @param pc The PageContext executing
	 * @param file The source file path (from PageSource.getDisplayPath())
	 * @param line The current line number
	 * @return true if execution should suspend at this location
	 */
	boolean shouldSuspend(PageContext pc, String file, int line);

	/**
	 * Called when an exception is about to be handled.
	 * The debugger can choose to suspend execution to allow inspection.
	 *
	 * @param pc The PageContext where the exception occurred
	 * @param exception The exception that was thrown
	 * @param caught true if this exception will be caught (by try/catch or error handler),
	 *               false if it's an uncaught exception that will terminate the request
	 * @return true if the debugger wants to suspend execution at this point
	 */
	default boolean onException(PageContext pc, Throwable exception, boolean caught) {
		return false; // default: don't suspend
	}

	/**
	 * Called when output is written to System.out or System.err.
	 * Only called when a DebuggerPrintStream is installed.
	 *
	 * @param text The text that was written
	 * @param isStdErr true if written to stderr, false if stdout
	 */
	default void onOutput(String text, boolean isStdErr) {
		// default: no-op
	}

	/**
	 * Get executable line numbers for a file.
	 * Triggers compilation if the file hasn't been compiled yet.
	 *
	 * @param absolutePath The absolute file path
	 * @return Array of line numbers where breakpoints can be set, or empty array if file has errors
	 */
	default int[] getExecutableLines(String absolutePath) {
		return new int[0];
	}

	/**
	 * Called when a UDF/method is entered.
	 * Allows debuggers to implement function breakpoints (break on function name).
	 * Must be fast - called on every function invocation when DEBUGGER is enabled.
	 *
	 * @param pc The PageContext executing
	 * @param functionName The function name (case as defined in source)
	 * @param componentName The component name if this is a CFC method, null for standalone UDFs
	 * @param file The source file path (from PageSource.getDisplayPath())
	 * @param startLine The first line of the function (for suspend location)
	 * @return true if the debugger wants to suspend at function entry
	 */
	default boolean onFunctionEntry(PageContext pc, String functionName, String componentName, String file, int startLine) {
		return false;
	}
}

package lucee.runtime.engine;

/**
 * Extended ExecutionLog interface with line-based position support.
 * Used by native debugger for line-level tracking.
 */
public interface ExecutionLogPro extends ExecutionLog {

	/**
	 * Whether this implementation expects the pos parameter to be line numbers (true)
	 * or character offsets (false).
	 *
	 * This affects bytecode generation - changing this requires recompilation of cfclasses.
	 *
	 * @return true if pos should be line numbers, false for character offsets
	 */
	default boolean isLineBased() {
		return false;
	}
}

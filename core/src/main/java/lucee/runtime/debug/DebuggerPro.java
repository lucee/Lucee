package lucee.runtime.debug;

import lucee.runtime.debug.Debugger;

// MAYBE not needed, if DebuggerImpl can get the right line number.....

// FUTURE move content to loader
public interface DebuggerPro extends Debugger {
	/**
	 * adds new Timer info to debug
	 * 
	 * @param label Label
	 * @param exe Execution time
	 * @param template Template
	 * @param line line number
	 * @return debug timer object
	 */
	public DebugTimer addTimer(String label, long exe, String template, int line);
	
}

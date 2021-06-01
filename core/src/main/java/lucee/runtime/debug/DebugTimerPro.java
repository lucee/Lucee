package lucee.runtime.debug;

import lucee.runtime.debug.DebugTimer;

// FUTURE move content to loader
public interface DebugTimerPro extends DebugTimer {
    /**
	 * @return the line number
	 */
	public int getLine();
}
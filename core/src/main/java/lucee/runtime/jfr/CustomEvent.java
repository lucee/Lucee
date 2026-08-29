package lucee.runtime.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import jdk.jfr.Threshold;

/**
 * Custom JFR event for application-defined events.
 * Used by jfrEmit() for instant events and jfrBegin()/jfrCommit() for duration events.
 */
@Label("Lucee Custom Event")
@Category({ "Lucee", "Application" })
@Description("Custom event from jfrEmit() or jfrBegin()/jfrCommit() BIFs")
@StackTrace(true)
@Threshold("0 ms")
public class CustomEvent extends jdk.jfr.Event {

	@Label("Event Name")
	@Description("User-defined event name")
	public String eventName;

	@Label("Category")
	@Description("User-defined category")
	public String eventCategory;

	@Label("Message")
	@Description("Event message or description")
	public String message;

	@Label("Data")
	@Description("JSON-serialized custom data")
	public String data;

	@Label("Success")
	@Description("Whether the operation completed successfully")
	public boolean success = true;

	/**
	 * Start time in nanoseconds for duration calculation.
	 * Set by JfrUtil.registerActiveEvent(), used by JfrUtil.commitEvent() for threshold checking.
	 * Not recorded in JFR (no annotation), only used internally.
	 */
	public transient long startTime = 0;
}

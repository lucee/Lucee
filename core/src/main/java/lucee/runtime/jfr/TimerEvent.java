package lucee.runtime.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import jdk.jfr.Threshold;

@Label("Lucee Timer")
@Category({ "Lucee", "Performance" })
@Description("Duration event from cftimer tag")
@StackTrace(false)
@Threshold("0 ms")
public class TimerEvent extends jdk.jfr.Event {

	@Label("Label")
	@Description("Timer label from cftimer tag")
	public String label;

	@Label("Template")
	@Description("CFML template path where timer was started")
	public String template;

	@Label("Line")
	@Description("Line number in template")
	public int line;

	@Label("Success")
	@Description("Whether the timed block completed successfully")
	public boolean success = true;

	@Label("Error Message")
	@Description("Error message if an exception occurred")
	public String errorMessage;
}

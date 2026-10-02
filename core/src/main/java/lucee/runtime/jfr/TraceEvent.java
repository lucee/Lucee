package lucee.runtime.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;

@Label("Lucee Trace")
@Category({ "Lucee", "Trace" })
@Description("Trace event from cftrace tag")
@StackTrace(true)
public class TraceEvent extends jdk.jfr.Event {

	@Label("Type")
	@Description("Trace type: INFO, DEBUG, WARN, ERROR, FATAL, TRACE")
	public String type;

	@Label("Category")
	@Description("User-defined category for grouping traces")
	public String category;

	@Label("Text")
	@Description("Trace message text")
	public String text;

	@Label("Template")
	@Description("CFML template path where trace occurred")
	public String template;

	@Label("Line")
	@Description("Line number in template")
	public int line;

	@Label("Variable")
	@Description("Variable name if specified")
	public String variable;

	@Label("Variable Value")
	@Description("Serialized variable value")
	public String variableValue;
}

package lucee.commons.io.log;

import java.util.Map;

import lucee.runtime.db.ClassDefinition;

/**
 * The definition of one appender of a logger with its layout (LDEV-6792).
 */
public final class AppenderDefinition {

	public final ClassDefinition appender;
	public final Map<String, String> appenderArgs;
	public final ClassDefinition layout;
	public final Map<String, String> layoutArgs;

	public AppenderDefinition(ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout, Map<String, String> layoutArgs) {
		this.appender = appender;
		this.appenderArgs = appenderArgs;
		this.layout = layout;
		this.layoutArgs = layoutArgs;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append(appender).append(';');
		LoggerAndSourceData.toString(sb, appenderArgs);
		sb.append(';').append(layout).append(';');
		LoggerAndSourceData.toString(sb, layoutArgs);
		return sb.toString();
	}
}

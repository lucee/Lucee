package lucee.test;

import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

public class Layout6820 extends AbstractStringLayout {
	public Layout6820() {
		super(StandardCharsets.UTF_8);
	}
	@Override
	public String toSerializable(LogEvent event) {
		return "MVNLAYOUT:" + event.getMessage().getFormattedMessage() + "\n";
	}
}

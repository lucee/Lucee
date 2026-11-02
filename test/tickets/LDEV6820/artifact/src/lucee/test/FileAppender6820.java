package lucee.test;

import java.io.FileWriter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

public class FileAppender6820 extends AbstractAppender {
	private String file;
	public FileAppender6820() {
		super("FileAppender6820", null, null, true, Property.EMPTY_ARRAY);
	}
	public void setFile(String file) { this.file = file; }
	@Override
	public void append(LogEvent event) {
		try (FileWriter fw = new FileWriter(file, true)) {
			fw.write("MVNAPPENDER:" + event.getMessage().getFormattedMessage() + "\n");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

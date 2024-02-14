package lucee.commons.io.log.log4j2.layout;

import org.apache.logging.log4j.core.LogEvent;

public class Util {
	public static Object getLoggerName(LogEvent event) {
		String name = event.getLoggerName();
		if (lucee.loader.util.Util.isEmpty(name)) {
			return "root";
		}
		if (name.startsWith("web.")) {
			int index = name.indexOf('.', 4);
			if (index != -1) name = name.substring(index + 1);
		}
		else if (name.startsWith("server.")) {
			name = name.substring(7);
		}

		return name;
	}
}

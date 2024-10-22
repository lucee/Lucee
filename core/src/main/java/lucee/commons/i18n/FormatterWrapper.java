package lucee.commons.i18n;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FormatterWrapper {
	public final DateTimeFormatter formatter;
	public int successCount;
	public final String pattern;
	public final boolean custom;
	public final short type;
	public final ZoneId zone;

	FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone) {
		this.formatter = formatter;
		this.successCount = 0;
		this.pattern = pattern;
		this.type = type;
		this.zone = zone;
		this.custom = false;
	}

	FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone, boolean custom) {
		this.formatter = formatter;
		this.successCount = 0;
		this.pattern = pattern;
		this.type = type;
		this.zone = zone;
		this.custom = custom;
	}
}
package lucee.commons.i18n;

import java.time.format.DateTimeFormatter;

public class FormatterWrapper {
	public final DateTimeFormatter formatter;
	public int successCount;

	FormatterWrapper(DateTimeFormatter formatter) {
		this.formatter = formatter;
		this.successCount = 0;
	}
}
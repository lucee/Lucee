package lucee.commons.date;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;

/**
 * Single place where Lucee turns a two-digit year into a full year.
 * 
 * By default this uses the same sliding window as java.text.SimpleDateFormat (from 80 years before
 * to 20 years after the current date, applied at year granularity). The system property
 * "lucee.date.twodigityear.pivot" (env var "LUCEE_DATE_TWODIGITYEAR_PIVOT") replaces it with a fixed
 * pivot from 0 to 100: two-digit years below the pivot become 20xx, all others 19xx.
 */
public final class TwoDigitYear {

	public static final String SYSPROP = "lucee.date.twodigityear.pivot";

	public static final int SLIDING = -1;
	private static final int DEFAULT_YEARS_BEFORE = 80;

	private static final int PIVOT;
	private static final int YEARS_BEFORE;

	static {
		PIVOT = readPivot();
		YEARS_BEFORE = readYearsBefore();
	}

	private TwoDigitYear() {
	}

	/**
	 * converts a two-digit year (0-99) to a full year, all other values are returned unchanged
	 */
	public static int toYear(int year) {
		return toYear(year, PIVOT);
	}

	/**
	 * converts a two-digit year (0-99) to a full year, using the given pivot or, if the pivot is
	 * SLIDING, the sliding window; all other values are returned unchanged
	 */
	public static int toYear(int year, int pivot) {
		if (year < 0 || year > 99) return year;
		if (pivot >= 0) return year < pivot ? 2000 + year : 1900 + year;

		// Java's window starts inside the year (current - YEARS_BEFORE) and ends inside the year 100
		// later, the two-digit year shared by both is mapped to the later one
		int first = Year.now().getValue() - YEARS_BEFORE + 1;
		int result = (first / 100) * 100 + year;
		if (result < first) result += 100;
		return result;
	}

	/**
	 * @return the configured fixed pivot, or SLIDING when the sliding window is used
	 */
	public static int getPivot() {
		return PIVOT;
	}

	/**
	 * @return how many years before the current year the sliding window starts, as read from Java
	 */
	public static int getYearsBefore() {
		return YEARS_BEFORE;
	}

	private static int readPivot() {
		String str = SystemUtil.getSystemPropOrEnvVar(SYSPROP, null);
		if (StringUtil.isEmpty(str, true)) return SLIDING;
		str = str.trim();
		if ("sliding".equalsIgnoreCase(str)) return SLIDING;
		int pivot = Caster.toIntValue(str, Integer.MIN_VALUE);
		if (pivot < 0 || pivot > 100) {
			LogUtil.log(Log.LEVEL_WARN, "application",
					"invalid value [" + str + "] for [" + SYSPROP + "], it must be a number from 0 to 100 or [sliding]; the sliding window is used instead");
			return SLIDING;
		}
		return pivot;
	}

	private static int readYearsBefore() {
		int current = Year.now().getValue();
		// read the window directly from Java
		try {
			Date start = new SimpleDateFormat("MM/dd/yy", Locale.US).get2DigitYearStart();
			int before = current - year(start);
			if (before >= 0 && before <= 100) return before;
		}
		catch (Exception e) {
			LogUtil.log("application", e);
		}

		// fallback: probe how Java maps two-digit years and take the earliest year it produces
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
			sdf.setLenient(false);
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < 100; i++) {
				Date d = sdf.parse("12/31/" + (i < 10 ? "0" + i : i), new ParsePosition(0));
				if (d == null) continue;
				min = Math.min(min, year(d));
			}
			int before = current - min;
			if (before >= 0 && before <= 100) return before;
		}
		catch (Exception e) {
			LogUtil.log("application", e);
		}
		return DEFAULT_YEARS_BEFORE;
	}

	private static int year(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).getYear();
	}
}

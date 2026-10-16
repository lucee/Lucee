/**
 * Copyright (c) 2024, Lucee Association Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package lucee.commons.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.dt.DateTime;

/**
 * Modern {@code java.time}-based implementation of {@link DateTimeUtil}.
 *
 * <p>
 * Replaces the legacy {@link JREDateTimeUtil} which relied on {@link java.util.Calendar} and
 * thread-local instances. This implementation uses {@link ZonedDateTime} directly, which is
 * thread-safe, immutable, and free of the shared-mutable-state risks of the old approach.
 *
 * <h3>Key differences from JREDateTimeUtil</h3>
 * <ul>
 * <li>No thread-local Calendar instances — ZonedDateTime is immutable and safe to construct
 * freely.</li>
 * <li>No spurious {@code synchronized} blocks — synchronization was only needed because of the
 * shared Calendar state, not the logic itself.</li>
 * <li>Week-of-year edge case (week 1 of next year in December) is handled cleanly by
 * {@link WeekFields} and {@link IsoFields}, rather than manual millisecond arithmetic.</li>
 * <li>Historical/synthetic date handling (pre-1900 Time objects) is delegated to
 * {@link DateTimeUtil#toOffsetIfNeeded(ZoneId, long)}, inherited from the base class.</li>
 * </ul>
 *
 * <h3>Caveat</h3>
 * <p>
 * Unlike {@link JREDateTimeUtil}, this implementation does NOT attempt to replicate the exact
 * legacy DST / historical timezone behavior of {@link java.util.Calendar}. If you need a
 * byte-for-byte compatible replacement for all edge cases involving pre-1970 timestamps or exotic
 * historical timezone transitions, keep using {@link JREDateTimeUtil}. For all modern dates
 * (post-1970), behavior is equivalent.
 */
public final class JavaTimeDateTimeUtil extends DateTimeUtil {

	// -------------------------------------------------------------------------
	// Singleton
	// -------------------------------------------------------------------------

	// Not exposed directly — DateTimeUtil.getInstance() controls which impl is used.
	JavaTimeDateTimeUtil() {}

	// -------------------------------------------------------------------------
	// Core conversion: components → epoch millis
	// -------------------------------------------------------------------------

	@Override
	long _toTime(TimeZone tz, int year, int month, int day, int hour, int minute, int second, int milliSecond) {
		if (tz == null) tz = ThreadLocalPageContext.getTimeZone(tz);
		return _toTime(tz.toZoneId(), year, month, day, hour, minute, second, milliSecond);
	}

	@Override
	long _toTime(ZoneId zone, int year, int month, int day, int hour, int minute, int second, int milliSecond) {
		if (zone == null) zone = ThreadLocalPageContext.getTimeZone().toZoneId();
		return ZonedDateTime.of(year, month, day, hour, minute, second, milliSecond * 1_000_000, zone).toInstant().toEpochMilli();
	}

	// -------------------------------------------------------------------------
	// Helper: DateTime → ZonedDateTime
	// -------------------------------------------------------------------------

	private ZonedDateTime toZDT(TimeZone tz, DateTime dt) {
		if (tz == null) tz = ThreadLocalPageContext.getTimeZone();
		ZoneId zone = toOffsetIfNeeded(tz.toZoneId(), dt.getTime());
		return Instant.ofEpochMilli(dt.getTime()).atZone(zone);
	}

	private ZonedDateTime toZDT(Locale locale, TimeZone tz, DateTime dt) {
		// Locale doesn't change the instant; it only matters for week-start rules.
		return toZDT(tz, dt);
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	@Override
	public int getYear(TimeZone tz, DateTime dt) {
		return toZDT(tz, dt).getYear();
	}

	@Override
	public int getMonth(TimeZone tz, DateTime dt) {
		return toZDT(tz, dt).getMonthValue();
	}

	@Override
	public int getDay(TimeZone tz, DateTime dt) {
		return toZDT(tz, dt).getDayOfMonth();
	}

	@Override
	public int getHour(TimeZone tz, DateTime dt) {
		return toZDT(tz, dt).getHour();
	}

	@Override
	public int getMinute(TimeZone tz, DateTime dt) {
		return toZDT(tz, dt).getMinute();
	}

	@Override
	public int getSecond(TimeZone tz, DateTime dt) {
		return toZDT(tz, dt).getSecond();
	}

	@Override
	public int getMilliSecond(TimeZone tz, DateTime dt) {
		// ZonedDateTime stores nanos; convert back to millis
		return toZDT(tz, dt).getNano() / 1_000_000;
	}

	// -------------------------------------------------------------------------
	// Setters — replace one field and write back to the DateTime
	// -------------------------------------------------------------------------

	@Override
	public void setYear(TimeZone tz, DateTime dt, int value) {
		dt.setTime(toZDT(tz, dt).withYear(value).toInstant().toEpochMilli());
	}

	@Override
	public void setMonth(TimeZone tz, DateTime dt, int value) {
		dt.setTime(toZDT(tz, dt).withMonth(value).toInstant().toEpochMilli());
	}

	@Override
	public void setDay(TimeZone tz, DateTime dt, int value) {
		dt.setTime(toZDT(tz, dt).withDayOfMonth(value).toInstant().toEpochMilli());
	}

	@Override
	public void setHour(TimeZone tz, DateTime dt, int value) {
		dt.setTime(toZDT(tz, dt).withHour(value).toInstant().toEpochMilli());
	}

	@Override
	public void setMinute(TimeZone tz, DateTime dt, int value) {
		dt.setTime(toZDT(tz, dt).withMinute(value).toInstant().toEpochMilli());
	}

	@Override
	public void setSecond(TimeZone tz, DateTime dt, int value) {
		dt.setTime(toZDT(tz, dt).withSecond(value).toInstant().toEpochMilli());
	}

	@Override
	public void setMilliSecond(TimeZone tz, DateTime dt, int value) {
		// Keep existing seconds; only replace the sub-second part
		ZonedDateTime zdt = toZDT(tz, dt).with(ChronoField.MILLI_OF_SECOND, value);
		dt.setTime(zdt.toInstant().toEpochMilli());
	}

	// -------------------------------------------------------------------------
	// Derived date fields
	// -------------------------------------------------------------------------

	@Override
	public int getDayOfYear(Locale locale, TimeZone tz, DateTime dt) {
		return toZDT(locale, tz, dt).getDayOfYear();
	}

	/**
	 * Returns the 1-based day-of-week relative to the locale's first day of week.
	 *
	 * <p>
	 * For example, for a Monday-first locale (most of Europe): Monday=1, Tuesday=2, …, Sunday=7. For a
	 * Sunday-first locale (US): Sunday=1, Monday=2, …, Saturday=7.
	 *
	 * <p>
	 * This is a clean replacement for the fragile arithmetic in {@link JREDateTimeUtil}.
	 */
	@Override
	public int getDayOfWeek(Locale locale, TimeZone tz, DateTime dt) {
		WeekFields weekFields = WeekFields.of(locale);
		return toZDT(locale, tz, dt).get(weekFields.dayOfWeek());
	}

	@Override
	public int getFirstDayOfMonth(TimeZone tz, DateTime dt) {
		ZonedDateTime zdt = toZDT(tz, dt);
		// Day-of-year for the 1st of the same month
		return zdt.withDayOfMonth(1).getDayOfYear();
	}

	/**
	 * Returns the locale-aware week-of-year.
	 *
	 * <p>
	 * Uses {@link WeekFields} directly, avoiding the manual December/week-1 correction that was needed
	 * in the Calendar-based implementation.
	 */
	@Override
	public int getWeekOfYear(Locale locale, TimeZone tz, DateTime dt) {
		WeekFields weekFields = WeekFields.of(locale);
		return toZDT(locale, tz, dt).get(weekFields.weekOfWeekBasedYear());
	}

	@Override
	public long getMilliSecondsInDay(TimeZone tz, long time) {
		if (tz == null) tz = ThreadLocalPageContext.getTimeZone();
		ZonedDateTime zdt = Instant.ofEpochMilli(time).atZone(tz.toZoneId());
		return (zdt.getHour() * 3_600_000L) + (zdt.getMinute() * 60_000L) + (zdt.getSecond() * 1_000L) + (zdt.getNano() / 1_000_000L);
	}

	@Override
	public int getDaysInMonth(TimeZone tz, DateTime dt) {
		ZonedDateTime zdt = toZDT(tz, dt);
		return daysInMonth(zdt.getYear(), zdt.getMonthValue());
	}

	// -------------------------------------------------------------------------
	// toString (CFML timestamp format)
	// -------------------------------------------------------------------------

	/**
	 * Produces the CFML timestamp literal, e.g. {@code {ts '2024-04-20 14:30:00'}}. Optionally appends
	 * a timezone offset, e.g. {@code +02:00}.
	 */
	@Override
	public String toString(PageContext pc, DateTime dt, TimeZone tz, Boolean addTimeZoneOffset) {
		if (tz == null) tz = ThreadLocalPageContext.getTimeZone(pc);
		ZonedDateTime zdt = Instant.ofEpochMilli(dt.getTime()).atZone(tz.toZoneId());

		StringBuilder sb = new StringBuilder(32);
		sb.append("{ts '");
		DateTimeUtil.toString(sb, zdt.getYear(), 4);
		sb.append('-');
		DateTimeUtil.toString(sb, zdt.getMonthValue(), 2);
		sb.append('-');
		DateTimeUtil.toString(sb, zdt.getDayOfMonth(), 2);
		sb.append(' ');
		DateTimeUtil.toString(sb, zdt.getHour(), 2);
		sb.append(':');
		DateTimeUtil.toString(sb, zdt.getMinute(), 2);
		sb.append(':');
		DateTimeUtil.toString(sb, zdt.getSecond(), 2);

		if (shouldAddOffset(pc, addTimeZoneOffset)) {
			appendZoneOffset(zdt, sb);
		}

		sb.append("'}");
		return sb.toString();
	}

	private boolean shouldAddOffset(PageContext pc, Boolean addTimeZoneOffset) {
		if (addTimeZoneOffset == Boolean.FALSE) return false;
		if (addTimeZoneOffset == Boolean.TRUE) return true;
		// null — defer to page context setting
		if (pc instanceof PageContextImpl) {
			return ((PageContextImpl) pc).getTimestampWithTSOffset();
		}
		return false;
	}

	/**
	 * Appends a ±HH:mm offset derived from the ZonedDateTime's offset, e.g. {@code +02:00}.
	 */
	private void appendZoneOffset(ZonedDateTime zdt, StringBuilder sb) {
		int totalSeconds = zdt.getOffset().getTotalSeconds();
		char sign = totalSeconds < 0 ? '-' : '+';
		int absSec = Math.abs(totalSeconds);
		int hours = absSec / 3600;
		int minutes = (absSec % 3600) / 60;

		sb.append(sign);
		DateTimeUtil.toString(sb, hours, 2);
		sb.append(':');
		DateTimeUtil.toString(sb, minutes, 2);
	}
}
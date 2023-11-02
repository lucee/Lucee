package lucee.runtime.functions.dateTime;

import java.util.TimeZone;

import lucee.commons.date.DateTimeUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

public class SetMinute extends BIF {

	private static final long serialVersionUID = -6903969643282438979L;

	public static DateTime call(PageContext pc, DateTime date, double value) {
		return _call(date, (int) value, pc.getTimeZone());
	}

	public static DateTime call(PageContext pc, DateTime date, double value, TimeZone tz) {
		return _call(date, (int) value, tz == null ? pc.getTimeZone() : tz);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 2 || args.length > 3) throw new FunctionException(pc, "SetMinute", 2, 3, args.length);

		TimeZone tz = args.length == 3 ? Caster.toTimeZone(args[2], pc.getTimeZone()) : pc.getTimeZone();
		return _call(Caster.toDate(args[0], tz), Caster.toIntValue(args[1]), tz);

	}

	private static DateTime _call(DateTime date, int value, TimeZone tz) {
		DateTimeUtil.getInstance().setMinute(tz, date, value);
		return date;
	}

}

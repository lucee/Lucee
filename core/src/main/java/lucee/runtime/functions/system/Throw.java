package lucee.runtime.functions.system;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.CustomTypeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

// LDEV-6282: Java BIF replacing the CFML wrapper (throw.cfm added a UDF frame, polluting debugger scope view).
public final class Throw extends BIF {

	private static final long serialVersionUID = 1L;
	private static final int LEVEL = 1;

	public static String call(PageContext pc) throws PageException {
		return _call(pc, null, "application", "", "", "", null, null);
	}

	public static String call(PageContext pc, Object message) throws PageException {
		return _call(pc, message, "application", "", "", "", null, null);
	}

	public static String call(PageContext pc, Object message, String type) throws PageException {
		return _call(pc, message, type, "", "", "", null, null);
	}

	public static String call(PageContext pc, Object message, String type, String detail) throws PageException {
		return _call(pc, message, type, detail, "", "", null, null);
	}

	public static String call(PageContext pc, Object message, String type, String detail, String errorcode) throws PageException {
		return _call(pc, message, type, detail, errorcode, "", null, null);
	}

	public static String call(PageContext pc, Object message, String type, String detail, String errorcode, String extendedInfo) throws PageException {
		return _call(pc, message, type, detail, errorcode, extendedInfo, null, null);
	}

	public static String call(PageContext pc, Object message, String type, String detail, String errorcode, String extendedInfo, Object object) throws PageException {
		return _call(pc, message, type, detail, errorcode, extendedInfo, object, null);
	}

	public static String call(PageContext pc, Object message, String type, String detail, String errorcode, String extendedInfo, Object object, Object cause) throws PageException {
		return _call(pc, message, type, detail, errorcode, extendedInfo, object, cause);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		Object message = args.length > 0 ? args[0] : null;
		String type = args.length > 1 && args[1] != null ? Caster.toString(args[1]) : "application";
		String detail = args.length > 2 && args[2] != null ? Caster.toString(args[2]) : "";
		String errorcode = args.length > 3 && args[3] != null ? Caster.toString(args[3]) : "";
		String extendedInfo = args.length > 4 && args[4] != null ? Caster.toString(args[4]) : "";
		Object object = args.length > 5 ? args[5] : null;
		Object cause = args.length > 6 ? args[6] : null;
		return _call(pc, message, type, detail, errorcode, extendedInfo, object, cause);
	}

	private static String _call(PageContext pc, Object message, String type, String detail, String errorcode, String extendedInfo, Object object, Object cause) throws PageException {
		PageException pcause = toCauseException(cause);

		// Order mirrors Throw.doStartTag exactly: message first, then object, then fall-through.
		throwIfPresent(message, type, detail, errorcode, extendedInfo, pcause);
		throwIfPresent(object, type, detail, errorcode, extendedInfo, pcause);

		CustomTypeException exception = new CustomTypeException("", detail, errorcode, type, extendedInfo, LEVEL);
		ExceptionUtil.initCauseEL(exception, pcause);
		throw exception;
	}

	private static void throwIfPresent(Object obj, String type, String detail, String errorcode, String extendedInfo, PageException cause) throws PageException {
		if (StringUtil.isEmpty(obj)) return;

		// If the value is itself throwable / catchBlock / error struct, convert and throw as-is.
		PageException pe = lucee.runtime.tag.Throw.toPageException(obj, null);
		if (pe != null) throw pe;

		// Otherwise treat as a plain message string.
		CustomTypeException exception = new CustomTypeException(Caster.toString(obj), detail, errorcode, type, extendedInfo, LEVEL);
		ExceptionUtil.initCauseEL(exception, cause);
		throw exception;
	}

	private static PageException toCauseException(Object cause) throws PageException {
		if (cause == null) return null;
		if (cause instanceof Throwable && ExceptionUtil.isThreadDeath((Throwable) cause)) {
			throw new lucee.runtime.exp.ApplicationException(
					"cannot set this kind [" + cause.getClass().getName() + "] of exception as caused by");
		}
		PageException pe = lucee.runtime.tag.Throw.toPageException(cause, null);
		if (pe == null) {
			throw new lucee.runtime.exp.ApplicationException(
					"cannot cast this type [" + cause.getClass().getName() + "] to an exception");
		}
		return pe;
	}
}

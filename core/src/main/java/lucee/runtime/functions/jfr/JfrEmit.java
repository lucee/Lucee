package lucee.runtime.functions.jfr;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.jfr.JfrUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

/**
 * Emits an instant JFR event (point-in-time event, not a duration).
 * Use jfrBegin()/jfrCommit() for duration events.
 */
public final class JfrEmit extends BIF {

	private static final long serialVersionUID = 7856423454323L;

	public static String call(PageContext pc, String category, String label) {
		return call( pc, category, label, null );
	}

	public static String call(PageContext pc, String category, String label, Struct data) {
		// Silently ignore if JFR not enabled - this is expected during normal operation
		if( !JfrUtil.isEnabled() ) return null;

		JfrUtil.emitInstantEvent( category, label, data );
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if( args.length == 2 ) return call( pc, Caster.toString( args[0] ), Caster.toString( args[1] ) );
		if( args.length == 3 ) return call( pc, Caster.toString( args[0] ), Caster.toString( args[1] ), Caster.toStruct( args[2] ) );
		throw new FunctionException( pc, "JfrEmit", 2, 3, args.length );
	}
}

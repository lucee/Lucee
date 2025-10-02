package lucee.runtime.functions.jfr;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.jfr.CustomEvent;
import lucee.runtime.jfr.JfrUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

/**
 * Starts a duration event that must be committed with jfrCommit().
 * Returns an event ID to be used for committing the event.
 */
public final class JfrBegin extends BIF {

	private static final long serialVersionUID = 7856423454324L;

	public static String call(PageContext pc, String category) {
		return call( pc, category, null, null );
	}

	public static String call(PageContext pc, String category, String label) {
		return call( pc, category, label, null );
	}

	public static String call(PageContext pc, String category, String label, Struct data) {
		// Return null when JFR not available (consistent with other BIFs)
		// Note: We check isAvailable() not isEnabled() because begin/commit
		// can be used without active recordings - they just won't be recorded
		if( !JfrUtil.isAvailable() ) return null;

		CustomEvent event = new CustomEvent();
		event.eventCategory = category;

		// Provide descriptive default label when empty
		event.eventName = StringUtil.isEmpty( label ) ? "Event-" + category : label;

		// Extract message from data if provided
		if( data != null && !data.isEmpty() ) {
			try {
				if( data.containsKey( "message" ) ) {
					Object msg = data.get( "message" );
					event.message = msg != null ? msg.toString() : null;
				}
			}
			catch( PageException e ) {
				// Silently ignore - data.get() can throw but shouldn't fail event creation
				// This is safe because message is optional metadata
			}
		}

		// registerActiveEvent() will capture startTime and call begin()
		return JfrUtil.registerActiveEvent( event );
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if( args.length == 1 ) return call( pc, Caster.toString( args[0] ) );
		if( args.length == 2 ) return call( pc, Caster.toString( args[0] ), Caster.toString( args[1] ) );
		if( args.length == 3 ) return call( pc, Caster.toString( args[0] ), Caster.toString( args[1] ), Caster.toStruct( args[2] ) );
		throw new FunctionException( pc, "JfrBegin", 1, 3, args.length );
	}
}

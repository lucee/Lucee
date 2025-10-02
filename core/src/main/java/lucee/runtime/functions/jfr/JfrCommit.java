package lucee.runtime.functions.jfr;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.jfr.JfrUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

/**
 * Commits a duration event started with jfrBegin().
 * Optionally filters events by minimum duration threshold.
 */
public final class JfrCommit extends BIF {

	private static final long serialVersionUID = 7856423454325L;

	public static String call(PageContext pc, String eventId) throws PageException {
		return call( pc, eventId, null, 0 );
	}

	public static String call(PageContext pc, String eventId, Struct data) throws PageException {
		return call( pc, eventId, data, 0 );
	}

	public static String call(PageContext pc, String eventId, Struct data, double threshold) throws PageException {
		// Validate JFR is available
		if( !JfrUtil.isAvailable() ) {
			throw new FunctionException( pc, "JfrCommit", 1, "eventId", "Java Flight Recorder is not available in this JVM" );
		}

		// Validate eventId
		if( StringUtil.isEmpty( eventId ) ) {
			throw new FunctionException( pc, "JfrCommit", 1, "eventId", "Event ID cannot be empty" );
		}

		// Check if JFR is enabled
		if( !JfrUtil.isEnabled() ) {
			// JFR not recording - silently ignore
			return null;
		}

		// Validate the event exists
		if( JfrUtil.getActiveEvent( eventId ) == null ) {
			throw new FunctionException( pc, "JfrCommit", 1, "eventId", "Event not found with ID: " + eventId + ". Event may have already been committed or never started." );
		}

		// Convert threshold to milliseconds (function accepts seconds as double)
		// -1 = disable threshold checking, 0 = commit all, > 0 = minimum duration
		long thresholdMs = threshold < 0 ? -1 : (long) ( threshold * 1000 );

		// Commit the event
		boolean committed = JfrUtil.commitEvent( eventId, data, thresholdMs );

		// Return null (standard pattern for void-like BIFs)
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if( args.length == 1 ) return call( pc, Caster.toString( args[0] ) );
		if( args.length == 2 ) return call( pc, Caster.toString( args[0] ), Caster.toStruct( args[1] ) );
		if( args.length == 3 ) return call( pc, Caster.toString( args[0] ), Caster.toStruct( args[1] ), Caster.toDoubleValue( args[2] ) );
		throw new FunctionException( pc, "JfrCommit", 1, 3, args.length );
	}
}

package lucee.runtime.functions.jfr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.jfr.JfrAnalyzer;
import lucee.runtime.jfr.JfrUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class JfrAnalyze extends BIF {

	private static final long serialVersionUID = 7856423454326L;

	public static Object call(PageContext pc, String jfrFile) throws PageException {
		return call( pc, jfrFile, null, null );
	}

	public static Object call(PageContext pc, String jfrFile, String returnType) throws PageException {
		return call( pc, jfrFile, returnType, null );
	}

	/**
	 * Analyzes a JFR recording file and returns events as array or query.
	 *
	 * @param pc PageContext
	 * @param jfrFile Path to JFR recording file
	 * @param returnType "array" (default) or "query" (case-insensitive)
	 * @param options Struct with optional filters: eventTypes, categories, startTime, endTime,
	 *                minDuration, maxDuration, includeStackTraces, maxEvents, sortBy, sortOrder, fields
	 * @return Array of structs or Query object containing event data
	 * @throws PageException if JFR unavailable, file not found, or read error
	 */
	public static Object call(PageContext pc, String jfrFile, String returnType, Struct options) throws PageException {
		if( !JfrUtil.isAvailable() ) {
			throw new FunctionException( pc, "JfrAnalyze", 1, "jfrFile", "Java Flight Recorder is not available in this JVM" );
		}

		// toResourceNotExisting() allows non-existing paths for write operations
		// but here we require the file to exist for reading
		Resource res = ResourceUtil.toResourceNotExisting( pc, jfrFile );
		if( !res.exists() ) {
			throw new FunctionException( pc, "JfrAnalyze", 1, "jfrFile", "JFR file does not exist: " + jfrFile );
		}

		Path path = Paths.get( res.getAbsolutePath() );

		JfrAnalyzer.Options opts = new JfrAnalyzer.Options( pc, options );

		try {
			// returnType is case-insensitive: "query", "QUERY", "Query" all work
			if( returnType != null && "query".equalsIgnoreCase( returnType.trim() ) ) {
				return JfrAnalyzer.analyzeAsQuery( pc, path, opts );
			}
			else {
				return JfrAnalyzer.analyzeAsArray( pc, path, opts );
			}
		}
		catch( IOException e ) {
			throw new FunctionException( pc, "JfrAnalyze", 1, "jfrFile", "Error reading JFR file: " + e.getMessage() );
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if( args.length == 1 ) return call( pc, Caster.toString( args[0] ) );
		if( args.length == 2 ) return call( pc, Caster.toString( args[0] ), Caster.toString( args[1] ) );
		if( args.length == 3 ) return call( pc, Caster.toString( args[0] ), Caster.toString( args[1] ), Caster.toStruct( args[2] ) );
		throw new FunctionException( pc, "JfrAnalyze", 1, 3, args.length );
	}
}

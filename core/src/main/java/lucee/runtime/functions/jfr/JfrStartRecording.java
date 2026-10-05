package lucee.runtime.functions.jfr;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.jfr.JfrUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class JfrStartRecording extends BIF {

	private static final long serialVersionUID = 7856423454327L;

	public static String call(PageContext pc) throws PageException {
		return call( pc, null );
	}

	public static String call(PageContext pc, Struct options) throws PageException {
		if( !JfrUtil.isAvailable() ) {
			throw new FunctionException( pc, "JfrStartRecording", 0, "options", "Java Flight Recorder is not available in this JVM" );
		}

		try {
			jdk.jfr.Recording recording = new jdk.jfr.Recording();

			if( options != null && !options.isEmpty() ) {
				if( options.containsKey( "name" ) ) {
					recording.setName( Caster.toString( options.get( "name" ) ) );
				}

				if( options.containsKey( "maxAge" ) ) {
					long seconds = Caster.toLongValue( options.get( "maxAge" ) );
					recording.setMaxAge( Duration.ofSeconds( seconds ) );
				}

				if( options.containsKey( "maxSize" ) ) {
					long bytes = Caster.toLongValue( options.get( "maxSize" ) );
					recording.setMaxSize( bytes );
				}

				if( options.containsKey( "dumpOnExit" ) ) {
					recording.setDumpOnExit( Caster.toBooleanValue( options.get( "dumpOnExit" ) ) );
				}

				if( options.containsKey( "destination" ) ) {
					String dest = Caster.toString( options.get( "destination" ) );
					Resource res = ResourceUtil.toResourceNotExisting( pc, dest );
					recording.setDestination( Paths.get( res.getAbsolutePath() ) );
				}

				// Event settings: enable specific event types with thresholds
				// Example: settings={ "jdk.ObjectAllocationSample": "512 kB", "jdk.JavaMonitorWait": "20 ms" }
				// Event names must match JFR event class names (see jfr print-events command)
				// Threshold format: "<value> <unit>" where unit is ns, us, ms, s, m, h, d, kB, MB, GB
				if( options.containsKey( "settings" ) ) {
					Object settingsObj = options.get( "settings" );
					if( settingsObj instanceof Struct ) {
						Struct settings = (Struct) settingsObj;
						for( Object keyObj : settings.keys() ) {
							String key = Caster.toString( keyObj );
							String value = Caster.toString( settings.get( key ) );
							recording.enable( key ).with( "threshold", value );
						}
					}
				}
			}

			recording.start();
			return String.valueOf( recording.getId() );
		}
		catch( IllegalArgumentException e ) {
			// Invalid option values (e.g., negative maxSize, invalid event names)
			throw new FunctionException( pc, "JfrStartRecording", 0, "options", "Invalid recording configuration: " + e.getMessage() );
		}
		catch( SecurityException e ) {
			// Permission denied for JFR operations
			throw new FunctionException( pc, "JfrStartRecording", 0, "options", "Permission denied for JFR recording: " + e.getMessage() );
		}
		catch( Exception e ) {
			// Catch-all for unexpected JFR errors
			throw new FunctionException( pc, "JfrStartRecording", 0, "options", "Error starting JFR recording: " + e.getMessage() );
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if( args.length == 0 ) return call( pc );
		if( args.length == 1 ) return call( pc, Caster.toStruct( args[0] ) );
		throw new FunctionException( pc, "JfrStartRecording", 0, 1, args.length );
	}
}

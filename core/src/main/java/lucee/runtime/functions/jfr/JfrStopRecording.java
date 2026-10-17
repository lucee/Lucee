package lucee.runtime.functions.jfr;

import java.io.IOException;
import java.nio.file.Paths;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.jfr.JfrUtil;
import lucee.runtime.op.Caster;

public final class JfrStopRecording extends BIF {

	private static final long serialVersionUID = 7856423454328L;

	public static String call(PageContext pc, Object recordingId) throws PageException {
		return call( pc, recordingId, null );
	}

	public static String call(PageContext pc, Object recordingId, String destination) throws PageException {
		if( !JfrUtil.isAvailable() ) {
			throw new FunctionException( pc, "JfrStopRecording", 1, "recordingId", "Java Flight Recorder is not available in this JVM" );
		}

		try {
			long id = Caster.toLongValue( recordingId );

			// Linear search is acceptable - typical use has < 10 active recordings
			// FlightRecorder maintains recordings in memory, so this is fast
			jdk.jfr.Recording recording = null;
			for( jdk.jfr.Recording r : jdk.jfr.FlightRecorder.getFlightRecorder().getRecordings() ) {
				if( r.getId() == id ) {
					recording = r;
					break;
				}
			}

			if( recording == null ) {
				throw new FunctionException( pc, "JfrStopRecording", 1, "recordingId", "Recording not found with ID: " + id + ". Recording may have already been stopped." );
			}

			if( destination != null && !destination.trim().isEmpty() ) {
				// ResourceUtil validates paths and prevents traversal attacks
				Resource res = ResourceUtil.toResourceNotExisting( pc, destination );
				recording.dump( Paths.get( res.getAbsolutePath() ) );
			}

			recording.stop();
			recording.close();

			return destination != null ? destination : "Recording stopped (ID: " + id + ")";
		}
		catch( IOException e ) {
			// File I/O errors (disk full, permissions, invalid path)
			throw new FunctionException( pc, "JfrStopRecording", 2, "destination", "Error writing JFR file: " + e.getMessage() );
		}
		catch( IllegalArgumentException e ) {
			// Invalid recordingId format
			throw new FunctionException( pc, "JfrStopRecording", 1, "recordingId", "Invalid recording ID: " + e.getMessage() );
		}
		catch( SecurityException e ) {
			// Permission denied
			throw new FunctionException( pc, "JfrStopRecording", 2, "destination", "Permission denied: " + e.getMessage() );
		}
		catch( Exception e ) {
			// Catch-all for unexpected errors
			throw new FunctionException( pc, "JfrStopRecording", 1, "recordingId", "Error stopping recording: " + e.getMessage() );
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if( args.length == 1 ) return call( pc, args[0] );
		if( args.length == 2 ) return call( pc, args[0], Caster.toString( args[1] ) );
		throw new FunctionException( pc, "JfrStopRecording", 1, 2, args.length );
	}
}

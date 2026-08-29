package lucee.runtime.jfr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * Utility class for managing Java Flight Recorder (JFR) events in Lucee.
 * Handles event registration, lifecycle management, and serialization.
 */
public class JfrUtil {

	private static final boolean JFR_AVAILABLE;
	private static final ConcurrentHashMap<String, jdk.jfr.Event> activeEvents = new ConcurrentHashMap<>();
	private static final AtomicLong eventIdCounter = new AtomicLong( 1 );

	static {
		boolean available = false;
		try {
			ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
			Class.forName( "jdk.jfr.Event", true, systemClassLoader );
			Class.forName( "jdk.jfr.FlightRecorder", true, systemClassLoader );
			available = true;
		}
		catch( ClassNotFoundException e ) {
			available = false;
		}
		JFR_AVAILABLE = available;
	}

	public static boolean isAvailable() {
		return JFR_AVAILABLE;
	}

	public static boolean isEnabled() {
		if( !JFR_AVAILABLE ) return false;
		try {
			return jdk.jfr.FlightRecorder.isAvailable() && !jdk.jfr.FlightRecorder.getFlightRecorder().getRecordings().isEmpty();
		}
		catch( Exception e ) {
			return false;
		}
	}

	/**
	 * Register an active duration event and return its ID for later commit.
	 * Uses an AtomicLong counter for efficient ID generation.
	 * Captures start time and calls begin() on the event.
	 *
	 * @param event The JFR event to register
	 * @return Event ID as string, or null if event is null
	 */
	public static String registerActiveEvent( jdk.jfr.Event event ) {
		if( event == null ) return null;

		// Capture start time before begin() for accurate duration measurement
		if( event instanceof CustomEvent ) {
			( (CustomEvent) event ).startTime = System.nanoTime();
		}

		// Start the JFR event timing
		event.begin();

		String eventId = String.valueOf( eventIdCounter.getAndIncrement() );
		activeEvents.put( eventId, event );
		return eventId;
	}

	public static jdk.jfr.Event getActiveEvent( String eventId ) {
		if( StringUtil.isEmpty( eventId ) ) return null;
		return activeEvents.get( eventId );
	}

	public static void removeActiveEvent( String eventId ) {
		if( StringUtil.isEmpty( eventId ) ) return;
		activeEvents.remove( eventId );
	}

	/**
	 * Commit an active duration event with optional data.
	 * Event is removed from active events map only after successful processing.
	 *
	 * @param eventId The event ID returned from registerActiveEvent
	 * @param data Optional data to attach to the event
	 * @param thresholdMs Minimum duration in milliseconds (-1 to disable, 0 for all events)
	 * @return true if event was committed, false if below threshold or not found
	 */
	public static boolean commitEvent( String eventId, Map<String, Object> data, long thresholdMs ) {
		if( StringUtil.isEmpty( eventId ) ) return false;

		jdk.jfr.Event event = activeEvents.get( eventId );
		if( event == null ) return false;

		// Check if this is a CustomEvent and populate data fields
		if( event instanceof CustomEvent ) {
			CustomEvent ce = (CustomEvent) event;

			// Handle success flag - can be set explicitly even without other data
			if( data != null && data.containsKey( "success" ) ) {
				ce.success = Boolean.TRUE.equals( data.get( "success" ) );
			}

			// Serialize data if present
			if( data != null && !data.isEmpty() ) {
				ce.data = serializeData( data );
			}

			// Check duration threshold if enabled (thresholdMs >= 0)
			if( thresholdMs >= 0 && ce.startTime > 0 ) {
				long durationMs = ( System.nanoTime() - ce.startTime ) / 1_000_000;
				if( durationMs < thresholdMs ) {
					// Below threshold - remove from map and don't commit
					activeEvents.remove( eventId );
					return false;
				}
			}
		}

		// Commit the event if it should be recorded
		boolean committed = false;
		if( event.shouldCommit() ) {
			event.commit();
			committed = true;
		}

		// Remove from active events map after processing
		activeEvents.remove( eventId );
		return committed;
	}

	public static void emitInstantEvent( String category, String label, Map<String, Object> data ) {
		if( !isEnabled() ) return;

		CustomEvent event = new CustomEvent();
		event.eventCategory = category;
		event.eventName = label;
		if( data != null && !data.isEmpty() ) {
			event.data = serializeData( data );
			if( data.containsKey( "message" ) ) {
				Object msg = data.get( "message" );
				event.message = msg != null ? msg.toString() : null;
			}
			if( data.containsKey( "success" ) ) {
				event.success = Boolean.TRUE.equals( data.get( "success" ) );
			}
		}
		event.commit();
	}

	/**
	 * Serialize data to JSON format for JFR event storage.
	 * Falls back to manual JSON construction if PageContext is unavailable.
	 *
	 * @param data Map of data to serialize
	 * @return JSON string or null if data is empty
	 */
	private static String serializeData( Map<String, Object> data ) {
		if( data == null || data.isEmpty() ) return null;
		try {
			PageContext pc = ThreadLocalPageContext.get();
			if( pc != null ) {
				JSONConverter converter = new JSONConverter( false, null );
				return converter.serialize( pc, data );
			}
			else {
				// Fallback: manual JSON construction when PageContext unavailable
				return manualJsonSerialize( data );
			}
		}
		catch( ConverterException e ) {
			// Fallback to manual serialization if conversion fails
			return manualJsonSerialize( data );
		}
	}

	/**
	 * Manual JSON serialization fallback.
	 * Handles basic types: String, Number, Boolean, null.
	 */
	private static String manualJsonSerialize( Map<String, Object> data ) {
		if( data == null || data.isEmpty() ) return "{}";
		StringBuilder sb = new StringBuilder( "{" );
		boolean first = true;
		for( Map.Entry<String, Object> entry : data.entrySet() ) {
			if( !first ) sb.append( "," );
			first = false;
			sb.append( "\"" ).append( escapeJson( entry.getKey() ) ).append( "\":" );
			Object value = entry.getValue();
			if( value == null ) {
				sb.append( "null" );
			}
			else if( value instanceof String ) {
				sb.append( "\"" ).append( escapeJson( value.toString() ) ).append( "\"" );
			}
			else if( value instanceof Number || value instanceof Boolean ) {
				sb.append( value );
			}
			else {
				sb.append( "\"" ).append( escapeJson( value.toString() ) ).append( "\"" );
			}
		}
		sb.append( "}" );
		return sb.toString();
	}

	/**
	 * Escape special characters for JSON strings.
	 */
	private static String escapeJson( String str ) {
		if( str == null ) return "";
		return str.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ).replace( "\n", "\\n" ).replace( "\r", "\\r" ).replace( "\t", "\\t" );
	}
}

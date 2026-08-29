package lucee.runtime.jfr;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class JfrAnalyzer {

	public static class Options {
		public Set<String> eventTypes;
		public Set<String> categories;
		public Instant startTime;
		public Instant endTime;
		public Long minDuration;
		public Long maxDuration;
		public boolean includeStackTraces;
		public int maxEvents;
		public String sortBy;
		public boolean sortDesc;
		public Set<String> fields;

		public Options(PageContext pc, Struct options) throws PageException {
			if( options == null ) {
				includeStackTraces = false;
				maxEvents = Integer.MAX_VALUE;
				return;
			}

			if( options.containsKey( "eventTypes" ) ) {
				eventTypes = new HashSet<>();
				Object val = options.get( "eventTypes" );
				if( val instanceof Array ) {
					Array arr = (Array) val;
					for( int i = 1; i <= arr.size(); i++ ) {
						eventTypes.add( Caster.toString( arr.get( i, null ) ) );
					}
				}
				else {
					eventTypes.add( Caster.toString( val ) );
				}
			}

			if( options.containsKey( "category" ) ) {
				categories = new HashSet<>();
				Object val = options.get( "category" );
				if( val instanceof Array ) {
					Array arr = (Array) val;
					for( int i = 1; i <= arr.size(); i++ ) {
						categories.add( Caster.toString( arr.get( i, null ) ) );
					}
				}
				else {
					categories.add( Caster.toString( val ) );
				}
			}

			if( options.containsKey( "startTime" ) ) {
				startTime = Instant.ofEpochMilli( Caster.toDate( options.get( "startTime" ), pc.getTimeZone() ).getTime() );
			}

			if( options.containsKey( "endTime" ) ) {
				endTime = Instant.ofEpochMilli( Caster.toDate( options.get( "endTime" ), pc.getTimeZone() ).getTime() );
			}

			if( options.containsKey( "minDuration" ) ) {
				minDuration = Caster.toLongValue( options.get( "minDuration" ) );
			}

			if( options.containsKey( "maxDuration" ) ) {
				maxDuration = Caster.toLongValue( options.get( "maxDuration" ) );
			}

			includeStackTraces = Caster.toBooleanValue( options.get( "includeStackTraces", Boolean.FALSE ), false );

			maxEvents = Caster.toIntValue( options.get( "maxEvents", Integer.MAX_VALUE ), Integer.MAX_VALUE );

			if( options.containsKey( "sortBy" ) ) {
				sortBy = Caster.toString( options.get( "sortBy" ) );
			}

			sortDesc = "desc".equalsIgnoreCase( Caster.toString( options.get( "sortOrder", "asc" ) ) );

			if( options.containsKey( "fields" ) ) {
				fields = new HashSet<>();
				Object val = options.get( "fields" );
				if( val instanceof Array ) {
					Array arr = (Array) val;
					for( int i = 1; i <= arr.size(); i++ ) {
						fields.add( Caster.toString( arr.get( i, null ) ) );
					}
				}
				else {
					fields.add( Caster.toString( val ) );
				}
			}
		}

		public boolean matchesEvent(jdk.jfr.consumer.RecordedEvent event) {
			if( eventTypes != null && !eventTypes.contains( event.getEventType().getName() ) ) {
				return false;
			}

			if( categories != null ) {
				boolean matchesCategory = false;
				for( String category : event.getEventType().getCategoryNames() ) {
					if( categories.contains( category ) ) {
						matchesCategory = true;
						break;
					}
				}
				if( !matchesCategory ) return false;
			}

			if( startTime != null && event.getStartTime().isBefore( startTime ) ) {
				return false;
			}

			if( endTime != null && event.getStartTime().isAfter( endTime ) ) {
				return false;
			}

			long durationMs = event.getDuration().toMillis();
			if( minDuration != null && durationMs < minDuration ) {
				return false;
			}

			if( maxDuration != null && durationMs > maxDuration ) {
				return false;
			}

			return true;
		}
	}

	public static Array analyzeAsArray(PageContext pc, Path path, Options opts) throws IOException, PageException {
		List<Struct> eventsList = new ArrayList<>();

		try( jdk.jfr.consumer.RecordingFile recordingFile = new jdk.jfr.consumer.RecordingFile( path ) ) {
			while( recordingFile.hasMoreEvents() ) {
				jdk.jfr.consumer.RecordedEvent event = recordingFile.readEvent();

				if( !opts.matchesEvent( event ) ) {
					continue;
				}

				Struct eventData = new StructImpl();
				eventData.set( "eventType", event.getEventType().getName() );
				eventData.set( "startTime", event.getStartTime().toString() );
				eventData.set( "duration", event.getDuration().toMillis() );

				if( opts.includeStackTraces && event.getStackTrace() != null ) {
					eventData.set( "stackTrace", formatStackTrace( event.getStackTrace() ) );
				}

				Struct fields = new StructImpl();
				for( jdk.jfr.ValueDescriptor field : event.getEventType().getFields() ) {
					String fieldName = field.getName();
					if( opts.fields != null && !opts.fields.contains( fieldName ) ) {
						continue;
					}
					Object value = event.getValue( fieldName );
					if( value != null ) {
						fields.set( fieldName, value.toString() );
					}
				}
				eventData.set( "fields", fields );

				eventsList.add( eventData );

				if( eventsList.size() >= opts.maxEvents ) {
					break;
				}
			}
		}

		if( opts.sortBy != null ) {
			sortEvents( eventsList, opts );
		}

		Array events = new ArrayImpl();
		for( Struct event : eventsList ) {
			events.append( event );
		}

		return events;
	}

	public static Query analyzeAsQuery(PageContext pc, Path path, Options opts) throws IOException, PageException {
		List<QueryRow> rows = new ArrayList<>();

		try( jdk.jfr.consumer.RecordingFile recordingFile = new jdk.jfr.consumer.RecordingFile( path ) ) {
			while( recordingFile.hasMoreEvents() ) {
				jdk.jfr.consumer.RecordedEvent event = recordingFile.readEvent();

				if( !opts.matchesEvent( event ) ) {
					continue;
				}

				QueryRow row = new QueryRow();
				row.eventType = event.getEventType().getName();
				row.startTime = event.getStartTime().toString();
				row.duration = event.getDuration().toMillis();

				Struct fields = new StructImpl();
				for( jdk.jfr.ValueDescriptor field : event.getEventType().getFields() ) {
					String fieldName = field.getName();
					if( opts.fields != null && !opts.fields.contains( fieldName ) ) {
						continue;
					}
					Object value = event.getValue( fieldName );
					if( value != null ) {
						fields.set( fieldName, value.toString() );
					}
				}
				row.fields = fields;

				rows.add( row );

				if( rows.size() >= opts.maxEvents ) {
					break;
				}
			}
		}

		if( opts.sortBy != null ) {
			sortQueryRows( rows, opts );
		}

		Query qry = new QueryImpl( new String[] { "eventType", "startTime", "duration", "fields" }, rows.size(), "jfr" );
		for( int i = 0; i < rows.size(); i++ ) {
			QueryRow row = rows.get( i );
			qry.setAt( "eventType", i + 1, row.eventType );
			qry.setAt( "startTime", i + 1, row.startTime );
			qry.setAt( "duration", i + 1, row.duration );
			qry.setAt( "fields", i + 1, row.fields );
		}

		return qry;
	}

	private static class QueryRow {
		String eventType;
		String startTime;
		long duration;
		Struct fields;
	}

	private static void sortEvents(List<Struct> events, Options opts) {
		Comparator<Struct> comparator = null;

		try {
			if( "duration".equalsIgnoreCase( opts.sortBy ) ) {
				comparator = Comparator.comparingLong( s -> {
					try {
						return Caster.toLongValue( s.get( "duration" ) );
					}
					catch( PageException e ) {
						return 0L;
					}
				} );
			}
			else if( "startTime".equalsIgnoreCase( opts.sortBy ) ) {
				comparator = Comparator.comparing( s -> {
					try {
						return Caster.toString( s.get( "startTime" ) );
					}
					catch( PageException e ) {
						return "";
					}
				} );
			}
			else if( "eventType".equalsIgnoreCase( opts.sortBy ) ) {
				comparator = Comparator.comparing( s -> {
					try {
						return Caster.toString( s.get( "eventType" ) );
					}
					catch( PageException e ) {
						return "";
					}
				} );
			}

			if( comparator != null ) {
				if( opts.sortDesc ) {
					comparator = comparator.reversed();
				}
				events.sort( comparator );
			}
		}
		catch( Exception e ) {
		}
	}

	private static void sortQueryRows(List<QueryRow> rows, Options opts) {
		Comparator<QueryRow> comparator = null;

		if( "duration".equalsIgnoreCase( opts.sortBy ) ) {
			comparator = Comparator.comparingLong( r -> r.duration );
		}
		else if( "startTime".equalsIgnoreCase( opts.sortBy ) ) {
			comparator = Comparator.comparing( r -> r.startTime );
		}
		else if( "eventType".equalsIgnoreCase( opts.sortBy ) ) {
			comparator = Comparator.comparing( r -> r.eventType );
		}

		if( comparator != null ) {
			if( opts.sortDesc ) {
				comparator = comparator.reversed();
			}
			rows.sort( comparator );
		}
	}

	private static String formatStackTrace(jdk.jfr.consumer.RecordedStackTrace stackTrace) {
		StringBuilder sb = new StringBuilder();
		List<jdk.jfr.consumer.RecordedFrame> frames = stackTrace.getFrames();
		for( int i = 0; i < Math.min( frames.size(), 10 ); i++ ) {
			jdk.jfr.consumer.RecordedFrame frame = frames.get( i );
			jdk.jfr.consumer.RecordedMethod method = frame.getMethod();
			sb.append( method.getType().getName() )
				.append( "." )
				.append( method.getName() )
				.append( " (line " )
				.append( frame.getLineNumber() )
				.append( ")\n" );
		}
		if( frames.size() > 10 ) {
			sb.append( "... " ).append( frames.size() - 10 ).append( " more frames\n" );
		}
		return sb.toString();
	}
}

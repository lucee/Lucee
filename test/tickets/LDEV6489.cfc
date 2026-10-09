/*
 * LDEV-6489: an empty or truncated .tsk file in the spooler's open directory.
 * The spooler (and the admin task list) read every *.tsk; on a read error the file was deleted with only an
 * EOFException in the log (no file name). A file that is still being written got deleted too (lost task).
 * Expected: a fresh file is left alone (it may still be written), an older unreadable file is moved aside to
 * remote-client/broken and the log names the file. No mail server needed (future sendTime only).
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function beforeAll() {
		variables.engine = getPageContext().getConfig().getSpoolerEngine();
		variables.rcDir = getPageContext().getConfig().getRemoteClientDirectory();
		variables.openDir = rcDir.getRealResource( "open" );
		variables.brokenDir = rcDir.getRealResource( "broken" );
		openDir.mkdirs();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6489 empty or truncated spooler task file", function() {

			it( title="control: a spooled task is written to the open directory and can be read back", body=function( currentSpec ) {
				var subject = "LDEV6489-control-" & createUUID();
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="127.0.0.1" port=2525 spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
					echo( "LDEV-6489" );
				}
				var tasks = variables.engine.getOpenTasksAsQuery( 1, 10000 );
				var mine = queryFilter( tasks, function( row ) { return row.name == subject; } );
				expect( mine.recordCount ).toBe( 1, "spooled task not found" );
				expect( fileExists( variables.openDir.getRealResource( mine.id & ".tsk" ).getAbsolutePath() ) ).toBeTrue( "task file not in [#variables.openDir#]" );
				variables.engine.remove( mine.id );
			});

			it( title="control: a spooled task that fails is stored again (retry) and can still be read back", body=function( currentSpec ) {
				var subject = "LDEV6489-retry-" & createUUID();
				// port 1 is closed, so the first attempt fails and the task file is written again with its lastExecution
				mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject
						server="127.0.0.1" port=1 spoolEnable=true {
					echo( "LDEV-6489 retry" );
				}
				var mine = "";
				var end = getTickCount() + 20000;
				while ( getTickCount() < end ) {
					var tasks = variables.engine.getOpenTasksAsQuery( 1, 10000 );
					mine = queryFilter( tasks, function( row ) { return row.name == subject; } );
					if ( mine.recordCount && year( mine.lastExecution ) > 2000 ) break;
					sleep( 250 );
				}
				expect( mine.recordCount ).toBe( 1, "failed task not found in the open directory after its first attempt" );
				expect( year( mine.lastExecution ) ).toBeGT( 2000, "task was not executed and stored again within 20s" );
				variables.engine.remove( mine.id );
			});

			it( title="an older truncated task file is moved aside (not deleted, not retried) and the log names it", body=function( currentSpec ) {
				var name = "LDEV6489-trunc-" & createUUID() & ".tsk";
				var f = variables.openDir.getRealResource( name ).getAbsolutePath();
				fileWrite( f, binaryDecode( "ACED00", "hex" ) ); // truncated java serialization stream header
				fileSetLastModified( f, dateAdd( "h", -1, now() ) );
				variables.engine.getOpenTasksAsQuery( 1, 10000 );
				sleep( 500 );
				var moved = variables.brokenDir.getRealResource( name ).getAbsolutePath();
				var stillOpen = fileExists( f );
				var isMoved = fileExists( moved );
				if ( stillOpen ) fileDelete( f );
				if ( isMoved ) fileDelete( moved );
				expect( stillOpen ).toBeFalse( "unreadable task file stays in the open directory and is read again on every run" );
				expect( isMoved ).toBeTrue( "unreadable task file was deleted instead of being moved to [#variables.brokenDir#]" );
				expect( logsMention( name ) ).toBeTrue( "no log entry names the unreadable file [#name#]" );
			});

			it( title="a fresh empty task file (may still be written) is left alone", body=function( currentSpec ) {
				var name = "LDEV6489-empty-" & createUUID() & ".tsk";
				var f = variables.openDir.getRealResource( name ).getAbsolutePath();
				fileWrite( f, "" );
				variables.engine.getOpenTasksAsQuery( 1, 10000 );
				sleep( 500 );
				var stillOpen = fileExists( f );
				if ( stillOpen ) fileDelete( f );
				var moved = variables.brokenDir.getRealResource( name ).getAbsolutePath();
				if ( fileExists( moved ) ) fileDelete( moved );
				expect( stillOpen ).toBeTrue( "a task file that may still be written was deleted on read" );
			});
		});
	}

	private boolean function logsMention( required string name ) {
		var dirs = [ expandPath( "{lucee-server}/logs" ), expandPath( "{lucee-web}/logs" ) ];
		for ( var d in dirs ) {
			if ( !directoryExists( d ) ) continue;
			for ( var lf in directoryList( d, false, "path", "*.log" ) ) {
				for ( var line in listToArray( fileRead( lf ), chr( 10 ) ) ) {
					if ( find( arguments.name, line ) ) {
						systemOutput( "LDEV6489 log [" & listLast( lf, "/\" ) & "]: " & left( line, 500 ), true );
						return true;
					}
				}
			}
		}
		return false;
	}
}

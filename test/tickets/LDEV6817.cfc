/*
 * LDEV-6817: spooler task ids were created from a counter of the running instance (starting at 1) and a
 * non-atomic exists() check, so two instances sharing a spool directory could pick the same id and the second
 * task overwrote the first one (silent mail loss). The ids have to be unique across instances and task files
 * written by older versions (8 digit counter ids) still have to be read.
 * A queued mail (future sendTime, nothing is sent) is used as the task.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function beforeAll() {
		variables.engine = getPageContext().getConfig().getSpoolerEngine();
		variables.openDir = getPageContext().getConfig().getRemoteClientDirectory().getRealResource( "open" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6817 spooler task ids across instances", function() {

			it( title="a task being written by another instance (id reserved by its temp file) is not overwritten", body=function( currentSpec ) {
				var first = queueMail( "first" );
				var other = "";
				var second = {};
				try {
					// another instance that started with its own counter is in the middle of storing its task with the
					// id this instance would take next: the task file only exists as <id>.tsk.tmp until it is renamed
					if ( reFind( "^\d+$", first.id ) ) {
						var nextId = numberFormat( val( first.id ) + 1, "00000000" );
						other = variables.openDir.getRealResource( nextId & ".tsk.tmp" ).getAbsolutePath();
						fileWrite( other, "task of another instance" );
					}
					second = queueMail( "second" );
					expect( second.id ).notToBe( first.id );
					if ( len( other ) ) {
						expect( fileExists( other ) ).toBeTrue( "the task of the other instance was removed" );
						expect( fileRead( other ) ).toBe( "task of another instance", "the task of the other instance was overwritten" );
					}
				}
				finally {
					if ( len( other ) && fileExists( other ) ) fileDelete( other );
					cleanup( first );
					if ( !structIsEmpty( second ) ) cleanup( second );
				}
			});

			it( title="every queued task gets its own id", body=function( currentSpec ) {
				var tasks = [];
				try {
					loop times=5 {
						arrayAppend( tasks, queueMail( "many" ) );
					}
					var ids = {};
					loop array=tasks item="local.t" {
						ids[ t.id ] = true;
						expect( fileExists( t.file ) ).toBeTrue( "task file [#t.file#] does not exist" );
					}
					expect( structCount( ids ) ).toBe( 5, "task ids are not unique" );
				}
				finally {
					loop array=tasks item="local.t" {
						cleanup( t );
					}
				}
			});

			it( title="a task file written by an older version (counter id) is still read and can be removed", body=function( currentSpec ) {
				// same fixture as LDEV-3213: mail queued by 7.1.2.32 with sendTime 2099 (never sent), its id is the
				// 8 digit counter id of that version
				var file = variables.openDir.getRealResource( "ldev6817old.tsk" ).getAbsolutePath();
				var renamed = "";
				if ( !variables.openDir.exists() ) variables.openDir.mkdirs();
				try {
					fileCopy( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV3213/MailSpoolerTask-7.1.2.32-mail-1.1.0.11-RC.tsk", file );
					var q = variables.engine.getOpenTasksAsQuery( 1, 10000 );
					var mine = queryFilter( q, function( row ) { return row.name == "LDEV3213-upgrade-fixture"; } );
					expect( mine.recordCount ).toBe( 1, "task with the old id format was not found" );
					var id = mine.id;
					expect( id ).toMatch( "^\d{8}$" );
					// the engine finds a task by its id as file name
					renamed = variables.openDir.getRealResource( id & ".tsk" ).getAbsolutePath();
					expect( fileExists( renamed ) ).toBeFalse( "task file [#renamed#] already exists" );
					fileMove( file, renamed );
					variables.engine.remove( id );
					expect( fileExists( renamed ) ).toBeFalse( "task with the old id format was not removed" );
				}
				finally {
					if ( fileExists( file ) ) fileDelete( file );
				}
			});
		});
	}

	private struct function queueMail( required string label ) {
		var subject = "LDEV6817-#arguments.label#-" & createUUID();
		mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject server="127.0.0.1" port="1"
				spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
			echo( "LDEV-6817" );
		}
		var q = variables.engine.getOpenTasksAsQuery( 1, 10000 );
		var mine = queryFilter( q, function( row ) { return row.name == subject; } );
		expect( mine.recordCount ).toBe( 1, "spooled task not found" );
		return { subject: subject, id: mine.id, file: variables.openDir.getRealResource( mine.id & ".tsk" ).getAbsolutePath() };
	}

	private function cleanup( required struct t ) {
		variables.engine.remove( arguments.t.id );
		if ( fileExists( arguments.t.file ) ) fileDelete( arguments.t.file );
	}
}

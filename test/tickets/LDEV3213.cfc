/*
 * LDEV-3213: a spooled task written by another Lucee version can't be read when one of its classes has no
 * explicit serialVersionUID and the computed one changed ("local class incompatible: stream classdesc
 * serialVersionUID = ..."). The task file was then deleted, so a queued mail was lost after an update
 * (e.g. 6.2 -> 7.0: MailSpoolerTask, Attachment, ExecutionPlanImpl, ArraySupport, ProxyDataImpl all differ). On 7.1
 * the mail task classes come from the mail extension, core reads them the same way.
 * The test writes a queued mail (future sendTime, nothing is sent) and changes the serialVersionUID of the
 * task class in the file, as if it was written by another version with the same fields.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function beforeAll() {
		variables.engine = getPageContext().getConfig().getSpoolerEngine();
		variables.rcDir = getPageContext().getConfig().getRemoteClientDirectory();
		variables.openDir = variables.rcDir.getRealResource( "open" );
		variables.brokenDir = variables.rcDir.getRealResource( "broken" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3213 spooled task with a different serialVersionUID", function() {

			it( title="control: a queued mail can be read back", body=function( currentSpec ) {
				var t = queueMail();
				try {
					expect( findTask( t.subject ) ).toBe( 1 );
				}
				finally {
					cleanup( t );
				}
			});

			it( title="a queued mail written with another serialVersionUID (same fields) can still be read", body=function( currentSpec ) {
				var t = queueMail();
				try {
					var info = rewrite( t, false );
					systemOutput( "LDEV3213 changed serialVersionUID of [#info.className#]", true );
					expect( findTask( t.subject ) ).toBe( 1, "task with a changed serialVersionUID of [#info.className#] could not be read (and was removed)" );
				}
				finally {
					cleanup( t );
				}
			});

			it( title="upgrade: a mail queued by an earlier 7.1 / mail extension (real task file) is read by this version", body=function( currentSpec ) {
				// written by 7.1.2.32-SNAPSHOT with mail extension 1.1.0.11-RC: queued mail with cc, replyto,
				// failto, a header, an attachment, text + html parts, sendTime 2099 (never sent).
				// Note: mails queued by 6.2 / 7.0 use the core class lucee.runtime.spooler.mail.MailSpoolerTask,
				// which no longer exists in 7.1 (mail moved to the extension), so they are not covered here.
				var subject = "LDEV3213-upgrade-fixture";
				var file = variables.openDir.getRealResource( "ldev3213upgrade.tsk" ).getAbsolutePath();
				if ( !variables.openDir.exists() ) variables.openDir.mkdirs();
				fileCopy( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV3213/MailSpoolerTask-7.1.2.32-mail-1.1.0.11-RC.tsk", file );
				var t = { subject: subject, id: "ldev3213upgrade", file: file };
				try {
					var q = variables.engine.getOpenTasksAsQuery( 1, 10000 );
					var mine = queryFilter( q, function( row ) { return row.name == subject; } );
					expect( mine.recordCount ).toBe( 1, "mail queued by 7.1.2.32 could not be read" );
					var detail = serializeJSON( mine.detail );
					expect( detail ).toInclude( "receiver@lucee.org" );
					expect( detail ).toInclude( "sender@lucee.org" );
					t.id = mine.id;
				}
				finally {
					cleanup( t );
				}
			});

			it( title="a task whose class has different fields is still rejected (not misread)", body=function( currentSpec ) {
				var t = queueMail();
				try {
					var info = rewrite( t, true );
					expect( findTask( t.subject ) ).toBe( 0, "task with different fields of [#info.className#] was read" );
				}
				finally {
					cleanup( t );
				}
			});
		});
	}

	private struct function queueMail() {
		var subject = "LDEV3213-" & createUUID();
		mail to="receiver@lucee.org" from="sender@lucee.org" subject=subject server="127.0.0.1" port="1"
				spoolEnable=true sendTime=dateAdd( "d", 1, now() ) {
			echo( "LDEV-3213" );
		}
		var q = variables.engine.getOpenTasksAsQuery( 1, 10000 );
		var mine = queryFilter( q, function( row ) { return row.name == subject; } );
		expect( mine.recordCount ).toBe( 1, "spooled task not found" );
		return { subject: subject, id: mine.id, file: variables.openDir.getRealResource( mine.id & ".tsk" ).getAbsolutePath() };
	}

	// changes the serialVersionUID of the first class in the stream (the task class itself),
	// with changeField=true also the first letter of its first field name
	private struct function rewrite( required struct t, boolean changeField=false ) {
		var bin = fileReadBinary( arguments.t.file );
		var bb = createObject( "java", "java.nio.ByteBuffer" ).wrap( bin );
		// AC ED 00 05, 73 TC_OBJECT, 72 TC_CLASSDESC, u2 length, name, 8 byte serialVersionUID
		expect( bb.get( 4 ) ).toBe( 115, "stream does not start with an object" );
		expect( bb.get( 5 ) ).toBe( 114, "stream does not start with a class descriptor" );
		var len = bb.getShort( 6 );
		var className = createObject( "java", "java.lang.String" ).init( bin, javaCast( "int", 8 ), javaCast( "int", len ), "UTF-8" );
		var suidPos = 8 + len + 7;
		bb.put( suidPos, javaCast( "byte", bitXor( bb.get( suidPos ), 1 ) ) );
		if ( arguments.changeField ) {
			// flags (1), field count (2), first field: type code (1), name length (2), name
			var fieldCount = bb.getShort( 8 + len + 8 + 1 );
			expect( fieldCount ).toBeGT( 0, "[#className#] has no serialized fields" );
			var namePos = 8 + len + 8 + 1 + 2 + 1 + 2;
			bb.put( namePos, javaCast( "byte", bitXor( bb.get( namePos ), 32 ) ) );
		}
		// replace the task file (same id, so remove() still finds it)
		variables.engine.remove( arguments.t.id );
		fileWrite( arguments.t.file, bb.array() );
		fileSetLastModified( arguments.t.file, dateAdd( "h", -1, now() ) );
		return { className: className };
	}

	private numeric function findTask( required string subject ) {
		var q = variables.engine.getOpenTasksAsQuery( 1, 10000 );
		return queryFilter( q, function( row ) { return row.name == subject; } ).recordCount;
	}

	private function cleanup( required struct t ) {
		try { variables.engine.remove( arguments.t.id ); } catch ( e ) {}
		if ( fileExists( arguments.t.file ) ) fileDelete( arguments.t.file );
		var broken = variables.brokenDir.getRealResource( arguments.t.id & ".tsk" ).getAbsolutePath();
		if ( fileExists( broken ) ) fileDelete( broken );
	}
}

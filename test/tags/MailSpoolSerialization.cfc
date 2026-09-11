/*
 * Unit-level regression test for LDEV-6455.
 *
 * A spooled cfmail is persisted by the spooler via Java serialization and later reloaded, so the
 * mail extension's spool task must survive an ObjectSave -> ObjectLoad round-trip. Two regressions
 * broke that after mail moved from the core into the extension:
 *
 *   - mail-extension 1.1.0.6 held a non-serializable java.nio.charset.Charset, so ObjectSave threw
 *     NotSerializableException: sun.nio.cs.UTF_8 (write side).
 *   - mail-extension 1.1.0.8-RC serializes fine, but ObjectLoad throws
 *     ClassNotFoundException: org.lucee.extension.mail.spooler.MailSpoolerTask, because the spooler
 *     deserializes with the core engine classloader, which cannot see extension-bundle classes
 *     (read side - the same code path as SpoolerEngineImpl.getTask()).
 *
 * This complements MailSpool.cfc (which drives the full spooled-delivery path); here we build a real
 * MailSpoolerTask directly and exercise only the serialization round-trip - fast and deterministic,
 * no SMTP server and no background spooler thread.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	// mail-extension id; the org.lucee:mail jar (which carries the extension classes) shares its version
	variables.MAIL_ID = "212BA548-F15A-4EBD-8B1EEDF8DD8A844D";

	private string function mailExtVersion() {
		for ( var row in extensionList() ) {
			if ( row.id == variables.MAIL_ID ) return row.version;
		}
		return "";
	}

	function run( testResults, testBox ) {
		describe( title = "MailSpoolerTask serialization (LDEV-6455)", body = function() {

			it( title = "survives an ObjectSave/ObjectLoad round-trip", body = function( currentSpec ) {
				var ver = mailExtVersion();
				expect( ver ).notToBeEmpty( "mail extension is not installed" );

				// load the extension classes straight from their maven bundle
				var js   = { "maven": [ "org.lucee:mail:" & ver ] };
				var smtp = createObject( "java", "org.lucee.extension.mail.smtp.SMTPClient", js ).init();
				var task = createObject( "java", "org.lucee.extension.mail.spooler.MailSpoolerTask", js )
								.init( smtp, [], javacast( "long", 0 ) );

				expect( task.getClass().getName() ).toBe( "org.lucee.extension.mail.spooler.MailSpoolerTask" );

				// write side: the task must be serializable (regressed in 1.1.0.6)
				var bytes = ObjectSave( task );
				expect( isBinary( bytes ) ).toBeTrue( "ObjectSave did not return binary" );

				// read side: it must deserialize again (regressed in 1.1.0.8-RC:
				// ClassNotFoundException for the extension task class under the core classloader)
				var reloaded = ObjectLoad( bytes );
				expect( reloaded.getClass().getName() ).toBe( "org.lucee.extension.mail.spooler.MailSpoolerTask",
					"ObjectLoad could not restore the spool task - the spooler deserializes with a "
					& "classloader that cannot see extension-bundle classes" );
			});

		});
	}
}

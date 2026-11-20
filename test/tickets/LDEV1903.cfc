/**
 * LDEV-1903: cfpop getAll, looping over message.cids and reading message.cids[key] throws "key [...] not found".
 * Runs against a self-contained GreenMail mock (SMTP + POP3), so the spec always executes.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.user = "ldev1903_" & lCase( left( replace( createUUID(), "-", "", "all" ), 16 ) ) & "@localhost";
	variables.pass = "ldev1903pass";


	// a component UDF, not a closure: a closure's variables writes stay in the closure scope
	function startGreenMail() {
		variables.helper = new test._GreenMail();
		variables.greenMail = variables.helper.start( [ "smtp", "pop" ] );
		variables.helper.user( variables.greenMail, variables.user, variables.pass );
	}

	function stopGreenMail() {
		variables.helper.stop( variables.greenMail );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-1903 cfpop message.cids", body=function() {
			beforeEach( startGreenMail );
			afterEach( stopGreenMail );
			it( title="cids has the inline image", body=function( currentSpec ) {
				var message = getMessage();
				expect( message.recordCount ).toBe( 1 );
				expect( isStruct( message.cids ) ).toBeTrue();
				expect( structCount( message.cids ) ).toBe( 1 );
				expect( serializeJSON( message.cids ) ).toInclude( "image001.png@01D40D23.A10F2DD0" );
			});

			it( title="loop collection=message.cids and read message.cids[key]", body=function( currentSpec ) {
				var message = getMessage();
				var result = {};
				loop collection=message.cids item="local.key" {
					result[ key ] = message.cids[ key ];
				}
				expect( structCount( result ) ).toBe( 1 );
				expect( serializeJSON( result ) ).toInclude( "image001.png@01D40D23.A10F2DD0" );
			});

			it( title="same with attachmentPath (as in the ticket)", body=function( currentSpec ) {
				var dir = getTempDirectory() & "ldev1903-" & createUUID() & "/";
				directoryCreate( dir );
				try {
					var message = getMessage( dir );
					var result = {};
					loop collection=message.cids item="local.key" {
						result[ key ] = message.cids[ key ];
					}
					expect( structCount( result ) ).toBe( 1 );
					expect( serializeJSON( result ) ).toInclude( "image001.png@01D40D23.A10F2DD0" );
				}
				finally {
					directoryDelete( dir, true );
				}
			});

			it( title="read message.cids[key] inside a query loop", body=function( currentSpec ) {
				var message = getMessage();
				var result = {};
				loop query=message {
					loop collection=message.cids item="local.key" {
						result[ key ] = message.cids[ key ];
					}
				}
				expect( structCount( result ) ).toBe( 1 );
				expect( serializeJSON( result ) ).toInclude( "image001.png@01D40D23.A10F2DD0" );
			});

		});
	}


	// a fresh mailbox with one html mail that has an inline image (Content-ID), read back with cfpop getAll
	private query function getMessage( string attachmentPath="" ) {
		// Outlook style inline image: filename image001.png, Content-ID image001.png@<id>, like in the ticket
		// Outlook style inline image: filename image001.png, Content-ID image001.png@<id>, like in the ticket
		var imgDir = getTempDirectory() & "ldev1903-img-" & createUUID() & "/";
		directoryCreate( imgDir );
		var img = imgDir & "image001.png";
		// 1x1 png
		fileWrite( img, binaryDecode( "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==", "base64" ) );
		try {
			mail to=variables.user from="ldev1903@localhost" subject="LDEV-1903" type="html"
					server="localhost" port=variables.greenMail.ports.smtp spoolEnable=false {
				mailparam file=img contentID="image001.png@01D40D23.A10F2DD0" disposition="inline";
				echo( '<p>LDEV-1903 <img src="cid:image001.png@01D40D23.A10F2DD0"></p>' );
			}
		}
		finally {
			directoryDelete( imgDir, true );
		}
		var args = {
			server: "localhost",
			port: variables.greenMail.ports.pop,
			username: variables.user,
			password: variables.pass,
			secure: false
		};
		if ( len( arguments.attachmentPath ) ) args.attachmentPath = arguments.attachmentPath;
		local.message = new test._GreenMail().waitFor( function() {
			pop action="getAll" name="local.message" attributeCollection=args;
			return local.message.recordCount > 0 ? local.message : nullValue();
		}, 10000 );
		systemOutput( "LDEV1903 cids: " & serializeJSON( local.message.cids ), true );
		return local.message;
	}
}

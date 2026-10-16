/**
 * LDEV-1903: cfpop getAll, looping over message.cids and reading message.cids[key] throws "key [...] not found".
 * Uses the pop + smtp test services (greenmail in CI, auth disabled so every login creates its own mailbox).
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	variables.popCfg = server.getTestService( "pop" );
	variables.smtpCfg = server.getTestService( "smtp" );

	function run( testResults, testBox ) {
		describe( title="LDEV-1903 cfpop message.cids", skip=notHasServices(), body=function() {

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

	private boolean function notHasServices() {
		return structCount( variables.popCfg ) == 0 || structCount( variables.smtpCfg ) == 0;
	}

	// a fresh mailbox with one html mail that has an inline image (Content-ID), read back with cfpop getAll
	private query function getMessage( string attachmentPath="" ) {
		var user = "ldev1903_" & lCase( left( replace( createUUID(), "-", "", "all" ), 16 ) ) & "@localhost";
		// Outlook style inline image: filename image001.png, Content-ID image001.png@<id>, like in the ticket
		var imgDir = getTempDirectory() & "ldev1903-img-" & createUUID() & "/";
		directoryCreate( imgDir );
		var img = imgDir & "image001.png";
		// 1x1 png
		fileWrite( img, binaryDecode( "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==", "base64" ) );
		try {
			mail to=user from="ldev1903@localhost" subject="LDEV-1903" type="html"
					server=variables.smtpCfg.SERVER port=variables.smtpCfg.PORT_INSECURE spoolEnable=false {
				mailparam file=img contentID="image001.png@01D40D23.A10F2DD0" disposition="inline";
				echo( '<p>LDEV-1903 <img src="cid:image001.png@01D40D23.A10F2DD0"></p>' );
			}
		}
		finally {
			directoryDelete( imgDir, true );
		}
		var args = {
			server: variables.popCfg.SERVER,
			port: variables.popCfg.PORT_INSECURE,
			username: user,
			password: variables.popCfg.PASSWORD,
			secure: false
		};
		if ( len( arguments.attachmentPath ) ) args.attachmentPath = arguments.attachmentPath;
		var start = getTickCount();
		while ( true ) {
			pop action="getAll" name="local.message" attributeCollection=args;
			if ( local.message.recordCount > 0 || getTickCount() - start > 10000 ) break;
			sleep( 200 );
		}
		systemOutput( "LDEV1903 cids: " & serializeJSON( local.message.cids ), true );
		return local.message;
	}
}

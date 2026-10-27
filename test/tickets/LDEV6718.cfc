component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {

	function beforeAll() {
		variables.template = createURI( "LDEV6718/upload.cfm" );
		variables.dirs = [];
	}

	function afterAll() {
		for ( var dir in variables.dirs ) {
			if ( directoryExists( dir ) ) directoryDelete( dir, true );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6718 cffile upload, serverFileExt for a file without an extension", function() {

			it( title="default nameconflict gives an empty string", body=function( currentSpec ) {
				var result = upload( newDir(), "README", "error" );
				expectNoExtension( result, "README" );
			});

			it( title="nameconflict=overwrite on an existing file gives an empty string", body=function( currentSpec ) {
				var dir = newDir();
				fileWrite( dir & "README", "old" );
				var result = upload( dir, "README", "overwrite" );
				expectNoExtension( result, "README" );
			});

			it( title="nameconflict=makeunique without an existing file gives an empty string", body=function( currentSpec ) {
				var result = upload( newDir(), "README", "makeunique" );
				expectNoExtension( result, "README" );
			});

			// the unique name gets a generated extension, serverFileExt has to match the stored file
			it( title="nameconflict=makeunique on an existing file matches the stored file", body=function( currentSpec ) {
				var dir = newDir();
				fileWrite( dir & "README", "old" );
				var result = upload( dir, "README", "makeunique" );
				expectMatchesServerFile( result );
				expect( result.serverFile ).notToBe( "README" );
			});

			it( title="nameconflict=forceunique matches the stored file", body=function( currentSpec ) {
				var result = upload( newDir(), "README", "forceunique" );
				expectMatchesServerFile( result );
				expect( result.serverFile ).notToBe( "README" );
			});

			it( title="a file with an extension still gives its extension", body=function( currentSpec ) {
				var result = upload( newDir(), "notes.txt", "error" );
				expect( result.serverFileExtExists ).toBeTrue();
				expect( result.serverFileExt ).toBe( "txt" );
				expect( result.clientFileExt ).toBe( "txt" );
				expect( result.serverFile ).toBe( "notes.txt" );
				expect( result.saved ).toBeTrue();
			});
		});
	}

	private void function expectNoExtension( required struct result, required string serverFile ) {
		expect( arguments.result.serverFileExtExists ).toBeTrue();
		expect( arguments.result.serverFileExt ).toBe( "" );
		expect( arguments.result.clientFileExt ).toBe( "" );
		expect( arguments.result.serverFile ).toBe( arguments.serverFile );
		expect( arguments.result.saved ).toBeTrue();
	}

	private void function expectMatchesServerFile( required struct result ) {
		var name = arguments.result.serverFile;
		expect( arguments.result.serverFileExtExists ).toBeTrue();
		expect( arguments.result.serverFileExt ).toBe( find( ".", name ) ? listLast( name, "." ) : "" );
		expect( arguments.result.clientFileExt ).toBe( "" );
		expect( arguments.result.saved ).toBeTrue();
	}

	private string function newDir() {
		var dir = getTempDirectory() & "ldev6718-" & createUUID() & "/";
		directoryCreate( dir );
		arrayAppend( variables.dirs, dir );
		return dir;
	}

	private struct function upload( required string destination, required string fileName, required string nameconflict ) {
		var nl = chr( 13 ) & chr( 10 );
		var boundary = "LDEV6718Boundary";
		var body = "--" & boundary & nl
			& 'Content-Disposition: form-data; name="myFile"; filename="#arguments.fileName#"' & nl
			& "Content-Type: text/plain" & nl & nl
			& "hello" & nl
			& "--" & boundary & "--" & nl;
		var res = _internalRequest(
			template: variables.template,
			method: "POST",
			urls: { destination: arguments.destination, nameconflict: arguments.nameconflict },
			headers: { "Content-Type": "multipart/form-data; boundary=" & boundary },
			body: charsetDecode( body, "utf-8" )
		);
		return deserializeJSON( res.filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}

component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {

	function beforeAll() {
		variables.template = createURI( "LDEV2285/upload.cfm" );
		variables.dirs = [];
	}

	function afterAll() {
		for ( var dir in variables.dirs ) {
			if ( directoryExists( dir ) ) directoryDelete( dir, true );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2285 the client file name of an upload is taken as a plain file name", function() {

			it( title="a colon in the file name is replaced", body=function( currentSpec ) {
				var r = uploadOne( "tag", "MyFunny:File.pdf" );
				expect( r.clientFile ).toBe( "MyFunny_File.pdf" );
				expect( r.clientFileName ).toBe( "MyFunny_File" );
				expect( r.clientFileExt ).toBe( "pdf" );
				expect( r.serverFile ).toBe( "MyFunny_File.pdf" );
				expect( r.saved ).toBeTrue();
				expect( r.size ).toBe( 5 );
			});

			it( title="other characters not allowed on Windows are replaced", body=function( currentSpec ) {
				var r = uploadOne( "tag", "a*b?c<d>e|f.txt" );
				expect( r.serverFile ).toBe( "a_b_c_d_e_f.txt" );
				expect( r.saved ).toBeTrue();
			});

			it( title="a Windows full path only keeps the file name", body=function( currentSpec ) {
				var r = uploadOne( "tag", "C:\Users\x\file.pdf" );
				expect( r.clientFile ).toBe( "file.pdf" );
				expect( r.clientDirectory ).toBe( "C:/Users/x" );
				expect( r.serverFile ).toBe( "file.pdf" );
				expect( r.saved ).toBeTrue();
				expect( r.size ).toBe( 5 );
			});

			it( title="a plain file name has no client directory", body=function( currentSpec ) {
				var r = uploadOne( "tag", "notes.txt" );
				expect( r.clientFile ).toBe( "notes.txt" );
				expect( r.clientDirectory ).toBe( "" );
				expect( r.serverFile ).toBe( "notes.txt" );
			});

			it( title="umlauts and other unicode characters are kept", body=function( currentSpec ) {
				var r = uploadOne( "tag", "Öffnungszeiten.pdf" );
				expect( r.serverFile ).toBe( "Öffnungszeiten.pdf" );
				expect( r.saved ).toBeTrue();
				r = uploadOne( "tag", "日本語 ファイル.txt" );
				expect( r.serverFile ).toBe( "日本語 ファイル.txt" );
				expect( r.saved ).toBeTrue();
			});

			it( title="trailing dots and spaces are removed", body=function( currentSpec ) {
				var r = uploadOne( "tag", "report.pdf. " );
				expect( r.clientFile ).toBe( "report.pdf" );
				expect( r.serverFile ).toBe( "report.pdf" );
				expect( r.saved ).toBeTrue();
			});

			it( title="a file name with nothing usable left is rejected", body=function( currentSpec ) {
				var res = upload( "tag", [ "   " ] );
				expect( res ).toHaveKey( "error" );
				res = upload( "tag", [ " . " ] );
				expect( res ).toHaveKey( "error" );
			});

			it( title="fileUpload() takes the plain file name", body=function( currentSpec ) {
				var r = uploadOne( "function", "C:\Users\x\My:File.pdf" );
				expect( r.clientFile ).toBe( "My_File.pdf" );
				expect( r.serverFile ).toBe( "My_File.pdf" );
				expect( r.saved ).toBeTrue();
			});

			it( title="cffile uploadAll takes the plain file names", body=function( currentSpec ) {
				var res = upload( "uploadAll", [ "one:a.txt", "C:\Users\x\two.txt" ] );
				expect( res ).toHaveKey( "results" );
				expect( arrayLen( res.results ) ).toBe( 2 );
				expect( res.results[ 1 ].serverFile ).toBe( "one_a.txt" );
				expect( res.results[ 2 ].serverFile ).toBe( "two.txt" );
			});

			it( title="fileUploadAll() takes the plain file names", body=function( currentSpec ) {
				var res = upload( "functionAll", [ "one:a.txt", "C:\Users\x\two.txt" ] );
				expect( res ).toHaveKey( "results" );
				expect( arrayLen( res.results ) ).toBe( 2 );
				expect( res.results[ 1 ].serverFile ).toBe( "one_a.txt" );
				expect( res.results[ 2 ].serverFile ).toBe( "two.txt" );
			});
		});
	}

	private struct function uploadOne( required string mode, required string fileName ) {
		var res = upload( arguments.mode, [ arguments.fileName ] );
		expect( res ).toHaveKey( "results", res.error ?: "" );
		return res.results[ 1 ];
	}

	private struct function upload( required string mode, required array fileNames ) {
		var dir = getTempDirectory() & "ldev2285-" & createUUID() & "/";
		directoryCreate( dir );
		arrayAppend( variables.dirs, dir );
		var nl = chr( 13 ) & chr( 10 );
		var boundary = "LDEV2285Boundary";
		var body = "";
		var i = 0;
		for ( var fileName in arguments.fileNames ) {
			i++;
			body &= "--" & boundary & nl
				& 'Content-Disposition: form-data; name="#( i == 1 ? "myFile" : "myFile" & i )#"; filename="#fileName#"' & nl
				& "Content-Type: text/plain" & nl & nl
				& "hello" & nl;
		}
		body &= "--" & boundary & "--" & nl;
		var res = _internalRequest(
			template: variables.template,
			method: "POST",
			urls: { destination: dir, mode: arguments.mode },
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

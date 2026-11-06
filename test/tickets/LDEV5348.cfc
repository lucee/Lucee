component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {

	function beforeAll() {
		variables.template = createURI( "LDEV5348/upload.cfm" );
		variables.dirs = [];
	}

	function afterAll() {
		for ( var dir in variables.dirs ) {
			if ( directoryExists( dir ) ) directoryDelete( dir, true );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5348 cffile/fileUpload/fileUploadAll continueOnError", function() {

			describe( "default behaviour is unchanged", function() {

				it( title="fileUploadAll throws for a rejected file", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "funcAll", dir, "error", [ file( "bad.png" ), file( "good.txt" ) ] );
					expect( res.exception ).toInclude( "not permitted" );
				});

				it( title="fileUploadAll throws for a rejected file with continueOnError=false", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "funcAll", dir, "error", [ file( "bad.png" ), file( "good.txt" ) ], { continueOnError: false } );
					expect( res.exception ).toInclude( "not permitted" );
				});

				it( title="cffile uploadAll throws for a rejected file", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "tagAll", dir, "error", [ file( "bad.png" ), file( "good.txt" ) ] );
					expect( res.exception ).toInclude( "not permitted" );
				});

				it( title="cffile uploadAll throws if the destination file already exists", body=function( currentSpec ) {
					var dir = newDir();
					fileWrite( dir & "good.txt", "old" );
					var res = upload( "tagAll", dir, "error", [ file( "good.txt" ) ] );
					expect( res.exception ).toInclude( "already exists" );
					expect( fileRead( dir & "good.txt" ) ).toBe( "old" );
				});

				it( title="fileUpload throws for a form field that does not exist", body=function( currentSpec ) {
					var res = upload( "funcOne", newDir(), "error", [ file( "good.txt" ) ], { field: "nope" } );
					expect( res.exception ).toInclude( "[nope]" );
				});
			});

			describe( "continueOnError=true", function() {

				it( title="fileUploadAll uploads the remaining files and reports the rejected one", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "funcAll", dir, "error", [ file( "bad.png" ), file( "good.txt" ) ], { continueOnError: true, errors: "uploadErrors" } );
					expect( res.exception ).toBe( "" );
					expect( res.result ).toHaveLength( 2 );

					var bad = entry( res.result, "bad.png" );
					expect( bad.fileWasSaved ).toBeFalse();
					expect( bad.reason ).toBe( "INVALID_FILE_TYPE" );
					expect( bad.clientFile ).toBe( "bad.png" );
					expect( bad.clientFileExt ).toBe( "png" );
					expect( bad.clientFileName ).toBe( "bad" );
					expect( bad.message ).toInclude( "not permitted" );

					var good = entry( res.result, "good.txt" );
					expect( good.fileWasSaved ).toBeTrue();
					expect( good.serverFile ).toBe( "good.txt" );

					expect( fileExists( dir & "good.txt" ) ).toBeTrue();
					expect( fileExists( dir & "bad.png" ) ).toBeFalse();

					expect( res.errors ).toHaveLength( 1 );
					expect( res.errors[ 1 ].reason ).toBe( "INVALID_FILE_TYPE" );
					expect( res.errors[ 1 ].clientFile ).toBe( "bad.png" );
					expect( res.errors[ 1 ].clientFileExt ).toBe( "png" );
					expect( res.errors[ 1 ].clientFileName ).toBe( "bad" );
					expect( res.errors[ 1 ].message ).toInclude( "not permitted" );
				});

				it( title="cffile uploadAll uploads the remaining files and fills the errors variable", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "tagAll", dir, "error", [ file( "bad.png" ), file( "good.txt" ), file( "other.txt" ) ], { continueOnError: true, errors: "myErrors" } );
					expect( res.exception ).toBe( "" );
					expect( res.result ).toHaveLength( 3 );
					expect( entry( res.result, "bad.png" ).fileWasSaved ).toBeFalse();
					expect( entry( res.result, "good.txt" ).fileWasSaved ).toBeTrue();
					expect( entry( res.result, "other.txt" ).fileWasSaved ).toBeTrue();
					expect( fileExists( dir & "good.txt" ) ).toBeTrue();
					expect( fileExists( dir & "other.txt" ) ).toBeTrue();
					expect( res.errors ).toHaveLength( 1 );
					expect( res.errors[ 1 ].reason ).toBe( "INVALID_FILE_TYPE" );
				});

				it( title="cffile uploadAll without result puts the errors into cffile.uploadAllErrors", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "tagAllDefault", dir, "error", [ file( "bad.png" ), file( "good.txt" ) ], { continueOnError: true } );
					expect( res.exception ).toBe( "" );
					expect( res.result.uploadAllErrors ).toHaveLength( 1 );
					expect( res.result.uploadAllErrors[ 1 ].reason ).toBe( "INVALID_FILE_TYPE" );
					expect( res.result.uploadAllErrors[ 1 ].clientFile ).toBe( "bad.png" );
				});

				it( title="a file that already exists is reported as FILE_EXISTS and is not overwritten", body=function( currentSpec ) {
					var dir = newDir();
					fileWrite( dir & "good.txt", "old" );
					var res = upload( "funcAll", dir, "error", [ file( "good.txt", "new" ), file( "other.txt" ) ], { continueOnError: true, errors: "uploadErrors" } );
					expect( res.exception ).toBe( "" );
					expect( res.result ).toHaveLength( 2 );
					var exists = entry( res.result, "good.txt" );
					expect( exists.fileWasSaved ).toBeFalse();
					expect( exists.reason ).toBe( "FILE_EXISTS" );
					expect( exists.message ).toInclude( "already exists" );
					expect( exists.dest ).toInclude( "good.txt" );
					expect( entry( res.result, "other.txt" ).fileWasSaved ).toBeTrue();
					expect( fileRead( dir & "good.txt" ) ).toBe( "old" );
					expect( fileExists( dir & "other.txt" ) ).toBeTrue();
					expect( res.errors ).toHaveLength( 1 );
					expect( res.errors[ 1 ].reason ).toBe( "FILE_EXISTS" );
				});

				it( title="the errors array is empty if all files are uploaded", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "funcAll", dir, "error", [ file( "good.txt" ), file( "other.txt" ) ], { continueOnError: true, errors: "uploadErrors" } );
					expect( res.exception ).toBe( "" );
					expect( res.result ).toHaveLength( 2 );
					expect( entry( res.result, "good.txt" ).fileWasSaved ).toBeTrue();
					expect( entry( res.result, "other.txt" ).fileWasSaved ).toBeTrue();
					expect( res.errors ).toBeArray();
					expect( res.errors ).toHaveLength( 0 );
				});

				it( title="fileUpload returns the error for a rejected file", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "funcOne", dir, "error", [ file( "bad.png" ) ], { continueOnError: true, field: "file1" } );
					expect( res.exception ).toBe( "" );
					expect( res.result.fileWasSaved ).toBeFalse();
					expect( res.result.reason ).toBe( "INVALID_FILE_TYPE" );
					expect( res.result.clientFile ).toBe( "bad.png" );
					expect( fileExists( dir & "bad.png" ) ).toBeFalse();
				});

				it( title="fileUpload still saves an accepted file", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "funcOne", dir, "error", [ file( "good.txt" ) ], { continueOnError: true, field: "file1" } );
					expect( res.exception ).toBe( "" );
					expect( res.result.fileWasSaved ).toBeTrue();
					expect( fileExists( dir & "good.txt" ) ).toBeTrue();
				});

				it( title="fileUpload returns FORM_FILE_NOT_FOUND for a form field that does not exist", body=function( currentSpec ) {
					var res = upload( "funcOne", newDir(), "error", [ file( "good.txt" ) ], { continueOnError: true, field: "nope" } );
					expect( res.exception ).toBe( "" );
					expect( res.result.fileWasSaved ).toBeFalse();
					expect( res.result.reason ).toBe( "FORM_FILE_NOT_FOUND" );
				});

				it( title="cffile upload returns the error for a rejected file", body=function( currentSpec ) {
					var dir = newDir();
					var res = upload( "tagOne", dir, "error", [ file( "bad.png" ) ], { continueOnError: true, field: "file1", errors: "myErrors" } );
					expect( res.exception ).toBe( "" );
					expect( res.result.fileWasSaved ).toBeFalse();
					expect( res.result.reason ).toBe( "INVALID_FILE_TYPE" );
					expect( res.errors ).toHaveLength( 1 );
				});

				it( title="a destination problem still throws", body=function( currentSpec ) {
					var res = upload( "funcAll", getTempDirectory() & "ldev5348-missing-" & createUUID() & "/sub/dir/", "error", [ file( "good.txt" ) ], { continueOnError: true } );
					expect( res.exception ).toInclude( "doesn't exist" );
				});
			});
		});
	}

	// the order of the uploaded files is not guaranteed
	private struct function entry( required array result, required string clientFile ) {
		for ( var item in arguments.result ) {
			if ( item.clientFile == arguments.clientFile ) return item;
		}
		throw( message="no entry for the file [#arguments.clientFile#]", type="expression" );
	}

	private struct function file( required string fileName, string content = "hello" ) {
		return { fileName: arguments.fileName, content: arguments.content };
	}

	private string function newDir() {
		var dir = getTempDirectory() & "ldev5348-" & createUUID() & "/";
		directoryCreate( dir );
		arrayAppend( variables.dirs, dir );
		return dir;
	}

	private struct function upload( required string mode, required string destination, required string nameconflict, required array files, struct extra = {} ) {
		var nl = chr( 13 ) & chr( 10 );
		var boundary = "LDEV5348Boundary";
		var body = "";
		var index = 0;
		for ( var f in arguments.files ) {
			index++;
			body &= "--" & boundary & nl
				& 'Content-Disposition: form-data; name="file#index#"; filename="#f.fileName#"' & nl
				& "Content-Type: text/plain" & nl & nl
				& f.content & nl;
		}
		body &= "--" & boundary & "--" & nl;
		var urls = { mode: arguments.mode, destination: arguments.destination, nameconflict: arguments.nameconflict };
		structAppend( urls, arguments.extra );
		var res = _internalRequest(
			template: variables.template,
			method: "POST",
			urls: urls,
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

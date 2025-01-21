component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll() {
		variables.dir = getTempDirectory() & "fileDelete\";
		if(directoryExists(dir)) {
			afterAll();
		}
		cfdirectory(directory="#dir#" action="create" mode="777");
	}

	function run() {
		describe( "testcase for FileDelete()", function() {
			it(title = "Checking FileDelete()", body = function( currentSpec ) {
				var src = variables.dir & "exists.txt";
				fileWrite(src, "text");
				fileDelete(src);
				expect(FileExists(src)).toBeFalse();
			});

			it(title = "Checking FileDelete() file in use", body = function( currentSpec ) {
				if (!isWindows()) return;
				var src = variables.dir & "locked.txt";

				systemOutput( "file in use", true );

				FileWrite( src, "text" );
				var error = "";
				try {
					var f = createObject("java", "java.io.File").init(src);
					var fos =  createObject("java", "java.io.FileOutputStream").init(f); // lock file
					FileDelete( src );
				} catch ( e ){
					error = e.message;
				} finally {
					fos.close();
				}
				expect( FileExists( src ) ).toBeTrue();
				expect( error ).notToBe( "" );
			});

			// disabled due to https://luceeserver.atlassian.net/browse/LDEV-3312
			xit(title = "Checking FileDelete() readonly", body = function( currentSpec ) {
				var src = variables.dir & "readonly.txt";
				fileWrite( src, "text" );
				if ( isWindows() );
					fileSetAttribute( src,"readOnly" );
				else
					fileSetAccessMode( src, "444" );
				var error = "";
				try {
					FileDelete( src );
				} catch ( e ){
					error = e.message;
				} finally {
					//systemOutput( fileInfo(src), true);
					if ( isWindows() );
						fileSetAttribute( src, "normal" );
					else
						fileSetAccessMode( src, "777" );
					//systemOutput( fileInfo(src), true);
					FileDelete( src );
				}

				expect( FileExists( src ) ).toBeFalse();
				expect( error ).notToBe( "" );
			});

			it(title = "Checking FileDelete() missing file", body = function( currentSpec ) {
				var src = variables.dir & "missing.txt";
				FileWrite( src, "text" );
				var handle = FileOpen( src );
				var res = handle.getResource();
				var error = "";
				try {
					FileDelete( handle );
					res.remove(false); // call directly as fileDelete does an exists check
				} catch ( e ){
					error = e.message;;
				} finally {
					FileClose( handle );
				}
				expect( error ).notToBe( "" );
			});
		});
	}
	
	function afterAll() {
		directoryDelete(variables.dir, true);
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}

}
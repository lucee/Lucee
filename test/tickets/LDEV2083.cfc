component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.base = GetDirectoryFromPath( getCurrentTemplatepath() );
		if( !directoryExists( base & "LDEV2083" ) ){
			directoryCreate( base & 'LDEV2083' );
		}
		variables.structPath={
			"path": base & "LDEV2083\example.m4a",
			"txtPath": base & "LDEV2083\test.txt",
			"mp3Path": base & "LDEV2083\sound.mp3"
		};
		cfloop (collection="#structPath#" item = "filePath"){
			if( !fileExists( structFind( structPath, filePath ) ) ){
				FileWrite( structFind( structPath, filePath ), '' );
			}
		}
	}
	function afterAll(){
		if( directoryExists( base & "LDEV2083" ) ){
			directoryDelete( base & "LDEV2083", true );
		}
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2083", function() {
			it(title = "FileGetMimeType() for .m4a throws java.lang.NullPointerException", body = function( currentSpec ) {
				expect( fileExists( structFind( structPath, "path" ) ) ).toBe( TRUE );
				expect( fileGetMimeType( structFind( structPath, "path"), false ) ).toBe( 'audio/mp4' );
			});

			it(title = "not existing file", body = function( currentSpec ) {
				expect( fileExists( "notexisting.m4a" ) ).toBe( FALSE );
				expect( fileGetMimeType( "notexisting.m4a", false) ).toBe( 'audio/mp4' );
			});

			it(title = "FileGetMimeType() for .txt file", body = function( currentSpec ) {
				expect( fileExists( structFind( structPath, "txtPath") ) ).toBe( TRUE );
				expect( fileGetMimeType( structFind( structPath, "txtPath"), false ) ).toBe( 'text/plain' );
			});

			it(title = "FileGetMimeType() for .mp3 file", body = function( currentSpec ) {
				expect( fileExists( structFind( structPath, "mp3Path" ) ) ).toBe( TRUE );
				expect( fileGetMimeType( structFind( structPath, "mp3Path"), false ) ).toBe( 'audio/mpeg' );
			});
		});
	}
}
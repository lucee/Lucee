component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.base = GetDirectoryFromPath( getCurrentTemplatepath() );
		variables.path = base & "fileSetAttribute";
		if ( !directoryExists( variables.path ) ){
			directoryCreate( variables.path );
		}
	}

	function isNotSupported() {
		var isWindows =find("Windows", server.os.name );
		if (isWindows > 0 ) return false;
		else return  true;
	}

	function afterAll(){
		if ( directoryExists( variables.path ) ){
			directoryDelete( variables.path, true );
		}
	}
	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-1880", skip=isNotSupported(),  body = function() {

			beforeEach( function( currentSpec ) {
				if ( !fileExists( path & "/example_LDEV1880.txt" ) ){
					variables.myfile = FileOpen(path, "write" );
					FileWrite( path & "/example_LDEV1880.txt","This is a sample file content" );
				}
			});
			afterEach( function( currentSpec ) {
				if ( fileExists( path & "/example_LDEV1880.txt" ) ){
					fileDelete( path & "/example_LDEV1880.txt" );
				}
			});

			it( title = "checking the file with Archive Attribute", body = function( currentSpec ) {
				fileSetAttribute( path & "/example_LDEV1880.txt",'Archive' );
				expect( getFileInfo( path & "/example_LDEV1880.txt").isArchive ).toBeTrue();
			});
			it( title = "checking the file with System Attribute", body = function( currentSpec ) {
				fileSetAttribute( path & "/example_LDEV1880.txt", 'System' );
				expect( getFileInfo( path & "/example_LDEV1880.txt").isSystem ).toBeTrue();
			});
			it( title = "checking the file with readOnly Attribute", body = function( currentSpec ) {
				fileSetAttribute( path & "/example_LDEV1880.txt", 'readOnly' );
				var info = getFileInfo( path & "/example_LDEV1880.txt" );
				expect( info.canRead ).toBeTrue();
				expect( info.canWrite ).toBeFalse();
			});
			it( title = "checking the file with Hidden Attribute", body = function( currentSpec ) {
				fileSetAttribute( path & "/example_LDEV1880.txt", 'Hidden' );
				expect( getFileInfo( path & "/example_LDEV1880.txt" ).isHidden ).toBeTrue();
			});
			it( title = "checking the file with Normal Attribute", body = function( currentSpec ) {
				fileSetAttribute( path & "/example_LDEV1880.txt", 'Normal' );
				var info = getFileInfo( path & "/example_LDEV1880.txt" );
				expect( info.canRead ).toBeTrue();
				expect( info.canWrite ).toBeTrue();
				expect( info.isHidden ).toBeFalse();
				expect( info.isSystem ).toBeFalse();
				expect( info.isArchive ).toBeFalse();
			});
		});
		describe( "Testcase for LDEV-2410", function() {
			it( title = "Checking changing file attribute between NORMAL and READONLY", body = function( currentSpec ) {
				var testFile = path & "\ro_normal_LDEV2410_#CreateUUID()#.txt";
				FileWrite(testFile, "I am in normal file");

				FileSetAttribute( testFile, 'normal' );
				var info = getFileInfo( testFile );
				expect(info.canWrite).toBe( true );

				FileSetAttribute( testFile ,'readOnly' );
				info = getFileInfo( testFile );
				expect( info.canWrite ).toBe( false );

				FileSetAttribute( testFile, 'normal' );
				info = getFileInfo( testFile );
				expect( info.canWrite ).toBe( true );

				FileSetAttribute( testFile ,'readOnly' );
				info = getFileInfo( testFile );
				expect( info.canWrite ).toBe( false );
				
			});
		});
		describe( title="Testcase for LDEV-2349", body=function() {
			it( title="Checking FileCopy- Destination file access mode with file attribute [readonly]",body=function( currentSpec ) {
				var testFile =  path & "\newfile_#CreateUUID()#.txt";
				var destFile =  path & "\destfile_#CreateUUID()#.txt";

				fileWrite( testFile, "This is new file");
				fileSetAttribute( testFile, "readonly" );
				fileCopy( testFile, destFile ); // dest file should also be read only

				expect( getFileInfo( testfile ).canwrite ).ToBeFalse();
				expect( getFileInfo( destfile ).canwrite ).ToBeFalse();
			});
			it( title="Checking FileCopy - Destination file access mode with file attribute [hidden] (windows)",skip=isNotSupported(),body=function( currentSpec ) {
				var testFile =  path & "\copy_hidden_newfile_#CreateUUID()#.txt";
				var destFile =  path & "\copy_hidden_destfile_#CreateUUID()#.txt";

				fileWrite( testFile, "This is new file");
				fileSetAttribute( testFile, "hidden");

				fileCopy( testFile, destFile ); // dest file should also be hidden

				expect( getFileInfo( testfile ).isHidden ).ToBeTrue();
				expect( getFileInfo( destfile ).isHidden ).ToBeTrue();
			});

			it( title="Checking FileCopy - Destination file access mode with file attribute [archive] (windows)",skip=isNotSupported(),body=function( currentSpec ) {
				var testFile =  path & "\copy_archived_newfile_#CreateUUID()#.txt";
				var destFile =  path & "\copy_archived_destfile_#CreateUUID()#.txt";

				fileWrite( testFile, "This is new file");
				fileSetAttribute( testFile, "archive");

				fileCopy( testFile, destFile ); // dest file should also be archived

				expect( getFileInfo( testfile ).isArchive ).ToBeTrue();
				expect( getFileInfo( destfile ).isArchive ).ToBeTrue();
			});

			it( title="Checking FileCopy- Destination file access mode with file attribute normal",body=function( currentSpec ) {
				var testFile =  path & "\copy_normal_newfile_#CreateUUID()#.txt";
				var destFile =  path & "\copy_normal_destfile_#CreateUUID()#.txt";

				fileWrite(testFile, "This is new file" );
				fileSetAttribute( testFile, "normal" );
				fileCopy( testFile, destFile );

				expect( getFileInfo( testfile ).isHidden ).ToBeFalse();
				expect( getFileInfo( destfile ).isHidden ).ToBeFalse();

				expect( getFileInfo( testfile ).canWrite ).ToBeTrue();
				expect( getFileInfo( destfile ).canWrite ).ToBeTrue();
			});
		});
	}
}

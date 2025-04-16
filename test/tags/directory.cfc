component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.SEP = Server.separator.file;
		variables.path = getTempDirectory() & "tagDirectory" & sep;
		if ( directoryExists( path ) )
			directoryDelete( path, true );
		directoryCreate( path );
	}
	function afterAll(){
		directoryDelete( path, true );
	}

	function run( testResults , testBox ) {
		describe( "test case for cfdirectory", function() {
			it(title = "cfdirectory action=create", body = function( currentSpec ) {
				var _dir = path & "create";
				directory action="create" directory=_dir;
				assertTrue(directoryExists(_dir));
			});

			it(title = "cfdirectory action=list", body = function( currentSpec ) {
				var _dir = path & "list";
				directory action="create" directory=_dir;
				var tmp = getTempFile(_dir, "list-me");
				directory action="list" directory=_dir name="local.list";
				expect( listLast( tmp, "\/" ) ).toBe( list.name );
				expect( "file" ).toBe( list.type );
			});

			it(title = "cfdirectory action=copy", body = function( currentSpec ) {
				directory action="create" directory="#path#\srcCopyDir";
				getTempFile("#path#\srcCopyDir", "copy-me-too");
				directory action="copy" directory="#path#\srcCopyDir" newDirectory="copyDirectory";
				assertTrue(directoryExists("#path#\copyDirectory"));
				expect( len(directoryList("#path#\copyDirectory"))).toBe( 1 );
			});

			it(title = "cfdirectory action=rename", body = function( currentSpec ) {
				directory action="create" directory="#path#\srcRenameDir";
				directory action="rename" directory="#path#\srcRenameDir" newDirectory="renameDirectory";
				assertFalse(directoryExists("#path#\srcRenameDir"));
				assertTrue(directoryExists("#path#\renameDirectory"));

			});

			it(title = "cfdirectory action=delete", body = function( currentSpec ) {
				var _dir = "#path#\deleteDir";
				directory action="create" directory="#_dir#";
				directory action= "delete" directory="#_dir#";
				assertFalse(directoryExists("#_dir#"));
			});

			it(title = "cfdirectory action=forceDelete", body = function( currentSpec ) {
				var _dir = "#path#\forceDeleteDir";
				directory action="create" directory="#_dir#";
				getTempFile(_dir, "force-deletes-me-too");
				directory action="forcedelete" directory="#_dir#";
				assertFalse( directoryExists( _dir ) );
			});
		});

		describe( "test case for cfdirectory action=create with mode", function() {
			it(title = "Checking mode directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = path & "mode";
				directory action="create" directory="#testDir#" mode="775";

				expect( directoryExists( testDir ) ).toBeTrue();
				expect( directoryInfo( testDir ).mode ).toBe("775");
			});

			it(title = "Checking default mode directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = path & "mode-default";
				directory action="create" directory="#testDir#";

				expect( directoryExists( testDir ) ).toBeTrue();
				expect( directoryInfo( testDir ).mode ).toBe("755");
			});

			it(title = "Checking preserve mode -1 directoryCreate()", skip=isWindows(), body = function( currentSpec ) {
				var testDir = path & "mode-1";
				directory action="create" directory="#testDir#" mode="-1";

				expect( directoryExists( testDir ) ).toBeTrue();
				
				var parentDirMode = directoryInfo( getTempDirectory() ).mode;
				expect( directoryInfo( testDir ).mode ).toBe( parentDirMode );
			});
		});

	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}
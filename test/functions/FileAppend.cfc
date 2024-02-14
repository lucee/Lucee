component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
		if(directoryExists(dir)){
			afterAll();
		}
		cfdirectory(directory="#dir#" action="create" mode="777");
	}

	function run( testResults , testBox ) {
		describe( "test case for FileAppend", function() {

			it(title = "Checking with FileAppend", body = function( currentSpec ) {
				var _file = getTempFile( dir, "fileAppend", "txt" );
				fileWrite( _file, "ABC" );
				fileAppend( _file, "DEF", "UTF-8" );
				assertEquals( "ABCDEF", trim( fileRead( _file ) ) );
			});

			it(title = "Checking with FileAppend Object - write", body = function( currentSpec ) {
				var _file = getTempFile( dir, "fileAppend", "txt" );
				var _fileResource = fileOpen( _file, "write" );
				fileAppend( _fileResource, "ABCDEFGHI", "UTF-8" );
				fileClose( _fileResource );
				assertEquals( "ABCDEFGHI", trim( fileRead (_fileResource) ) );
			});

			it(title = "Checking with FileAppend Object - append", body = function( currentSpec ) {
				var _file = getTempFile( dir, "fileAppend", "txt" );
				fileWrite( _file, "abc" );
				var _fileResource = fileOpen( _file, "append" );
				fileAppend( _fileResource, "ABCDEFGHI", "UTF-8" );
				fileClose( _fileResource );
				assertEquals( "abcABCDEFGHI", trim( fileRead( _fileResource ) ) );
			});

			it(title = "Checking with FileAppend", body = function( currentSpec ) {
				var f = getTempFile( getTempDirectory(), "fileAppend", "txt" );
				fileAppend( f, "123" );
				expect( fileRead( f ) ).toBe( "123" );
				fileAppend( f, "456" );
				expect( fileRead( f ) ).toBe( "123456" );
				fileDelete( f );
			});
		});
	}

	function afterAll(){
		directorydelete( dir, true );
	}
}

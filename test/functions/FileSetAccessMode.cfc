component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "fileSetAccessMode", function(){

			it( title="test access modes", skip=isNotUnix(), body=function(){
				
				var dir = getTempDirectory() & "fileSetAccessMode/";
				if ( !directoryExists( dir ) ){
					directoryCreate( dir );
				};

				var tests = [];

				arrayAppend( tests, _dir( dir, "755", "755" ) );
				arrayAppend( tests, _dir( dir, "644", "644" ) );

				arrayAppend( tests, _file( dir, "755/644.txt", "644" ) );
				arrayAppend( tests, _file( dir, "755/743.txt", "743" ) );
				arrayAppend( tests, _file( dir, "755/043.txt", "043" ) );

				arrayAppend( tests, _file( dir, "644/400.txt", "400" ) );

				var files = directoryList( dir, true, "query");
				var st = QueryToStruct( files, "name" );

				loop array=tests index="local.test"{
					systemOutput( test, true );
				}

				loop array=st index="local.item"{
					systemOutput( item, true );
				}

			});

		} );
	}

	private function _dir(parent, name, mode){
		var dir = directoryCreate( name & mode );
		fileSetAccessMode( dir, mode );
		return {
			name: dir,
			mode: mode,
			type: "dir"
		};
	}

	private function _file(parent, name, mode){
		var file = fileWrite( parent & name, "" );
		fileSetAccessMode( file, mode );
		return {
			name: file,
			mode: mode,
			type: "file"
		};
	}	

	private function isNotUnix(){
		return (server.os.name != "windows");
	}

}

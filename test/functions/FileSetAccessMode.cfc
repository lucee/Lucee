component extends="org.lucee.cfml.test.LuceeTestCase" { 

	function beforeAll(){
		variables.dir = getTempDirectory() & "fileSetAccessMode/";
		if ( directoryExists( dir ) )
			directoryDelete( dir, true );
		directoryCreate( dir );	
	};

	function afterAll(){
		if ( directoryExists( dir ) ){
			directoryDelete( dir, true );
		};
	};

	function run( testResults, testBox ){ 
		describe( "fileSetAccessMode", function(){
			it( title="test access modes", skip=isNotUnix(), body=function(){
				var tests = [];

				arrayAppend( tests, _dir( dir, "755", "755" ) );
				arrayAppend( tests, _dir( dir, "777", "777" ) );
				arrayAppend( tests, _dir( dir, "644", "644" ) );

				var files = directoryList( dir, true, "query");
				var st = QueryToStruct( files, "name" );

				loop array=st index="local.item"{
					systemOutput( item, true );
				}

				arrayAppend( tests, _file( dir, "644.txt", "644" ) );
				arrayAppend( tests, _file( dir, "743.txt", "743" ) );
				arrayAppend( tests, _file( dir, "043.txt", "443" ) );
				arrayAppend( tests, _file( dir, "400.txt", "400" ) );

				var files = directoryList( dir, true, "query");
				var st = QueryToStruct( files, "name" );

				loop collection=st item="local.item"{
					systemOutput( item, true );
				}
				loop array=tests item="local.test" {
					systemOutput( test, true );
				}
				loop array=tests item="local.test" {
					systemOutput( test, true );
					var key = mid( test.name, len( dir ) + 1 );
					systemOutput( key, true );
					expect( st ).toHaveKey( key );
					systemOutput( st[ key ], true );
					expect( test.mode ).toBe( st[ key ].mode );
				}

			});
		} );
	}

	private function _dir( parent, name, mode ){
		var dir = parent & name;
		directoryCreate( dir );
		fileSetAccessMode( dir, mode );
		return {
			name: dir,
			mode: mode,
			type: "dir"
		};
	}

	private function _file( parent, name, mode ){
		var file = parent & name;
		fileWrite( file, "" );
		fileSetAccessMode( file, mode );
		return {
			name: file,
			mode: mode,
			type: "file"
		};
	}	

	private function isNotUnix(){
		return (server.os.name contains "windows");
	}

}

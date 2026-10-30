component extends="org.lucee.cfml.test.LuceeTestCase" labels="file,directory" {

	function beforeAll() {
		variables.base = getTempDirectory() & "LDEV4014_" & createUniqueId() & "/";
		directoryCreate( variables.base );
	}

	function afterAll() {
		if ( directoryExists( variables.base ) ) directoryDelete( variables.base, true );
	}

	private string function newRoot( required string type ) {
		if ( arguments.type == "ram" ) return "ram:///LDEV4014_" & createUniqueId() & "/";
		return variables.base & createUniqueId() & "/";
	}

	function run( testResults, testBox ) {

		loop array=[ "local", "ram" ] item="local.type" {
			var type = local.type;

			describe( title="LDEV-4014 fileDelete throwOnMissing on #type#", body=function() {

				it( title="default still throws for a missing file", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root );
					expect( function() {
						fileDelete( root & "missing.txt" );
					}).toThrow( regex="does not exist" );
				});

				it( title="throwOnMissing=true throws for a missing file", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root );
					expect( function() {
						fileDelete( file=root & "missing.txt", throwOnMissing=true );
					}).toThrow( regex="does not exist" );
				});

				it( title="throwOnMissing=false does nothing for a missing file", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root );
					expect( fileDelete( file=root & "missing.txt", throwOnMissing=false ) ).toBeNull();
					expect( fileDelete( root & "missing.txt", false ) ).toBeNull();
					expect( fileExists( root & "missing.txt" ) ).toBeFalse();
				});

				it( title="throwOnMissing=false does nothing for a missing parent directory", body=function( currentSpec ) {
					var root = newRoot( type );
					fileDelete( file=root & "nope/missing.txt", throwOnMissing=false );
					expect( directoryExists( root ) ).toBeFalse();
				});

				it( title="throwOnMissing=false still deletes an existing file", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root );
					fileWrite( root & "a.txt", "a" );
					fileDelete( file=root & "a.txt", throwOnMissing=false );
					expect( fileExists( root & "a.txt" ) ).toBeFalse();
				});

				it( title="throwOnMissing=false still throws for a non empty directory", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root & "sub" );
					fileWrite( root & "sub/a.txt", "a" );
					expect( function() {
						fileDelete( file=root & "sub", throwOnMissing=false );
					}).toThrow();
					expect( fileExists( root & "sub/a.txt" ) ).toBeTrue( "the content must still be there" );
				});

				it( title="throwOnMissing=false still throws for an empty path", body=function( currentSpec ) {
					expect( function() {
						fileDelete( file="", throwOnMissing=false );
					}).toThrow();
				});

			});

			describe( title="LDEV-4014 directoryDelete throwOnMissing on #type#", body=function() {

				it( title="default still throws for a missing directory", body=function( currentSpec ) {
					var root = newRoot( type );
					expect( function() {
						directoryDelete( root & "missing" );
					}).toThrow();
					expect( function() {
						directoryDelete( root & "missing", true );
					}).toThrow();
				});

				it( title="throwOnMissing=true throws for a missing directory", body=function( currentSpec ) {
					var root = newRoot( type );
					expect( function() {
						directoryDelete( path=root & "missing", recurse=true, throwOnMissing=true );
					}).toThrow();
				});

				it( title="throwOnMissing=false does nothing for a missing directory", body=function( currentSpec ) {
					var root = newRoot( type );
					expect( directoryDelete( path=root & "missing", throwOnMissing=false ) ).toBeNull();
					expect( directoryDelete( path=root & "missing", recurse=true, throwOnMissing=false ) ).toBeNull();
					expect( directoryDelete( root & "missing", true, false ) ).toBeNull();
					expect( directoryDelete( root & "missing", false, false ) ).toBeNull();
					expect( directoryExists( root & "missing" ) ).toBeFalse();
				});

				it( title="throwOnMissing=false still deletes an existing empty directory", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root & "empty" );
					directoryDelete( path=root & "empty", throwOnMissing=false );
					expect( directoryExists( root & "empty" ) ).toBeFalse();
				});

				it( title="throwOnMissing=false with recurse=true still deletes an existing tree", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root & "tree/sub" );
					fileWrite( root & "tree/a.txt", "a" );
					fileWrite( root & "tree/sub/b.txt", "b" );
					directoryDelete( path=root & "tree", recurse=true, throwOnMissing=false );
					expect( directoryExists( root & "tree" ) ).toBeFalse();
				});

				it( title="throwOnMissing=false without recurse still throws for a non empty directory", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root & "tree" );
					fileWrite( root & "tree/a.txt", "a" );
					expect( function() {
						directoryDelete( path=root & "tree", recurse=false, throwOnMissing=false );
					}).toThrow( type="application", regex="is not empty" );
					expect( function() {
						directoryDelete( path=root & "tree", throwOnMissing=false );
					}).toThrow( type="application", regex="is not empty" );
					expect( fileExists( root & "tree/a.txt" ) ).toBeTrue( "the content must still be there" );
				});

				it( title="throwOnMissing=false still throws when the path is a file", body=function( currentSpec ) {
					var root = newRoot( type );
					directoryCreate( root );
					fileWrite( root & "a.txt", "a" );
					expect( function() {
						directoryDelete( path=root & "a.txt", recurse=true, throwOnMissing=false );
					}).toThrow( type="application", regex="it's a file" );
					expect( fileExists( root & "a.txt" ) ).toBeTrue( "the file must still be there" );
				});

				it( title="throwOnMissing=false still throws for an empty path", body=function( currentSpec ) {
					expect( function() {
						directoryDelete( path="", throwOnMissing=false );
					}).toThrow();
				});

			});
		}

		describe( title="LDEV-4014 concurrent deletes with throwOnMissing=false", body=function() {

			it( title="several threads removing the same file do not throw", body=function( currentSpec ) {
				var names = [];
				loop from=1 to=20 index="local.round" {
					var file = variables.base & "race_" & round & ".txt";
					fileWrite( file, "x" );
					var round_names = [];
					loop from=1 to=4 index="local.t" {
						var tn = "LDEV4014_f_#round#_#t#_" & createUniqueId();
						arrayAppend( round_names, tn );
						arrayAppend( names, tn );
						thread name=tn file=file {
							try {
								fileDelete( file=attributes.file, throwOnMissing=false );
								thread.error = "";
							}
							catch ( any e ) {
								thread.error = e.message;
							}
						}
					}
					thread action="join" name=arrayToList( round_names );
					expect( fileExists( file ) ).toBeFalse();
				}
				loop array=names item="local.n" {
					expect( cfthread[ n ].error ).toBe( "", "thread [#n#] failed: #cfthread[ n ].error#" );
				}
			});

			it( title="several threads removing the same directory do not throw", body=function( currentSpec ) {
				var names = [];
				loop from=1 to=20 index="local.round" {
					var dir = variables.base & "race_dir_" & round;
					directoryCreate( dir & "/sub" );
					fileWrite( dir & "/sub/a.txt", "x" );
					var round_names = [];
					loop from=1 to=4 index="local.t" {
						var tn = "LDEV4014_d_#round#_#t#_" & createUniqueId();
						arrayAppend( round_names, tn );
						arrayAppend( names, tn );
						thread name=tn dir=dir {
							try {
								directoryDelete( path=attributes.dir, recurse=true, throwOnMissing=false );
								thread.error = "";
							}
							catch ( any e ) {
								thread.error = e.message;
							}
						}
					}
					thread action="join" name=arrayToList( round_names );
					expect( directoryExists( dir ) ).toBeFalse();
				}
				loop array=names item="local.n" {
					expect( cfthread[ n ].error ).toBe( "", "thread [#n#] failed: #cfthread[ n ].error#" );
				}
			});

		});
	}

}

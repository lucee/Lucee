component extends="org.lucee.cfml.test.LuceeTestCase" labels="file,directory" {

	function beforeAll() {
		variables.base = getTempDirectory() & "LDEV4014_" & createUniqueId() & "/";
		directoryCreate( variables.base );
	}

	function afterAll() {
		if ( directoryExists( variables.base ) ) directoryDelete( variables.base, true );
	}

	private void function buildTree( required string dir ) {
		loop from=1 to=4 index="local.a" {
			loop from=1 to=4 index="local.b" {
				var sub = arguments.dir & "/a" & a & "/b" & b;
				directoryCreate( sub, true );
				loop from=1 to=5 index="local.f" {
					fileWrite( sub & "/f" & f & ".txt", "x" );
				}
			}
		}
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-4014 recursive directoryDelete", body=function() {

			it( title="removes a tree in a single thread", body=function( currentSpec ) {
				var dir = variables.base & "single_" & createUniqueId();
				buildTree( dir );
				directoryDelete( dir, true );
				expect( directoryExists( dir ) ).toBeFalse();
			});

			it( title="still reports a missing directory as missing", body=function( currentSpec ) {
				var dir = variables.base & "missing_" & createUniqueId();
				expect( function() {
					directoryDelete( dir, true );
				}).toThrow( regex="exist" );
			});

			it( title="several threads removing the same directory never fail with a raw NoSuchFileException", body=function( currentSpec ) {
				var names = [];
				loop from=1 to=30 index="local.round" {
					var dir = variables.base & "race_" & round;
					buildTree( dir );
					var round_names = [];
					loop from=1 to=6 index="local.t" {
						var tn = "LDEV4014_#round#_#t#_" & createUniqueId();
						arrayAppend( round_names, tn );
						arrayAppend( names, tn );
						thread name=tn dir=dir {
							try {
								directoryDelete( attributes.dir, true );
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
					var err = cfthread[ n ].error;
					// losing the race for the top level directory is expected, any other error is not
					expect( err == "" || reFindNoCase( "doesn't exist|does not exist", err ) > 0 ).toBeTrue( "thread [#n#] failed: #err#" );
				}
			});

			it( title="the same directory can be created and removed again after concurrent deletes", body=function( currentSpec ) {
				var dir = variables.base & "again";
				loop from=1 to=5 index="local.round" {
					buildTree( dir );
					var round_names = [];
					loop from=1 to=6 index="local.t" {
						var tn = "LDEV4014_again_#round#_#t#_" & createUniqueId();
						arrayAppend( round_names, tn );
						thread name=tn dir=dir {
							try {
								directoryDelete( attributes.dir, true );
							}
							catch ( any e ) {
								// the race itself is checked by the previous spec
							}
						}
					}
					thread action="join" name=arrayToList( round_names );
					expect( directoryExists( dir ) ).toBeFalse();
				}
				buildTree( dir );
				directoryDelete( dir, true );
				expect( directoryExists( dir ) ).toBeFalse();
				directoryCreate( dir );
				fileWrite( dir & "/x.txt", "x" );
				fileDelete( dir & "/x.txt" );
				directoryDelete( dir );
				expect( directoryExists( dir ) ).toBeFalse();
			});

		});
	}

}

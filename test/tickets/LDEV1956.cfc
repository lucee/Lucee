component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-1956 directoryCopy and fileCopy check the thread interrupt", function() {

			it( title = "directoryCopy still copies everything when the thread is not interrupted", body = function( currentSpec ) {
				var base = createTree( 20 );
				try {
					directoryCopy( base & "src", base & "trg", true );
					expect( countFiles( base & "trg" ) ).toBe( 20 );
				}
				finally {
					cleanup( base );
				}
			} );

			it( title = "directoryCopy stops when the thread gets interrupted and keeps the interrupt flag", body = function( currentSpec ) {
				var base = createTree( 30 );
				try {
					thread name="ldev1956a" src=base & "src" trg=base & "trg" {
						var state = { count: 0 };
						var filter = function( path ) {
							// the 5th file interrupts the copying thread, like a request timeout or a thread termination does
							if ( listLast( path, "/\" ) == "f.txt" && ++state.count == 5 ) getPageContext().getThread().interrupt();
							return true;
						};
						thread.message = "";
						try {
							directoryCopy( attributes.src, attributes.trg, true, filter );
						}
						catch ( any e ) {
							thread.message = e.message;
						}
						thread.stillInterrupted = getPageContext().getThread().isInterrupted();
						// do not leave the flag on the thread
						createObject( "java", "java.lang.Thread" ).interrupted();
					}
					thread action="join" name="ldev1956a" timeout=20000;

					var res = cfthread.ldev1956a;
					expect( res.message ).toInclude( "interrupted" );
					expect( res.stillInterrupted ).toBeTrue();

					// stopped early, what was copied before stays in place
					var copied = countFiles( base & "trg" );
					expect( copied ).toBeGT( 0 );
					expect( copied ).toBeLT( 30 );
				}
				finally {
					cleanup( base );
				}
			} );

			it( title = "directoryCopy stops when the thread gets terminated", body = function( currentSpec ) {
				var base = createTree( 100 );
				try {
					thread name="ldev1956b" src=base & "src" trg=base & "trg" {
						var filter = function( path ) {
							// CFML only busy wait of about 30ms per file, no sleep (a sleep would throw on interrupt itself)
							var end = getTickCount() + 30;
							while ( getTickCount() < end ) {}
							return true;
						};
						directoryCopy( attributes.src, attributes.trg, true, filter );
					}
					sleep( 300 );
					thread action="terminate" name="ldev1956b";
					thread action="join" name="ldev1956b" timeout=20000;

					expect( cfthread.ldev1956b.status ).toBe( "TERMINATED" );
					var copied = countFiles( base & "trg" );
					expect( copied ).toBeLT( 100 );
				}
				finally {
					cleanup( base );
				}
			} );

			it( title = "the stream copy stops when the thread gets interrupted and copies everything otherwise", body = function( currentSpec ) {
				var IOUtil = createObject( "java", "lucee.commons.io.IOUtil" );
				var data = repeatString( "0123456789abcdef", 65536 ); // 1 MB in memory

				var out = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
				IOUtil.copy( createObject( "java", "java.io.ByteArrayInputStream" ).init( data.getBytes( "UTF-8" ) ), out, true, true );
				expect( out.size() ).toBe( len( data ) );

				thread name="ldev1956c" data=data {
					thread.exception = "";
					thread.size = -1;
					var out = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
					getPageContext().getThread().interrupt();
					try {
						createObject( "java", "lucee.commons.io.IOUtil" ).copy( createObject( "java", "java.io.ByteArrayInputStream" ).init( attributes.data.getBytes( "UTF-8" ) ), out, true, true );
					}
					catch ( any e ) {
						thread.exception = e.type & ":" & e.message;
					}
					thread.size = out.size();
					createObject( "java", "java.lang.Thread" ).interrupted();
				}
				thread action="join" name="ldev1956c" timeout=20000;

				var res = cfthread.ldev1956c;
				expect( res.exception ).toInclude( "interrupted" );
				// stopped after the first chunk
				expect( res.size ).toBeLT( len( data ) );
			} );

		} );
	}

	private string function createTree( required numeric count ) {
		var base = getTempDirectory() & "ldev1956-" & createUniqueId() & "/";
		for ( var i = 1; i <= arguments.count; i++ ) {
			var dir = base & "src/d" & numberFormat( i, "000" );
			directoryCreate( dir, true );
			fileWrite( dir & "/f.txt", "x" );
		}
		return base;
	}

	private numeric function countFiles( required string dir ) {
		if ( !directoryExists( arguments.dir ) ) return 0;
		return directoryList( arguments.dir, true, "path", "*.txt" ).len();
	}

	private void function cleanup( required string base ) {
		if ( directoryExists( arguments.base ) ) directoryDelete( arguments.base, true );
	}

}

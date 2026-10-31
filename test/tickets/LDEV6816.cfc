component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6816/";
		variables.uri = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/LDEV6816/snippet.cfm";
		variables.file = variables.dir & "snippet.cfm";
		if ( !directoryExists( variables.dir ) ) directoryCreate( variables.dir );
		fileWrite( variables.file, "AAAA#chr( 10 )#BBBB#chr( 10 )#CCCC#chr( 10 )#" );
	}

	function afterAll(){
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ){
		describe( "LDEV-6816 page parts snippet cache key", function(){

			it( "returns the cached snippet when only the last access time of the page source changed", function(){
				var map = createObject( "java", "lucee.commons.io.res.util.ResourceSnippetsMap" ).init( 10, 10 );
				var ps = getPageContext().getPageSource( variables.uri );
				var sys = createObject( "java", "java.lang.System" );

				ps.setLastAccessTime( 1000 );
				var first = map.getSnippet( ps, 5, 9, "UTF-8" );
				expect( first.getContent() ).toBe( "BBBB" );

				// every request updates the last access time of the page source, this must not invalidate the cache
				ps.setLastAccessTime( 2000 );
				var second = map.getSnippet( ps, 5, 9, "UTF-8" );

				expect( sys.identityHashCode( second ) ).toBe( sys.identityHashCode( first ) );
			});

			it( "does not return a cached snippet after the source file has changed", function(){
				var map = createObject( "java", "lucee.commons.io.res.util.ResourceSnippetsMap" ).init( 10, 10 );
				var ps = getPageContext().getPageSource( variables.uri );

				expect( map.getSnippet( ps, 5, 9, "UTF-8" ).getContent() ).toBe( "BBBB" );

				fileWrite( variables.file, "XXXX#chr( 10 )#YYYY#chr( 10 )#ZZZZ#chr( 10 )#" );
				fileSetLastModified( variables.file, dateAdd( "s", 10, now() ) );

				expect( map.getSnippet( ps, 5, 9, "UTF-8" ).getContent() ).toBe( "YYYY" );
			});

		});
	}

}

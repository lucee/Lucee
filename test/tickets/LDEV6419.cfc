component extends="org.lucee.cfml.test.LuceeTestCase" labels="application" {

	function beforeAll() {
		variables.cfg = getPageContext().getConfig();
		// the physical directory that holds the Application.cfc, no trailing slash (as PageSource.getPhyscalFile().getParent())
		// the cache key is the resolved physical directory, so the fixture path has to use the
		// platform separator too (it is built with a forward slash above)
		variables.appDir = replace( reReplace( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6419/app", "[/\\]+$", "" ), "/", server.separator.file, "all" );
		variables.appListenerFiles = [ "Application.[cfc|cfm]", "Application.cfc", "Application.cfm" ];
		// ApplicationListener.MODE_CURRENT2ROOT, MODE_CURRENT, MODE_ROOT, MODE_CURRENT_OR_ROOT
		variables.appListenerModes = [ 0, 1, 2, 4 ];
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6419 Application.cfc page source cache key must be case insensitive", function() {

			it( title="a cached Application page source is found again when the directory comes back in a different case", body=function( currentSpec ) {
				var pc = getPageContext();
				var dir = variables.appDir & "/Mixed/CaSe/#createUniqueID()#"; // does not need to exist, only the cache key matters
				var ps = pc.getCurrentPageSource();

				variables.cfg.putApplicationPageSource( dir, ps, "Application.cfc", 1, true );

				expect( isNull( variables.cfg.getApplicationPageSource( pc, dir, "Application.cfc", 1, nullValue() ) ) ).toBeFalse( "same case [#dir#]" );
				expect( isNull( variables.cfg.getApplicationPageSource( pc, lCase( dir ), "Application.cfc", 1, nullValue() ) ) ).toBeFalse( "lower case [#lCase( dir )#]" );
				expect( isNull( variables.cfg.getApplicationPageSource( pc, uCase( dir ), "Application.cfc", 1, nullValue() ) ) ).toBeFalse( "upper case [#uCase( dir )#]" );
				expect( isNull( variables.cfg.getApplicationPageSource( pc, dir, "APPLICATION.CFC", 1, nullValue() ) ) ).toBeFalse( "upper case filename" );
			});

			it( title="a request puts the Application.cfc in the cache and a lookup with a differently cased path hits that entry", body=function( currentSpec ) {
				var pc = getPageContext();
				variables.cfg.flushApplicationPathCache();

				var result = _InternalRequest( template: createURI( "LDEV6419/app/index.cfm" ) );
				expect( trim( result.fileContent ) ).toBe( "LDEV-6419" );

				// find out under which filename and mode the request cached its Application.cfc
				var hit = findCacheEntry( pc, variables.appDir );
				expect( hit.found ).toBeTrue( "the request should have cached its Application.cfc for [#variables.appDir#]" );

				// the very same entry must be found when the path arrives with another case
				var other = variables.cfg.getApplicationPageSource( pc, uCase( variables.appDir ), hit.filename, hit.mode, nullValue() );
				expect( isNull( other ) ).toBeFalse( "cache entry for [#hit.filename#, mode #hit.mode#] not found for upper case path [#uCase( variables.appDir )#]" );
				other = variables.cfg.getApplicationPageSource( pc, lCase( variables.appDir ), hit.filename, hit.mode, nullValue() );
				expect( isNull( other ) ).toBeFalse( "cache entry for [#hit.filename#, mode #hit.mode#] not found for lower case path [#lCase( variables.appDir )#]" );
			});

			it( title="requesting the same application via a differently cased URL does not need a new cache entry", body=function( currentSpec ) {
				var pc = getPageContext();
				variables.cfg.flushApplicationPathCache();

				var r1 = _InternalRequest( template: createURI( "LDEV6419/app/index.cfm" ) );
				var first = findCacheEntry( pc, variables.appDir );
				expect( first.found ).toBeTrue( "the first request should have cached its Application.cfc" );

				// same file, but the path in the URL has a different case
				var r2 = _InternalRequest( template: createURI( "LDEV6419/APP/Index.cfm" ) );
				expect( trim( r2.fileContent ) ).toBe( "LDEV-6419" );

				// still found, whatever case the lookup uses
				expect( isNull( variables.cfg.getApplicationPageSource( pc, variables.appDir, first.filename, first.mode, nullValue() ) ) ).toBeFalse( "same case after second request" );
				expect( isNull( variables.cfg.getApplicationPageSource( pc, uCase( variables.appDir ), first.filename, first.mode, nullValue() ) ) ).toBeFalse( "upper case after second request" );
			});

		});
	}

	private struct function findCacheEntry( required any pc, required string dir ) {
		for ( var filename in variables.appListenerFiles ) {
			for ( var mode in variables.appListenerModes ) {
				if ( !isNull( variables.cfg.getApplicationPageSource( arguments.pc, arguments.dir, filename, mode, nullValue() ) ) ) {
					return { found: true, filename: filename, mode: mode };
				}
			}
		}
		return { found: false, filename: "", mode: -1 };
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}

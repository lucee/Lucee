component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function afterAll() {
		application action="update" regex=variables.origEngine;
	}

	function run( testResults, testBox ) {
		// needed while the specs are defined, the skip attributes below depend on it
		variables.origEngine = getApplicationSettings().regex;
		variables.perlAvailable = isEngineAvailable( "perl" );

		describe( "LDEV-6632 regex engines: java and compat are built in, perl is provided by the Perl Regex extension", function() {

			it( title="java is the default engine", body=function( currentSpec ) {
				expect( variables.origEngine.type ).toBe( "java" );
			} );

			it( title="java and compat are built in", body=function( currentSpec ) {
				application action="update" regex={ type: "java" };
				expect( reFind( "b+", "abbbc" ) ).toBe( 2 );
				application action="update" regex={ type: "compat" };
				expect( reFind( "b+", "abbbc" ) ).toBe( 2 );
				expect( reReplace( "abc", "(b)", "[\1]" ) ).toBe( "a[b]c" );
			} );

			it( title="an unknown engine name is rejected", body=function( currentSpec ) {
				expect( function() {
					application action="update" regex={ type: "doesnotexist" };
				} ).toThrow( message="invalid regex name" );
			} );

			// the engine names of the former core engine stay valid settings, they are reported by their canonical name
			it( title="perl and its aliases are accepted as a setting and reported as perl", body=function( currentSpec ) {
				for ( var name in [ "perl", "perl5", "classic", "oro", "PERL" ] ) {
					application action="update" regex={ type: name };
					var type = getApplicationSettings().regex.type;
					application action="update" regex={ type: "java" };
					expect( type ).toBe( "perl" );
				}
			} );

			it( title="without the Perl Regex extension a regex function fails with a clear message", skip=variables.perlAvailable, body=function( currentSpec ) {
				for ( var name in [ "perl", "perl5", "oro" ] ) {
					var msg = engineError( name );
					expect( msg ).notToBe( "" );
					expect( msg ).toInclude( "[perl]" );
					expect( msg ).toInclude( "not available" );
				}
			} );

			it( title="with the Perl Regex extension perl and its aliases work", skip=!variables.perlAvailable, body=function( currentSpec ) {
				for ( var name in [ "perl", "perl5", "oro" ] ) {
					application action="update" regex={ type: name };
					var found = reFind( "b+", "abbbc" );
					application action="update" regex={ type: "java" };
					expect( found ).toBe( 2 );
				}
			} );

			it( title="useJavaAsRegexEngine=false selects perl: it fails with a clear message without the extension", skip=variables.perlAvailable, body=function( currentSpec ) {
				var result = _InternalRequest( template: fixtureURL( "useJavaAsRegexEngine_false/index.cfm" ) );
				expect( result.filecontent.trim() ).toInclude( "ERR:" );
				expect( result.filecontent.trim() ).toInclude( "not available" );
			} );

			it( title="useJavaAsRegexEngine=false selects perl: it works with the extension", skip=!variables.perlAvailable, body=function( currentSpec ) {
				var result = _InternalRequest( template: fixtureURL( "useJavaAsRegexEngine_false/index.cfm" ) );
				expect( result.filecontent.trim() ).toBe( "OK:2" );
			} );

		} );

	}

	// the message of the error when a regex function is used with the given engine, empty if it works
	private string function engineError( required string name ) {
		application action="update" regex={ type: arguments.name };
		var msg = "";
		try {
			reFind( "b+", "abbbc" );
		}
		catch ( any e ) {
			msg = e.message;
		}
		application action="update" regex={ type: "java" };
		return msg;
	}

	private boolean function isEngineAvailable( required string name ) {
		return !len( engineError( arguments.name ) );
	}

	private string function fixtureURL( required string sub ) {
		if ( cgi.request_url eq "http://localhost/index.cfm" ) {
			return "/test/tickets/LDEV6632/" & sub;
		}
		return getDirectoryFromPath( cgi.script_name ) & "LDEV6632/" & sub;
	}

}

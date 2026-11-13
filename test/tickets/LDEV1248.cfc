component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {

	function run( testResults, testBox ) {
		describe( "LDEV-1248 getCanonicalPath() expand argument", function() {

			it( "expand=true resolves a relative path like expandPath()", function() {
				var expected = getCanonicalPath( expandPath( "../tickets/LDEV1248.cfc" ) );
				expect( getCanonicalPath( "../tickets/LDEV1248.cfc", true ) ).toBe( expected );
				expect( getCanonicalPath( "../tickets/LDEV1248.cfc", true ) ).toInclude( "tickets" );
				expect( getCanonicalPath( "../tickets/LDEV1248.cfc", true ) ).notToInclude( ".." );
			});

			it( "expand=true resolves a mapping", function() {
				var expected = getCanonicalPath( expandPath( "/test/tickets/LDEV1248.cfc" ) );
				expect( getCanonicalPath( "/test/tickets/LDEV1248.cfc", true ) ).toBe( expected );
				expect( fileExists( getCanonicalPath( "/test/tickets/LDEV1248.cfc", true ) ) ).toBeTrue();
			});

			it( "expand=false does not expand and is the same as the one argument call", function() {
				var abs = getCurrentTemplatePath();
				expect( getCanonicalPath( abs, false ) ).toBe( getCanonicalPath( abs ) );
				expect( getCanonicalPath( "/test/tickets/LDEV1248.cfc", false ) ).toBe( getCanonicalPath( "/test/tickets/LDEV1248.cfc" ) );
			});

			it( "expand=true leaves an absolute path unchanged", function() {
				var abs = getCurrentTemplatePath();
				expect( getCanonicalPath( abs, true ) ).toBe( getCanonicalPath( abs ) );
			});

			it( "accepts the named argument expand", function() {
				expect( getCanonicalPath( path="../tickets/LDEV1248.cfc", expand=true ) )
					.toBe( getCanonicalPath( expandPath( "../tickets/LDEV1248.cfc" ) ) );
			});

		});
	}

}

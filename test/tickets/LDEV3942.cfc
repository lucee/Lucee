component extends="org.lucee.cfml.test.LuceeTestCase" labels="cflogin" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3942, cflogin attributes", function() {

			it( title="cflogin applicationToken is marked as implemented in the tag definition", body=function( currentSpec ) {
				var attrs = getTagData( "cf", "login" ).attributes;
				expect( attrs ).toHaveKey( "applicationtoken" );
				expect( attrs.applicationtoken.status ).toBe( "implemented" );
			});

			it( title="cflogin without applicationToken stores the login under the application name", body=function( currentSpec ) {
				var result = deserializeJSON( _InternalRequest( template: createURI( "index.cfm" ) ).fileContent );
				expect( result.loggedIn ).toBeTrue();
				expect( result.isUser ).toBeTrue();
				expect( result.isAdmin ).toBeTrue();
				expect( result.authKeys ).toBe( [ "CFAUTHORIZATION_LDEV-3942-CFLOGIN" ] );
			});

			it( title="cflogin applicationToken is used for the name of the login storage", body=function( currentSpec ) {
				var result = deserializeJSON( _InternalRequest(
					template: createURI( "index.cfm" ),
					url: { applicationToken: "ldev3942token" }
				).fileContent );
				expect( result.loggedIn ).toBeTrue();
				expect( result.authKeys ).toBe( [ "CFAUTHORIZATION_LDEV3942TOKEN" ] );
			});

			it( title="cflogin accepts idleTimeout, applicationToken and cookieDomain together", body=function( currentSpec ) {
				var result = deserializeJSON( _InternalRequest(
					template: createURI( "index.cfm" ),
					url: { applicationToken: "ldev3942token", idleTimeout: 3600, cookieDomain: "example.com" }
				).fileContent );
				expect( result.loggedIn ).toBeTrue();
				expect( result.isUser ).toBeTrue();
				expect( result.authKeys ).toBe( [ "CFAUTHORIZATION_LDEV3942TOKEN" ] );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/LDEV3942/";
		return baseURI & calledName;
	}

}

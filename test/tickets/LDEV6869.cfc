component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6869" {

	function run( testResults, testBox ) {
		describe( "LDEV-6869: switching the session type from JEE to Application should not throw a ClassCastException", function() {

			it( title="a request with the cookies of a JEE session gets a usable CFML session", body=function( currentSpec ) {
				var uri = createURI( "LDEV6869" );

				var jee = _InternalRequest(
					template: "#uri#/jee/index.cfm"
				);
				var jeeResult = deserializeJSON( jee.fileContent );
				expect( jeeResult.sessionType ).toBe( "jee" );
				expect( jeeResult.value ).toBe( "jee" );

				var first = _InternalRequest(
					template: "#uri#/application/index.cfm",
					cookies: jee.cookies
				);
				var firstResult = deserializeJSON( first.fileContent );
				expect( firstResult.sessionType ).toBe( "cfml" );
				expect( firstResult.value ).toBe( "application" );
				expect( firstResult.previous ).toBe( "", "the data of the JEE session should not be carried over" );

				// the response above sets no new cookies, as the request already sent the cfid
				var second = _InternalRequest(
					template: "#uri#/application/index.cfm",
					cookies: jee.cookies
				);
				var secondResult = deserializeJSON( second.fileContent );
				expect( secondResult.sessionType ).toBe( "cfml" );
				expect( secondResult.previous ).toBe( "application", "the CFML session should keep its data between requests" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}

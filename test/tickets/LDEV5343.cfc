component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function beforeAll() {
		variables.uri = createURI( "LDEV5343" );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5343 null coalescing operator (??)", body=function() {

			it( title="returns the right side when the variable is undefined", body=function( currentSpec ) {
				expect( ldev5343_undefined ?? "lucee" ).toBe( "lucee" );
			});

			it( title="returns the left side when it is defined", body=function( currentSpec ) {
				var value = "CFML";
				expect( value ?? "lucee" ).toBe( "CFML" );
			});

			it( title="returns the right side when the left side is null", body=function( currentSpec ) {
				var value = nullValue();
				expect( value ?? "lucee" ).toBe( "lucee" );
			});

			it( title="works with a missing struct key and without spaces", body=function( currentSpec ) {
				var sct = { a: 1 };
				expect( sct.a??2 ).toBe( 1 );
				expect( sct.b??2 ).toBe( 2 );
			});

			it( title="can be chained", body=function( currentSpec ) {
				var c = "c";
				expect( ldev5343_a ?? ldev5343_b ?? c ).toBe( "c" );
			});

			it( title="works inside a ternary", body=function( currentSpec ) {
				expect( true ? ( ldev5343_undefined ?? 1 ) : 2 ).toBe( 1 );
			});

			it( title="works in evaluate()", body=function( currentSpec ) {
				expect( evaluate( 'ldev5343_undefined ?? "lucee"' ) ).toBe( "lucee" );
			});

			it( title="works with full null support", body=function( currentSpec ) {
				var result = _InternalRequest(
					template: "#variables.uri#/LDEV5343.cfm",
					forms: { scene: "null" }
				);
				expect( trim( result.filecontent ) ).toBe( "lucee" );

				result = _InternalRequest(
					template: "#variables.uri#/LDEV5343.cfm",
					forms: { scene: "undefined" }
				);
				expect( trim( result.filecontent ) ).toBe( "lucee" );

				result = _InternalRequest(
					template: "#variables.uri#/LDEV5343.cfm",
					forms: { scene: "value" }
				);
				expect( trim( result.filecontent ) ).toBe( "CFML" );
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}

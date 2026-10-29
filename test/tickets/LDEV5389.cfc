component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "Testcase for LDEV-5389 - colon not caught", function() {

			it( title="colon instead of semi colon after a function call is a syntax error", body=function( currentSpec ) {
				var uri = createURI( "LDEV5389/colon.cfm" );
				expect( function() {
					_InternalRequest( template: uri );
				} ).toThrow( regex="did you mean \[;\] instead of \[:\]" );
			});

			it( title="a function as listener still works", body=function( currentSpec ) {
				var threadName = getOne():function( result ) {};
				expect( threadName ).notToBeEmpty();
			});
		});
	}

	private function getOne() {
		return 1;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}

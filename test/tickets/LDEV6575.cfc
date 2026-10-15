component extends="org.lucee.cfml.test.LuceeTestCase" labels="component" {

	function run( testResults, testBox ) {
		describe( "LDEV-6575 implicit accessor for a sub component property inside the parent file", function() {
			it( "invokes the getter of the sub component", function() {
				var uri = createURI( "LDEV6575" );
				var result = _InternalRequest( template: "#uri#/index.cfm" );
				expect( trim( result.filecontent ) ).toBe( "getter:lucee" );
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}

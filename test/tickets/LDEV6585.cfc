component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	function run( testResults, testBox ) {
		describe( "LDEV-6585 SerializeAISession() systemMessage for engines not extending AISessionSupport", function() {

			it( title="serializes and restores the system message of a session implementing AISession directly", body=function( currentSpec ) {
				var result = deserializeJSON( _InternalRequest( template: createURI( "LDEV6585/test.cfm" ) ).fileContent.trim() );
				expect( result.serialized ).toBe( "be terse" );
				expect( result.loaded ).toBe( "be terse" );
				expect( result.answer ).toBe( "[be terse] again" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}

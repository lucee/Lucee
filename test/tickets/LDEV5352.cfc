component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-5352", body=function() {
			it(title = "closure in cfparam default attribute compiles", body = function( currentSpec ) {
				// we neeed to call the code failing via internal request, because testbox does not like compiler erros
				local.result = _InternalRequest(
					template : "#createURI("LDEV5352")#/index.cfm"
				);
				expect( trim( local.result.filecontent ) ).toBe( "ok" );
			});
			it(title = "closure in ternary operator compiles", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#createURI("LDEV5352")#/ternary.cfm"
				);
				expect( trim( local.result.filecontent ) ).toBe( "ok" );
			});
		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}

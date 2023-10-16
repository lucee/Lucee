component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV4531");
	}

	public function run( testResults, textbox ) {
		describe( title="Testcase for LDEV-4531", body=function() {
			it( title="Checking literal struct with final static", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV4531.cfm",
					forms:{ Scene=1 }
				);
				expect(result.filecontent).toBe("{SUBKEY={Lucee}}");
			});

			it( title="Checking dot-operator struct with final static", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV4531.cfm",
					forms:{ Scene=2 } 
				);
				expect( function() {
					result.filecontent
				}).toThrow(); // acf throws the error
			})
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI&""&calledName;
	}
}

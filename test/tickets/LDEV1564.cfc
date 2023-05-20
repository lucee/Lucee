component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {
	function beforeAll(){
		variables.uri = createURI("LDEV1564");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1564", function() {
			it( title='Checking Transaction with ormEnable=true ', skip=notHasMsSQL(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("lucee");
			});

		});
	}

	private function notHasMsSQL(){
		return isEmpty( server.getDatasource( "mssql" ) );
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
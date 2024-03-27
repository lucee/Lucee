component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" skip="true" {

	function beforeAll() {
		variables.uri = createURI("LDEV3022");
	}
	
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3022", function() {
			it( title = "Checked with 'float' sql type ", skip=notHasMsSQL(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : { scene = 'float' }
				);
				expect( result.filecontent ).toBe( "1,11.97" ); // fails returns [0,]
			});
			it( title = "Checked with 'decimal' sql type ", skip=notHasMsSQL(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : { scene = 'decimal' }
				);
				expect( result.filecontent ).toBe( "1,11.97" );
			});
		});
	}

	private function notHasMsSQL(){
		return isEmpty( server.getDatasource( "mssql" ) );
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
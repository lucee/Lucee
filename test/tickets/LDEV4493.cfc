component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" {
	function beforeAll(){
		variables.uri = createURI("LDEV4493");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1537", function() {
			it(title = "Checking cfmail support priority='lowest'", skip=notHasServices(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/priority.cfm",
					url: {
						priority: "lowest"
					}
				);
				expect( checkForHeader("X-Priority" ) ).toBe( 5 );
			});

			it(title = "Checking cfmail support priority='high'", skip=notHasServices(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/priority.cfm",
					url: {
						priority: "high"
					}
				);
				expect( checkForHeader("X-Priority" ) ).toBe( 2 );
			});

			it(title = "Checking cfmail support priority=''", skip=notHasServices(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/priority.cfm",
					url: {
						priority: ""
					}
				);
				expect( checkForHeader( "X-Priority" ) ).toBe( "" );
			});

		});
	}

	private function checkForHeader( required string header ){

		var pop = server.getTestService("pop");

		pop action="getAll" name="local.inboxemails"
			server="#pop.server#"
			password="#pop.password#"
			port="#pop.PORT_INSECURE#"
			secure="no"
			username="luceeldev4493pop@localhost";

		var multipartMessage = queryGetRow( inboxemails, queryRecordCount( inboxemails ) ); // assumes last inbox mail must sended by above the process

		if ( structKeyExists( multipartMessage , "header" ) )
			return extractHeader ( multipartMessage.header, arguments.header );
		else
			return "";
	}

	private function extractHeader( required string headers, required string header ){
		var hdrs = ListToArray( arguments.headers, chr(10) );
		for (var h in hdrs ) {
			if ( trim( listFirst( h, ":" ) ) eq arguments.header )
				return trim( listRest( h, ":" ) );
		}
		return "";
	}

	private function notHasServices() {
		return structCount(server.getTestService("smtp")) == 0 || structCount(server.getTestService("pop")) == 0;
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

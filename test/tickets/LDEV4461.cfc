component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll() {
		variables.uri = createURI("LDEV4461");
	}

	function afterAll() {
		if (!notHasMssql()) {
			queryExecute( sql="DROP TABLE IF EXISTS test4461", options: {
				datasource: server.getDatasource("mssql")
			});
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4461",  function() {

			it( title="checking positional arguments on ORM EntityLoadByPk", skip="#notHasMssql()#",  body=function( currentSpec ) {
				local.result = _InternalRequest(
						template = "#variables.uri#\LDEV4461.cfm",
						forms = {scene:1}
				).filecontent.trim();
				expect(result).tobe("1");
			});

			it( title="checking named arguments on ORM EntityLoadByPk", skip="#notHasMssql()#",  body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#variables.uri#/LDEV4461.cfm",
					forms = {scene:2}
				).fileContent.trim();
				expect(result).tobe("1");
			});

			it( title="checking positional arguments on ORM EntityLoadByPk with unique", skip="#notHasMssql()#",  body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#variables.uri#/LDEV4461.cfm",
					forms = {scene:"unique"}
				).fileContent.trim();
				expect(result).tobe("1");
			});

			it( title="checking named arguments on ORM EntityLoadByPk with unique", skip="#notHasMssql()#",  body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#variables.uri#/LDEV4461.cfm",
					forms = {scene:"unique_named"}
				).fileContent.trim();
				expect(result).tobe("1");
			});

		});
	}

	private function notHasMssql() {
		if ( structCount( server.getTestService("orm") ) eq 0 ) return true;
		return structCount(server.getDatasource("mssql")) == 0;
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}

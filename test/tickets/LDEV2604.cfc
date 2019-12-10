component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2604");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2604", function() {
			it(title = "MSSQL Check reverting TRANSACTION ISOLATION LEVEL back in to READ COMMITED after changing it in query", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2604.cfm",
					forms:	{scene=1}
				);
				expect(result.filecontent.trim()).toBe("ReadCommitted");
			});

			it(title = "MSSQL Check reverting TRANSACTION ISOLATION LEVEL back in to READ COMMITED after changing it in transaction", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2604.cfm",
					forms:	{scene=2}
				);
				expect(result.filecontent.trim()).toBe("ReadCommitted");
			});

			it(title = "MySQL Check TRANSACTION ISOLATION LEVEL after changing it for QUERY", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2604.cfm",
					forms:	{scene=3}
				);
				expect(result.filecontent.trim()).toBe(true);
			});

			it(title = "MySQL Check TRANSACTION ISOLATION LEVEL after changing it for SESSION", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2604.cfm",
					forms:	{scene=4}
				);
				expect(result.filecontent.trim()).toBe(true);
			});

			it(title = "MySQL Check TRANSACTION ISOLATION LEVEL after changing it for GLOBAL", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2604.cfm",
					forms:	{scene=5}
				);
				expect(result.filecontent.trim()).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1992", function() {
			it( title='Checking ', body=function( currentSpec ) {
				var uri = createURI("LDEV1937");
				local.result = _InternalRequest(
					template:"#uri#/index.cfm");
				expect(result.filecontent.trim()).toBe("test From mySqlDatasource||test From msSqlDatasource");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	function afterAll(){
		variables.adminWeb = new org.lucee.cfml.Administrator("web", server.WebAdminPassword);
		var datasource1 = adminWeb.getDatasource('testmysql');
		var datasource2 = adminWeb.getDatasource('testMssql');
		if (!StructIsEmpty(datasource1)){
			adminWeb.removeDatasource('testmysql');
		}
		if (!StructIsEmpty(datasource2)){
			adminWeb.removeDatasource('testMssql');
		}
	}
} 
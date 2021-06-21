component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function beforeAll() {
		variables.uri = createURI("LDEV3030");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-3030", function() {

			it(title = "Checking metadata with cf_sql_xml datatype in MSSQL",skip=isMSSQLNotSupported(), body = function( currentSpec ) {
				result = _InternalRequest(
					template : "#uri#/LDEV3030.cfm",
					forms :	{scene = 1, datasource="LDEV3030_MSSQL"}
				);
				expect(trim(result.filecontent)).toBe('<metadata><param/></metadata>');
			});

			it(title = "Checking null value with cf_sql_xml in MSSQL",skip=isMSSQLNotSupported(), body = function( currentSpec ) {
				result = _InternalRequest(
					template : "#uri#/LDEV3030.cfm",
					forms :	{scene = 2, datasource = "LDEV3030_MSSQL"}
				);
				expect(trim(result.filecontent)).toBe(1);
			});

			it(title = "Checking metadata with cf_sql_xml datatype in jTDS MSSQL",skip=isMSSQLNotSupported(), body = function( currentSpec ) {
				result = _InternalRequest(
					template : "#uri#/LDEV3030.cfm",
					forms :	{scene = 3, datasource = "LDEV3030_jTDS"}
				);
				expect(trim(result.filecontent)).toBe('<metadata><param/></metadata>');
			});

			it(title = "Checking null value with cf_sql_xml in jTDS MSSQL",skip=isMSSQLNotSupported(), body = function( currentSpec ) {
				result = _InternalRequest(
					template : "#uri#/LDEV3030.cfm",
					forms :	{scene = 4, datasource = "LDEV3030_jTDS"}
				);
				expect(trim(result.filecontent)).toBe(1);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	function isMSSQLNotSupported() {
		return structIsEmpty(server.getDatasource("mssql"));
	}
}
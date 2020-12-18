component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3091");
	}

	function run ( testResults , testBox ) {
		describe("This testcase for LDEV-3091",function(){
			
			it(title="Column with data type SMALLINT signed value greater than range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='col_signed',  value=60000 }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_signed' at row 1");
			});

			it(title="Column with data type SMALLINT unsigned value between range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='col_unsigned', value=60000}
				);
				expect(trim(result.filecontent)).toBe("Success");
			});

			it(title="Column with data type SMALLINT unsigned value greater than range",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=1, columnName='col_unsigned', value=70000 }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_unsigned' at row 1");
			});	

			it(title="Column with data type SMALLINT signed value greater than range with cfsql type cf_sql_smallint",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=2, columnName='col_signed', value=60000}
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_signed' at row 1");
			});

			it(title="Column with data type SMALLINT unsigned value between range with cfsql type cf_sql_smallint",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=2, columnName='col_unsigned', value=60000 }
				);
				expect(trim(result.filecontent)).toBe("Success");
			});	

			it(title="Column with data type SMALLINT unsigned value greater than range with cfsql type cf_sql_smallint",body =function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene=2, columnName='col_unsigned', value=70000 }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_unsigned' at row 1");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
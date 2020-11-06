component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3118");
	}

	function run ( testResults , testBox ) {
		describe("This testcase for LDEV-3118",function(){
			it(title = "Value greater than integer range without cfsqltype = cf_sql_integer",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 1, columnName = 'col_Num',  value = 2200000000 , type = 'int'  }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_Num' at row 1");
			});
			it(title = "Value greater than TINYINT range without cfsqltype = cf_sql_TINYINT",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 1, columnName = 'col_Num',  value = 260 , type = 'TINYINT'  }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_Num' at row 1");
			});
			it(title = "Value greater than SMALLINT range without cfsqltype = cf_sql_SMALLINT",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 1, columnName = 'col_Num',  value = 2200000000 , type = 'SMALLINT'  }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_Num' at row 1");
			});
			it(title = "Value greater than BIGINT range without cfsqltype = cf_sql_BIGINT",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 1, columnName = 'col_Num',  value = 9300000000000000000 , type = 'BIGINT'  }
				);
				expect(trim(result.filecontent)).toBe("Data truncation: Out of range value for column 'col_Num' at row 1");
			});
			

			it(title = "Value greater than integer range with cfsqltype = cf_sql_integer",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 2, columnName = 'col_Num',  value = 2200000000 , type = 'int' , paramType = 'cf_sql_integer'}
				);
				expect(trim(result.filecontent)).toBe("Invalid data 2200000000 for CFSQLTYPE CF_SQL_INTEGER.");
			});
			it(title = "Value greater than TINYINT range with cfsqltype = cf_sql_TINYINT",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 2, columnName = 'col_Num',  value = 260 , type = 'TINYINT' , paramType = 'cf_sql_tinyint'}
				);
				expect(trim(result.filecontent)).toBe("Invalid data 260 for CFSQLTYPE CF_SQL_TINYINT.");
			});
			it(title = "Value greater than SMALLINT range with cfsqltype = cf_sql_SMALLINT",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 2, columnName = 'col_Num',  value = 2200000000 , type = 'SMALLINT', paramType = 'cf_sql_smallint'}
				);
				expect(trim(result.filecontent)).toBe("Invalid data 2200000000 for CFSQLTYPE CF_SQL_INTEGER.");
			});
			it(title = "Value greater than BIGINT range with cfsqltype = cf_sql_BIGINT",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 2, columnName = 'col_Num',  value = 9300000000000000000 , type = 'BIGINT' , paramType = 'cf_sql_bigint' }
				);
				expect(trim(result.filecontent)).toBe("Invalid data 9300000000000000000 for CFSQLTYPE CF_SQL_INTEGER.");
			});
			it(title = "Big value with column type DECIMAL",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 1, columnName = 'col_Num',  value = 9999999999999999 , type = 'DECIMAL(30,5)'  }
				);
				expect(trim(result.filecontent)).toBe("9999999999999999");
			});

		});	
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		// runs before all testcases
		allCountries = queryNew("Country,ShortCode");
		CountryList = "India,Switzerland";
		ShortCodeList = "IN,SWL";
		for(var idx=1;idx<="#listLen(CountryList)#";idx++){
			queryAddRow(allCountries);
			querySetCell(allCountries, "Country", listGetAt(CountryList, idx));
			querySetCell(allCountries, "ShortCode", listGetAt(ShortCodeList, idx));
		}
	}

	function afterAll(){
		// runs after all testcases
	}

	function run( testResults , testBox ){
		describe( title="Test suite for checking queryExecute's result in various cases(script based/queryExecute)", body=function(){
			beforeEach(function( currentSpec ){
				// runs before each spec in this suite group
			});

			afterEach(function( currentSpec ){
				// Runs after each spec in this suite group
				// Just releasing created variables from coldfusion memory.
				variables.result = JavaCast( "null", 0 );
				local.result = JavaCast( "null", 0 );
				variables.foo.result = JavaCast( "null", 0 );
				local.foo.result = JavaCast( "null", 0 );
				variables.foo = JavaCast( "null", 0 );
				local.foo = JavaCast( "null", 0 );
			});

			it(title="Case 1: result in variables scope", body=function(){
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result="result"});

				// Expectations for this case
				expect(isNull(variables.result)).toBeFalse();
				expect(isNull(local.result)).toBeTrue();
				expect(structKeyExists(result, "RecordCount") && result.RecordCount EQ 2).toBeTrue();
			}, labels="result in variables scope");

			it(title="Case 2: result in local scope", body=function(){
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result="local.result"});

				// Expectations for this case
				expect(isNull(variables.result)).toBeTrue();
				expect(isNull(local.result)).toBeFalse();
				expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue();
			}, labels="result in local scope");

			it(title="Case 3: result in local scope(var)", body=function(){
				var result = {};
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result="result"});

				// Expectations for this case
				expect(isNull(variables.result)).toBeTrue();
				expect(isNull(local.result)).toBeFalse();
				expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue();
			}, labels="result in local scope(var)");

			it(title="Case 4: result in local scope(var & without quotes and hashes)", body=function(){
				var result = {};
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result=local.result});

				// Expectations for this case
				expect(isNull(variables.result)).toBeTrue();
				expect(isNull(local.result)).toBeFalse();
				expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue();
			}, labels="result in local scope(var & without quotes and hashes)");

			it(title="Case 5: result in variables scope( without quotes and hashes)", body=function(){
				result = {};
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result=result});

				// Expectations for this case
				expect(isNull(variables.result)).toBeFalse();
				expect(isNull(local.result)).toBeTrue();
				expect(NOT isNull(variables.result) && structKeyExists(variables.result, "RecordCount") && variables.result.RecordCount EQ 2).toBeTrue();
			}, labels="result in local scope(var & without quotes and hashes)");

			it(title="Case 6: result not available in any scope(without quotes and hashes)", body=function(){
				var isSuccess = true;
				try {
					var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result=result});
				} catch (any e) {
					isSuccess = false; // error! variable [RESULT] doesn't exist
				}
				// Expectations for this case
				expect(isSuccess).toBeTrue();
			}, labels="result not available in any scope(without quotes and hashes)");

			it(title="Case 7: result in local scoped struct", body=function(){
				var foo = {};
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result="foo.result"});

				// Expectations for this case
				expect(isNull(variables.foo.result)).toBeTrue();
				expect(isNull(local.foo.result)).toBeFalse();
				expect(NOT isNull(local.foo.result) && structKeyExists(local.foo.result, "RecordCount") && local.foo.result.RecordCount EQ 2).toBeTrue();
			}, labels="result in local scoped struct");

			it(title="Case 8: result in variables scoped struct", body=function(){
				foo = {};
				var qData = queryExecute("SELECT * FROM allCountries", {}, {dbtype="query", result="foo.result"});

				// Expectations for this case
				expect(isNull(variables.foo.result)).toBeFalse();
				expect(isNull(local.foo.result)).toBeTrue();
				expect(NOT isNull(variables.foo.result) && structKeyExists(variables.foo.result, "RecordCount") && variables.foo.result.RecordCount EQ 2).toBeTrue();
			}, labels="result in variables scoped struct");

		}, labels="Test suite for checking queryExecute's result in various cases(script based)");

		describe( title="Test suite for checking queryExecute's result in various cases(tag based/cfquery)", body=function(){
			beforeEach(function( currentSpec ){
				// runs before each spec in this suite group
				if(currentSpec EQ "Case 9: result in variables scope(tag equivalent to case 1)"){
					include "LDEV-218.cfm";
				}
			});

			afterEach(function( currentSpec ){
				// Runs after each spec in this suite group
				// Just releasing created variables from coldfusion memory.
				variables.result = JavaCast( "null", 0 );
				local.result = JavaCast( "null", 0 );
			});

			it(title="Case 9: result in variables scope(tag equivalent to case 1)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case9();
			}, labels="result in variables scope(tag equivalent to case 1)");

			it(title="Case 10: result in local scope(tag equivalent to case 2)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case10();
			}, labels="result in local scope(tag equivalent to case 2)");

			it(title="Case 11: result in local scope(var)(tag equivalent to case 3)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case11();
			}, labels="result in local scope(var)(tag equivalent to case 3)");

			it(title="Case 12: result in local scope(var & without quotes and hashes)(tag equivalent to case 4)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case12();
			}, labels="result in local scope(var & without quotes and hashes)(tag equivalent to case 4)");

			it(title="Case 13: result in variables scope( without quotes and hashes)(tag equivalent to case 5)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case13();
			}, labels="result in variables scope( without quotes and hashes)(tag equivalent to case 5)");

			it(title="Case 14: result not available in any scope(without quotes and hashes)(tag equivalent to case 6)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case14();
			}, labels="result not available in any scope(without quotes and hashes)(tag equivalent to case 6)");

			it(title="Case 15: result in local scoped struct(tag equivalent to case 7)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case15();
			}, labels="result in local scoped struct(tag equivalent to case 7)");

			it(title="Case 16: result in variables scoped struct(tag equivalent to case 8)", body=function(){
				// Calling a function from LDEV-218.cfm
				Case16();
			}, labels="result in variables scoped struct(tag equivalent to case 8)");

		}, labels="Test suite for checking queryExecute's result in various cases(tag based)");
	}
}
<!---
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *--->
 <cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq">
	<cfscript>
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
			});

			afterEach(function( currentSpec ){
				// Runs after each spec in this suite group
				// Just releasing created variables from coldfusion memory.
				variables.result = JavaCast( "null", 0 );
				local.result = JavaCast( "null", 0 );
			});

			it(title="Case 9: result in variables scope(tag equivalent to case 1)", body=function(){
				Case9();
			}, labels="result in variables scope(tag equivalent to case 1)");

			it(title="Case 10: result in local scope(tag equivalent to case 2)", body=function(){
				Case10();
			}, labels="result in local scope(tag equivalent to case 2)");

			it(title="Case 11: result in local scope(var)(tag equivalent to case 3)", body=function(){
				Case11();
			}, labels="result in local scope(var)(tag equivalent to case 3)");

			it(title="Case 12: result in local scope(var & without quotes and hashes)(tag equivalent to case 4)", body=function(){
				Case12();
			}, labels="result in local scope(var & without quotes and hashes)(tag equivalent to case 4)");

			it(title="Case 13: result in variables scope( without quotes and hashes)(tag equivalent to case 5)", body=function(){
				Case13();
			}, labels="result in variables scope( without quotes and hashes)(tag equivalent to case 5)");

			it(title="Case 14: result not available in any scope(without quotes and hashes)(tag equivalent to case 6)", body=function(){
				Case14();
			}, labels="result not available in any scope(without quotes and hashes)(tag equivalent to case 6)");

			it(title="Case 15: result in local scoped struct(tag equivalent to case 7)", body=function(){
				Case15();
			}, labels="result in local scoped struct(tag equivalent to case 7)");

			it(title="Case 16: result in variables scoped struct(tag equivalent to case 8)", body=function(){
				Case16();
			}, labels="result in variables scoped struct(tag equivalent to case 8)");

		}, labels="Test suite for checking queryExecute's result in various cases(tag based)");
	}
	</cfscript>

	<cffunction name="Case9">
		<cfquery name="local.qData" result="result" dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.result)).toBeFalse()>
		<cfset expect(isNull(local.result)).toBeTrue()>
		<cfset expect(NOT isNull(variables.result) && structKeyExists(variables.result, "RecordCount") && variables.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>

	<cffunction name="Case10">
		<cfquery name="local.qData" result="local.result" dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.result)).toBeTrue()>
		<cfset expect(isNull(local.result)).toBeFalse()>
		<cfset expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>

	<cffunction name="Case11">
		<cfset var result = {}>
		<cfquery name="local.qData" result="result" dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.result)).toBeTrue()>
		<cfset expect(isNull(local.result)).toBeFalse()>
		<cfset expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>

	<cffunction name="Case12">
		<cfset var result = {}>
		<cfquery name="local.qData" result=local.result dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.result)).toBeTrue()>
		<cfset expect(isNull(local.result)).toBeFalse()>
		<cfset expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>

	<cffunction name="Case13">
		<cfset result = {}>
		<cfquery name="local.qData" result=result dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.result)).toBeFalse()>
		<cfset expect(isNull(local.result)).toBeTrue()>
		<cfset expect(NOT isNull(variables.result) && structKeyExists(variables.result, "RecordCount") && variables.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>

	<cffunction name="Case14">
		<cfset var isSuccess = true>
		<cftry>
			<cfquery name="local.qData" result=result dbtype="query">
				select * from allCountries
			</cfquery>
			<cfcatch type="any">
				<cfset isSuccess = false>
			</cfcatch>
		</cftry>
		<cfset expect(isSuccess).toBeTrue()>
	</cffunction>

	<cffunction name="Case15">
		<cfset var foo = {}>
		<cfquery name="local.qData" result="foo.result" dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.foo.result)).toBeTrue()>
		<cfset expect(isNull(local.foo.result)).toBeFalse()>
		<cfset expect(NOT isNull(local.foo.result) && structKeyExists(local.foo.result, "RecordCount") && local.foo.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>

	<cffunction name="Case16">
		<cfset foo = {}>
		<cfquery name="local.qData" result="foo.result" dbtype="query">
			select * from allCountries
		</cfquery>

		<cfset expect(isNull(variables.foo.result)).toBeFalse()>
		<cfset expect(isNull(local.foo.result)).toBeTrue()>
		<cfset expect(NOT isNull(variables.foo.result) && structKeyExists(variables.foo.result, "RecordCount") && variables.foo.result.RecordCount EQ 2).toBeTrue()>
	</cffunction>
</cfcomponent>

component extends = "org.lucee.cfml.test.LuceeTestCase" labels="query" {
	function beforeAll(){
		variables.uri = createURI( "LDEV3207" );
	}
	function run( testResults, textbox ) {
		describe( "LDEV-3207 cfqueryparam list=true with a list of only empty elements", function() {

			it( "tag: value='' works (single empty param)", function() {
				var q = queryNew( "id,value", "integer,varchar", [ [ 1, "" ], [ 2, "a" ] ] );
				query name="local.r" dbtype="query" {
					echo( "select id from q where value IN ( " );
					queryparam cfsqltype="cf_sql_varchar" value="" list=true;
					echo( " )" );
				}
				expect( r.recordcount ).toBe( 1 );
			});

			it( "tag: value=',,' (valueList of empty values) behaves like value=''", function() {
				var q = queryNew( "id,value", "integer,varchar", [ [ 1, "" ], [ 2, "a" ] ] );
				query name="local.r" dbtype="query" {
					echo( "select id from q where value IN ( " );
					queryparam cfsqltype="cf_sql_varchar" value=",," list=true;
					echo( " )" );
				}
				expect( r.recordcount ).toBe( 1 );
			});

			it( "tag: value=',,' with null=true does not fail", function() {
				var q = queryNew( "id,value", "integer,varchar", [ [ 1, "" ], [ 2, "a" ] ] );
				query name="local.r" dbtype="query" {
					echo( "select id from q where 1=1 or value IN ( " );
					queryparam cfsqltype="cf_sql_varchar" value=",," list=true null=true;
					echo( " )" );
				}
				expect( r.recordcount ).toBe( 2 );
			});

			it( "queryExecute: list of empty values with null=true does not fail", function() {
				var q = queryNew( "id,value", "integer,varchar", [ [ 1, "" ], [ 2, "a" ] ] );
				var r = queryExecute( "select id from q where 1=1 or value IN ( :v )", { v: { value: ",,", sqltype: "varchar", list: true, null: true } }, { dbtype: "query" } );
				expect( r.recordcount ).toBe( 2 );
			});

			it( "queryExecute: empty list without null=true still gives the clear error", function() {
				var q = queryNew( "id,value", "integer,varchar", [ [ 1, "" ] ] );
				expect( function() {
					queryExecute( "select id from q where value IN ( :v )", { v: { value: ",,", sqltype: "varchar", list: true } }, { dbtype: "query" } );
				}).toThrow( message="param [v] may not be empty" );
			});
		});

		describe(title="testcase for LDEV-3207 (MSSQL)", skip=true, body=function(){
			it(title = "query IN operator with queryparam list=true and value via valuelist", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3207.cfm",
					forms : { Scene = 1 }
                );
				expect(trim(result.fileContent)).toBe("success");
			});

			it(title = "query IN operator with queryparam list=true and empty value via valuelist", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV3207.cfm",
					forms : { Scene = 2 }
				);
				expect(trim(result.fileContent)).toBe("success");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
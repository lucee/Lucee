component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='Query Indexed not working with QueryOfQuery' , body=function(){

			it( title='queryIndex qoq', body=function() {
				var q = QueryNew("id,tag");
				QueryAddRow(q, [ 1, "lucee" ]);
				QueryAddRow(q, [ 2, "ralio" ]);
				```
				<cfquery name="local.qIndexed" indexName="id" dbtype="query">
					SELECT	id, tag
					FROM	q
				</cfquery>
				```
				var res = QueryRowDataByIndex( qIndexed, 2 );
				expect( res.tag ).toBe( "ralio" );
				expect( QueryRowByIndex( qIndexed, 1 ) ).toBe( 1 );
			});

			it( title='queryIndex qoq with queryExecute, string index and a filtered result', body=function() {
				var q = QueryNew( "cookie,val", "varchar,varchar", [ [ "a", "1" ], [ "b", "2" ], [ "c", "3" ] ] );
				var qIndexed = queryExecute( "SELECT cookie, val FROM q WHERE cookie <> 'a' ORDER BY cookie DESC", {}, { dbtype: "query", indexName: "cookie" } );
				expect( qIndexed.recordcount ).toBe( 2 );
				expect( QueryRowByIndex( qIndexed, "c" ) ).toBe( 1 );
				expect( QueryRowDataByIndex( qIndexed, "b" ).val ).toBe( "2" );
				// "a" is not part of the result
				expect( QueryRowByIndex( qIndexed, "a", -1 ) ).toBe( -1 );
			});

			it( title='qoq without indexName is not indexed', body=function() {
				var q = QueryNew( "id", "integer", [ [ 1 ] ] );
				var qNotIndexed = queryExecute( "SELECT id FROM q", {}, { dbtype: "query" } );
				expect( function() {
					QueryRowByIndex( qNotIndexed, 1 );
				}).toThrow();
			});

			it( title='queryIndex normal query', skip=isMySqlNotSupported(), body=function() {
				```
				<cfquery name="local.q" indexName="table_name" datasource="#mySqlCredentials()#" maxrows=4>
					SELECT	table_name, engine
					FROM	INFORMATION_SCHEMA.TABLES
				</cfquery>
				```
				systemOutput(q, true);
				expect( q.recordcount ).toBe( 4 );
				var res = QueryRowDataByIndex( q, q.table_name[ 1 ] );
				systemOutput(res, true);
				expect( res.engine ).toBe( q.engine[ 1 ] )
			});

		});
	}

	function isMySqlNotSupported() {
		var mySql = mySqlCredentials();
		return isEmpty(mysql);
	}

	private struct function mySqlCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}

}
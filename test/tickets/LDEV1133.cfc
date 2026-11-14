component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function beforeAll() {
		variables.data = queryNew( "id,name", "integer,varchar", [ [ 1, "a" ], [ 2, "b" ] ] );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1133 attribute key as alias of columnKey", function() {

			it( title="queryExecute with returntype struct and key", body=function( currentSpec ) {
				var result = queryExecute(
					"SELECT id, name FROM data ORDER BY id",
					{}, { dbtype: "query", returntype: "struct", key: "id" }
				);
				expect( result ).toBeStruct();
				expect( structKeyList( result ) ).toBe( "1,2" );
				expect( result[ 2 ].name ).toBe( "b" );
			});

			it( title="cfquery tag with returntype struct and key", body=function( currentSpec ) {
				query name="local.result" dbtype="query" returntype="struct" key="id" {
					echo( "SELECT id, name FROM data ORDER BY id" );
				}
				expect( result ).toBeStruct();
				expect( structKeyList( result ) ).toBe( "1,2" );
				expect( result[ 1 ].name ).toBe( "a" );
			});

			it( title="columnKey still works", body=function( currentSpec ) {
				var result = queryExecute(
					"SELECT id, name FROM data ORDER BY id",
					{}, { dbtype: "query", returntype: "struct", columnKey: "id" }
				);
				expect( structKeyList( result ) ).toBe( "1,2" );
			});

		});
	}

}

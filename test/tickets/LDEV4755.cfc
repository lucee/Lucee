component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.suffix = "LDEV4755";

	function beforeAll() {
		defineDatasource();
		try {
			queryExecute( "drop table T#suffix#" );
		}
		catch ( any e ) {
		}
		queryExecute( "create table T#suffix# ( id int NOT NULL, n int NOT NULL )" );
		loop from=1 to=3 index="local.i" {
			queryExecute( "insert into T#suffix# ( id, n ) values ( :id, :n )", {
				id: { value: i, type: "integer" },
				n: { value: i * 2, type: "integer" }
			} );
		}
	}

	function afterAll() {
		try {
			queryExecute( "drop table T#suffix#" );
		}
		catch ( any e ) {
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-4755 queryLazy accepts the same aliases for columnKey as queryExecute", function() {

			it( title="queryExecute with columnKey", body=function( currentSpec ) {
				var result = queryExecuteStruct( "columnKey" );
				assertStruct( result );
			});

			it( title="queryExecute with keyColumn", body=function( currentSpec ) {
				var result = queryExecuteStruct( "keyColumn" );
				assertStruct( result );
			});

			it( title="queryLazy with columnKey", body=function( currentSpec ) {
				var result = queryLazyStruct( "columnKey" );
				assertStruct( result );
			});

			it( title="queryLazy with keyColumn", body=function( currentSpec ) {
				var result = queryLazyStruct( "keyColumn" );
				assertStruct( result );
			});

			it( title="queryLazy with columnName", body=function( currentSpec ) {
				var result = queryLazyStruct( "columnName" );
				assertStruct( result );
			});

			it( title="queryLazy with column", body=function( currentSpec ) {
				var result = queryLazyStruct( "column" );
				assertStruct( result );
			});

			it( title="queryLazy still throws when no columnKey or alias is given", body=function( currentSpec ) {
				expect( function() {
					queryLazy(
						sql: "SELECT * FROM T#suffix# order by id",
						listener: function( row ) {},
						options: { returnType: "struct" }
					);
				}).toThrow( "application" );
			});

			it( title="queryLazy still throws when the alias is not a column", body=function( currentSpec ) {
				expect( function() {
					queryLazyStruct( "keyColumn", "not_a_column" );
				}).toThrow();
			});

		});
	}

	private struct function queryExecuteStruct( required string attrName, string value="id" ) localmode="true" {
		var options = { returnType: "struct" };
		options[ arguments.attrName ] = arguments.value;
		return queryExecute( "SELECT * FROM T#suffix# order by id", {}, options );
	}

	// a large blockfactor delivers all rows in one call; merge in case rows arrive in several blocks
	private struct function queryLazyStruct( required string attrName, string value="id" ) localmode="true" {
		var options = { returnType: "struct", blockfactor: 100 };
		options[ arguments.attrName ] = arguments.value;
		var result = {};
		queryLazy(
			sql: "SELECT * FROM T#suffix# order by id",
			listener: function( rows ) {
				structAppend( result, arguments.rows );
			},
			options: options
		);
		return result;
	}

	private void function assertStruct( required struct result ) {
		expect( structCount( arguments.result ) ).toBe( 3 );
		expect( arguments.result ).toHaveKey( "1" );
		expect( arguments.result ).toHaveKey( "3" );
		expect( arguments.result[ "2" ].n ).toBe( 4 );
	}

	private void function defineDatasource() {
		application action="update" datasource="#server.getDatasource( "h2", server._getTempDir( "LDEV4755" ) )#";
	}

}

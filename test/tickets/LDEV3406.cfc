component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3406", function() {

			it( title="serializeJSON() of a query with a bit column writes true/false (row format)", body=function( currentSpec ) {
				var qry = queryNew( "BITCOL", "bit", [ { "BITCOL": 0 }, { "BITCOL": 1 } ] );
				expect( serializeJSON( qry ) ).toBe( '{"COLUMNS":["BITCOL"],"DATA":[[false],[true]]}' );
			});

			it( title="serializeJSON() of a query with a bit column writes true/false (column format)", body=function( currentSpec ) {
				var qry = queryNew( "BITCOL", "bit", [ { "BITCOL": 0 }, { "BITCOL": 1 } ] );
				expect( serializeJSON( qry, "column" ) ).toBe( '{"ROWCOUNT":2,"COLUMNS":["BITCOL"],"DATA":{"BITCOL":[false,true]}}' );
			});

			it( title="serializeJSON() of a query with a bit column writes true/false (struct format)", body=function( currentSpec ) {
				var qry = queryNew( "BITCOL", "bit", [ { "BITCOL": 0 }, { "BITCOL": 1 } ] );
				expect( serializeJSON( qry, "struct" ) ).toBe( '[{"BITCOL":false},{"BITCOL":true}]' );
			});

			it( title="serializeJSON() of a query with bit and integer columns only changes the bit column", body=function( currentSpec ) {
				var qry = queryNew( "ID,FLAG", "integer,bit", [ { "ID": 1, "FLAG": 0 }, { "ID": 2, "FLAG": 1 } ] );
				expect( serializeJSON( qry ) ).toBe( '{"COLUMNS":["ID","FLAG"],"DATA":[[1,false],[2,true]]}' );
			});

			it( title="serializeJSON() of a query with a varchar column still keeps the value as is", body=function( currentSpec ) {
				var qry = queryNew( "STR", "varchar", [ { "STR": "true" }, { "STR": "0" } ] );
				expect( serializeJSON( qry ) ).toBe( '{"COLUMNS":["STR"],"DATA":[["true"],["0"]]}' );
			});

		});
	}

}

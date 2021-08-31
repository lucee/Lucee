component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1206", function() {
			it(title="Adding row to query values from struct", body = function( currentSpec ) {
				var tmpQry = queryNew( 'id,name');
				tmpQry.addRow( { id : 'foo', name : 'bar' } );
				tmpQry.addRow( { id : 1, name : 2 } );
				tmpQry.addRow( { id : { brad : 'wood' }, name : { luis : 'majano' } } );
				tmpQry.addRow( { id : [ 1 ], name : [ 'a', 'b', 'c' ] } );
				var tmpStruct = QueryRowData( tmpQry, 4 );
				expect(tmpStruct.id).toBeTypeOf("array");
			});

			it(title="Adding row to query values from Array ", body = function( currentSpec ) {
				var tmpQry = queryNew( 'id,name');
				tmpQry.addRow( [ id = 'foo', name = 'bar' ]);
				tmpQry.addRow( [ id = 1, name = 2 ] );
				tmpQry.addRow( [ id = { brad : 'wood' }, name = { luis : 'majano' } ] );
				tmpQry.addRow( [ {id = [ 1 ], name = [ 'a', 'b', 'c' ] } ] );
				var tmpStruct = QueryRowData( tmpQry, 4 );
				expect(tmpStruct.id).toBeTypeOf("array");
			});
		});
	}
}

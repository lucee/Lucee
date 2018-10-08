component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2027", body=function() {
			it( title='checking QueryFilter Function inside cfloop',body=function( currentSpec ) {
				var QueryMain = queryNew("id,Row", "integer,varchar",
					[
					{"id":1,"Row":"Row 1"},
					{"id":2,"Row":"Row 2"},
					{"id":3,"Row":"Row 3"}
					]
				);
				cfloop (query = QueryMain) {
					var current_Row = row;
					var current_ID = id;
					var current_currentrow = QueryMain.currentrow;
					
					QueryFiltered = QueryMain.filter(function(row) {
						return row.Row == currentRow;
					});
					assertEquals(current_ID, QueryMain.id);
					assertEquals(current_Row, row);
					assertEquals(current_currentrow, QueryMain.currentrow);
				}
			});
		});
	}
}

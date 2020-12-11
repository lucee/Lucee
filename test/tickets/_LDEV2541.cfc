component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textBox ) {
		describe("Test case for LDEV-2541", function(){
			qry1 = queryNew( 'id,name,title', 'integer,varchar,varchar' );
			qry1.addRow( [ 1,'alex', 'event1' ] );
			qry1.addRow( [ 2,'john', 'event2' ] );
			qry1.addRow( [ 3,'chris', 'event3' ] );
			qry1.addRow( [ 4,'dave', 'event4' ] );

			qry2 = queryNew( 'id,name,title', 'integer,varchar,varchar' );
			qry2.addRow( [ 1,'alex', 'event1' ] );
			qry2.addRow( [ 2,'john', 'event2' ] );
			qry2.addRow( [ 3,'chris', 'event3' ] );
			qry2.addRow( [ 4,'dave', 'event4' ] );

			it( title = "Checking query with rows order", body = function( currentSpec ){
				cfquery( dbtype = "query", name = "testRow" ) {
	    			writeOutput( 'SELECT * FROM qry1 UNION SELECT * FROM qry2' );
				}
				expect(valueList(testRow.id)).toBe("1,2,3,4");
			});

			it( title = "Checking query with maxrows", body = function( currentSpec ){
				cfquery( dbtype = "query", name = "testMaxrow", maxrows = "2") {
	    			writeOutput( 'SELECT * FROM qry1 UNION SELECT * FROM qry2' );
				}
				expect(valueList(testMaxrow.name)).toBe("alex,john");
			});
		});
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Test suite for queryrecordcount", body = function() {

			it( title = 'Checking with queryrecordcount()',body = function( currentSpec ) {
				var query = querynew(
					"id,name",
					"int,varchar",
					[
						{"id":1,"name":"student1"},
						{"id":2,"name":"student2"},
						{"id":3,"name":"student3"},
						{"id":4,"name":"student4"}
					]
				);
				var query2 = querynew("id,name");
				assertEquals("4",queryrecordcount(query));
				assertEquals("4",query.recordcount());
				assertEquals("0",queryrecordcount(query2));
				assertEquals("0",query2.recordcount());
			});
		});

	}
}
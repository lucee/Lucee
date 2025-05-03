component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Test suite for minute()", body = function() {
			it( title = "checking minute() function", body = function( currentSpec ) {
				var dt = createdatetime(2023,12,25,5,30,25);
				assertEquals(30,minute(dt));
				assertEquals(30,dt.minute());
			});
		});
	}
}

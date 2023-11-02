component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	function run( testResults , testBox ) {
		describe( "test case for find", function() {
			it(title = "Checking with find", body = function( currentSpec ) {
				
				assertEquals(find("x","Susi Sorglos"),0);
				assertEquals(find("s","Susi Sorglos"),3);
				assertEquals(find("s","Susi Sorglos",3),3);
				assertEquals(find("s","Susi Sorglos",4),12);
			});
		});
	}
}
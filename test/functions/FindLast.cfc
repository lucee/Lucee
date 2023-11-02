component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	function run( testResults , testBox ) {
		describe( "test case for find", function() {
			it(title = "Checking with findLast", body = function( currentSpec ) {
				
				assertEquals(findLast("s","Susi Sorglos"),12);
				assertEquals(findLast("s","Susi Sorglos",1),0);
				assertEquals(findLast("s","Susi Sorglos",4),3);

			});
		});
	}
}
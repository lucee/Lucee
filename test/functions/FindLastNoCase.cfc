component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	function run( testResults , testBox ) {
		describe( "test case for find", function() {
			it(title = "Checking with findLastNoCase", body = function( currentSpec ) {
				
				assertEquals(findLastNoCase("s","Susi Sorglos"),12);
				assertEquals(findLastNoCase("s","Susi Sorglos",1),1);
				assertEquals(findLastNoCase("s","Susi Sorglos",4),3);

			});
		});
	}
}
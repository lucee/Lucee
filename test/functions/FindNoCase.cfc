component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	function run( testResults , testBox ) {
		describe( "test case for find", function() {
			it(title = "Checking with findNoCase", body = function( currentSpec ) {
				
				assertEquals(findNoCase("x","Susi Sorglos"),0);
				assertEquals(findNoCase("s","Susi Sorglos"),1);
				assertEquals(findNoCase("s","Susi Sorglos",1),1);
				assertEquals(findNoCase("s","Susi Sorglos",2),3);
			});
		});
	}
}
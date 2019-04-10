component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for Exp", function() {
			it(title = "Checking with Exp", body = function( currentSpec ) {
				assertEquals("2.7182818284" ,"#left(exp(1),12)#");
				assertEquals("7.3890560989" ,"#left(exp(2),12)#");
				assertEquals("20.085536923" ,"#left(exp(3),12)#");
				assertEquals("3.3201169227" ,"#left(exp(1.2),12)#");
			});
		});	
	}
}
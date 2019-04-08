component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Chr()", body=function() {
			it(title="checking Chr() function", body = function( currentSpec ) {
				assertEquals("","#chr(0)#");
				assertEquals("&","#chr(38)#");
				assertEquals("	","#chr(9)#");
				try{
					assertEquals("","#chr(-1)#");
					fail("must throw:chr(-1)");
				} catch(any e){}
			});
		});
	}
}

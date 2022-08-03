component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ASin()", body=function() {
			it(title="Checking ASin() function", body = function( currentSpec ) {
				var res="0.304692654015"; 
				assertEquals(res,tostring( left(asin(0.3),len(res))  ));
				try{
					assertEquals("0",tostring(asin(1.3)));
					fail("must throw:1.3 must be within range: ( -1 : 1 )");
				} catch(any e){}
			});
		});
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ASin()", body=function() {
			it(title="Checking ASin() function", body = function( currentSpec ) {
				var res="0.3046926540153975"; 
				assertEquals(res,tostring( asin(0.3) ));
			});
			it(title="Checking ASin() function invaid input", body = function( currentSpec ) {
				try{
					assertEquals("0",tostring(asin(1.3)));
					fail("must throw:1.3 must be within range: ( -1 : 1 )");
				} catch(any e){}
			});
		});
	}
}
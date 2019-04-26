component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitMaskClear()", body=function() {
			it(title="Checking BitMaskClear() function", body = function( currentSpec ) {
				assertEquals("1",BitMaskClear(3,1,1));
				assertEquals("3",BitMaskClear(3,2,1));
				assertEquals("3",BitMaskClear(31,2,4));
				try{
					assertEquals("3",BitMaskClear(31,32,4));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}

				try{
					assertEquals("3",BitMaskClear(31,-1,4));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}

				try{
					assertEquals("3",BitMaskClear(31,1,32));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}
					
				try{
					assertEquals("3",BitMaskClear(31,1,-1));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}
	
			});
		});
	}
}

component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitMaskRead()", body=function() {
			it(title="Checking BitMaskRead() function", body = function( currentSpec ) {
				assertEquals("1",BitMaskRead(3,1,1));
				assertEquals("0",BitMaskRead(3,2,1));
				assertEquals("7",BitMaskRead(31,2,4));
				try{
					assertEquals("3",BitMaskRead(31,32,4));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}
				
				try{
					assertEquals("3",BitMaskRead(31,-1,4));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}

				try{
					assertEquals("3",BitMaskRead(31,1,32));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}
				
				try{
					assertEquals("3",BitMaskRead(31,1,-1));
					fail("must throw:Invalid argument for function BitMaskClear.");
				} catch(any e){}
			});
		});
	}
}

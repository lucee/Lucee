component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for acos()", body=function() {
			it(title="checking acos() function", body = function( currentSpec ) {
				assertEquals(0,acos(1));
				assertEquals(left("0.795398830184",14),tostring(acos(0.7)));
				try{
					assertEquals(1,tostring(acos(1.7)));
					fail("must throw:1.7 must be within range: ( -1 : 1 )");
				}
				catch(local.exp){}
			});
		});
	}
}
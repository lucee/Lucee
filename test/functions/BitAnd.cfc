component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for BitAnd()", body=function() {
			it(title="Checking BitAnd() function", body = function( currentSpec ) {
				assertEquals("0",BitAnd(1, 0));
				assertEquals("0",BitAnd(0, 0));
				assertEquals("0",BitAnd(1, 2));
				assertEquals("1",BitAnd(1, 3));
				assertEquals("1",BitAnd(3, 5));
				assertEquals("1",BitAnd(1, 1.0));
				assertEquals("0",BitAnd(1, 0.999999999999999999));
			});

			it(title="test outside the int range", body = function( currentSpec ) {
				var Integer=createObject("java","java.lang.Integer");
				assertEquals("1",BitAnd(1, Integer.MAX_VALUE));
				
				var failed=false;
				try {
					BitAnd(1, Integer.MAX_VALUE+1);
				}
				catch( e ) {
					failed=true;
				}
				assertEquals(true,failed);
			});
		});
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-366", function() {
			it( title='Checking "||" operator', body=function( currentSpec ) {
				var a = -1;
				var b = 0;
				var c = 1;
				assertEquals(-1,  "#a||a#");
				assertEquals(-1,  "#a||b#");
				assertEquals(-1,  "#a||c#");
				assertEquals(0,   "#b||b#");
				assertEquals(-1,  "#b||a#");
				assertEquals(1,   "#b||c#");
				assertEquals(1,   "#c||c#");
				assertEquals(1,   "#c||a#");
				assertEquals(1,   "#c||b#");
			});
		});
	}
}
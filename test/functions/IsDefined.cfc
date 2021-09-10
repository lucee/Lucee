component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for isDefined()", body=function() {
			it(title="checking isDefined() function", body = function( currentSpec ) {
				var myStruct = [ { b=1 } ];
				assertEquals(true, isDefined("myStruct[1]"));
        assertEquals(false, isDefined("myStruct[2]"));
        assertEquals(true, isDefined("myStruct[1].b"));
        assertEquals(false, isDefined("myStruct[1].c"));
			});
		});
	}
}

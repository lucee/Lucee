component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CompareNoCase()", body=function() {
			it(title="checking CompareNoCase() function", body = function( currentSpec ) {
				assertEquals("0","#compareNoCase("a","A")#");
				assertEquals("1","#compareNoCase("a","0")#");
				assertEquals("-1","#compareNoCase("aaaa","AAAAA")#");
			});
		});
	}
}
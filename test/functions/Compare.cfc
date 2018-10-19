component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Compare()", body=function() {
			it(title="checking Compare() function", body = function( currentSpec ) {
				assertEquals("0","#compare("0","0")#");
				assertEquals("0","#compare("0",0)#");
				assertEquals("0","#compare("","")#");
				assertEquals("-1","#compare("a","aa")#");
				assertEquals("1","#compare("a","A")#");
				assertEquals("1","#compare("a","0")#");
				assertEquals("-1","#compare("aaaa","aaaaa")#");
				assertEquals("-1","#compare("1","1.0")#");
				assertEquals("1","#compare("2","11")#");
			});
		});
	}
}

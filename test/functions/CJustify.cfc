component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Cjustify()", body=function() {
			it(title="checking Cjustify() function", body = function( currentSpec ) {
				assertEquals("abc","#Cjustify("abc",1)#");
				assertEquals("abc ","#Cjustify("abc",4)#");
				assertEquals(" abc ","#Cjustify("abc",5)#");
				assertEquals(" abc  ","#Cjustify("abc",6)#");
				try {
					assertEquals("abc","#Cjustify("abc",0)#");
					fail("must throw:Cjustify('abc',0)");
				} catch(any e){}
					
			});
		});
	}
}
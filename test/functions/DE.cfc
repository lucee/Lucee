component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for DE", function() {
			it(title = "Checking with DE", body = function( currentSpec ) {
				assertEquals("""1""", "#de(1)#");
				assertEquals("""hello""", "#de('hello')#");
				assertEquals("""h""""'ello""", "#de('h"''ello')#");
				assertEquals("""h""""'ello""", "#de('h"''ello')#");
				assertEquals("h""'ello", "#evaluate(de('h"''ello'))#");
			});
		});	
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListChangeDelims", function() {
			it(title = "Checking with ListChangeDelims", body = function( currentSpec ) {
				<!--- begin old test code --->
				assertEquals("", "#ListChangeDelims('',';','.')#");
				assertEquals("a", "#ListChangeDelims('a',';','.')#");
				assertEquals("a;b", "#ListChangeDelims('a.b',';','.')#");
				assertEquals("a;b", "#ListChangeDelims('a.b',';','.')#");
				assertEquals("a;b", "#ListChangeDelims('..a.b',';','.')#");
				assertEquals("a;b", "#ListChangeDelims('..a.b...',';','.')#");
				assertEquals("a;a", "#ListChangeDelims(',,,,,a,a,,,,',';')#");
				assertEquals("a;a", "#ListChangeDelims(',,,,,a,,,a,,,,',';')#");
				assertEquals("a;a", "#ListChangeDelims(',,,,,a,,,a,,,,',';',',:;')#");
				assertEquals("a;;b", "#ListChangeDelims('a,,b',';',',:;',true)#");
				assertEquals("a,,b", "#ListChangeDelims('a,,b',';',',:;',true,true)#");
				assertEquals("a;c", "#ListChangeDelims('a,,c',';')#");
				assertEquals("a;c", "#ListChangeDelims('a,,c',';',',',false)#");
				assertEquals("a;;c", "#ListChangeDelims('a,,c',';',',',true)#");
				<!--- end old test code --->
			});
		});	
	}
}
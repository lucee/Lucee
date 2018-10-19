component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListFind", function() {
			it(title = "Checking with ListFind", body = function( currentSpec ) {
				assertEquals("2", "#ListFind('abba,bb','bb')#");
				assertEquals("4", "#ListFind('abba,bb,AABBCC,BB','BB')#");
				assertEquals("0", "#ListFind('abba,bb,AABBCC','ZZ')#");
				assertEquals("2", "#ListFind(',,,,abba,bb,AABBCC','bb')#");
				assertEquals("2", "#ListFind(',,,,abba,,,,bb,AABBCC','bb')#");
				assertEquals("2", "#ListFind(',,,,abba,,,,bb,AABBCC','bb','.,;')#");
				assertEquals("2", "#ListFind('a,,c','c',',',false)#");
				assertEquals("3", "#ListFind('a,,c','c',',',true)#");
			});
		});	
	}
}

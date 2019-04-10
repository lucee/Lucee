component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListFindNoCase", function() {
			it(title = "Checking with ListFindNoCase", body = function( currentSpec ) {
				assertEquals("2", "#ListFindNoCase('abba,bb','bb')#");
				assertEquals("2", "#ListFindNoCase('abba,bb,AABBCC,BB','BB')#");
				assertEquals("0", "#ListFindNoCase('abba,bb,AABBCC','ZZ')#");
				assertEquals("2", "#ListFindNoCase('abba,,,,,,,bb,AABBCC,BB','BB')#");
				assertEquals("2", "#ListFindNoCase(',,,abba,,,,,,,bb,AABBCC,BB','BB')#");
				assertEquals("2", "#ListFindNoCase(',,,abba,,,,,,,bb,AABBCC,BB','BB','.,;')#");
				assertEquals("2", "#ListFindNoCase('a,,c','c',',',false)#");
				assertEquals("3", "#ListFindNoCase('a,,c','c',',',true)#");
				assertEquals("2", "#ListFindNoCase('a,,c','C',',',false)#");
			});
		});	
	}
}
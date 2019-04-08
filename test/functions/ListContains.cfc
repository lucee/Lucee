component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for ListContains", function() {
			it(title = "Checking with ListContains", body = function( currentSpec ) {
				<!--- begin old test code --->
				assertEquals("3", "#ListContains('a,b,abba,bcb','bb')#");
				assertEquals("3", "#ListContains('abba,bb,AABBCC','BB')#");
				assertEquals("0", "#ListContains('abba,bb,AABBCC','ZZ')#");
				assertEquals("0", "#ListContains(',,,,,abba,bb,AABBCC,,,,','ZZ')#");
				assertEquals("3", "#ListContains('abba,,,,,bb,AABBCC','BB')#");
				assertEquals("3", "#ListContains('abba,,,,,bb,AABBCC','BB',';,.')#");
				assertEquals("0", "#listcontains("evaluate,expression","")#");
				assertEquals("0", "#listcontains("evaluate,,expression","")#");
				assertEquals("2", "#listcontains("evaluate,,expression","expression")#");
				assertEquals("3", "#listcontains("evaluate, ,expression","expression")#");
				assertEquals("3", "#listcontains("evaluate, ,expression","expression",',',true)#");
				assertEquals("3", "#listcontains("evaluate,,expression","expression",',',true)#");
				assertEquals("3", "#listcontains("evaluate, ,expression","expression",',',false)#");
				assertEquals("2", "#listcontains("evaluate,,expression","expression",',',false)#");
				assertEquals("2", "#listcontains("evaluate,,expression","expression",';,',false,false)#");
				assertEquals("1", "#listcontains("evaluate,,expression","expression",';,',false,true)#");
				<!--- end old test code --->
			});
		});	
	}
}
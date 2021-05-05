component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for toBase64", function() {

			it(title = "Checking with toBase64", body = function( currentSpec ) {

				assertEquals('c29tZSBzdHJpbmcgdG8gZW5jb2Rl',"#'some string to encode'.toBase64()#");	
				assertEquals('c29tZSBzdHJpbmcgdG8gZW5jb2Rl',"#toBase64('some string to encode')#");
			});		
		});	
	}
}
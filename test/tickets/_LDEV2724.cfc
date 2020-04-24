component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2724", function() {
			it(title = "Extended ASCII characters inside thread", body = function( currentSpec ) {
				processingdirective 
				pageencoding = "utf-8";
				testString = "¥400 yen price";
				thread
					name = "thread"
					string = testString
				{
					writeOutput(testString);
				}
				expect("¥400 yen price").toBe(thread.Output);
			});

			it(title = "Extended ASCII characters outside thread", body = function( currentSpec ) {
				char = chr(254);
				expect('þ').toBe(char);
				char = chr(200);
				expect('È').toBe(char);
				char = chr(196);
				expect('Ä').toBe(char);
				char = chr(244);
				expect('ô').toBe(char);
			});
		});
	}
}
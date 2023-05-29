component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for urlEncode()", body=function() {
			it(title="checking urlEncode() function", body = function( currentSpec ) {
				assertEquals("http%3A%2F%2F", "#urlEncode("http://")#");
				assertEquals("https%3A%2F%2Fdev.lucee.org%2Ft%2Fwelcome-to-lucee-dev%2F2064", "#urlEncode("https://dev.lucee.org/t/welcome-to-lucee-dev/2064")#");
				assertEquals("._%3A", "#urlEncode('._:')#");
				assertEquals("-%3Alucee", "#urlEncode('-:lucee')#");
				assertEquals("https%3A%2F%2Fwww.w3.org%2FTR%2Fhtml40%2Fappendix%2Fnotes.html", "#urlEncode('https://www.w3.org/TR/html40/appendix/notes.html')#");
			});
		});
	}
}
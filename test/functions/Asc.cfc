component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Asc()", body=function() {
			it(title="Checking Asc() function", body = function( currentSpec ) {
				assertEquals("97",asc("a"));
				assertEquals("65",asc("A"));
				assertEquals("9",asc("	"));
				assertEquals("0",asc(""));
				assertEquals("97",asc("abc"));
			});
		});
	}
}
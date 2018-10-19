component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1997", function() {
			it( title='Checking getPageContext().getRequest().getRequestUrl()', body=function( currentSpec ) {
				var getURL = getPageContext().getRequest().getRequestUrl().toString();
				var result= getURL.Split("/");
				expect(result[3]).toBe(CGI.SERVER_NAME);
			});
		});
	}
} 

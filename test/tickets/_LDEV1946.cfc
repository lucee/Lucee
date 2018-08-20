component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1946", function() {
			it(title = "checking onMissingMethod() & onError() ", body = function( currentSpec ) {
				cfhttp(url= "http://" &CGI.server_name &GetDirectoryFromPath( CGI.script_name ) &"LDEV1946/test.cfm") { }
				expect(cfhttp.filecontent).toBe('Missing');
			});
		});
	}
}
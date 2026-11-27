component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	variables.httpbin = server.getTestService("httpbin");
	variables.hasHttpbin = structCount(variables.httpbin) > 0;
	variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4011", function() {
			it( title="check cfhttp throwOnError to include method and url in error detail", body=function( currentSpec ) {
				if ( !variables.hasHttpbin ) return;
				var targetUrl = "#variables.httpbinUrl#/status/404";
				try {
					http url="#targetUrl#" throwonerror=true method="post";
				}
				catch(any e) {
					var details = e.detail;
				}
				expect(details).toBe("POST #targetUrl#");
			});
		});
	}

}
component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	variables.updateProvider = server.getTestService("updateProvider").url;

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4011", function() {
			it( title="check cfhttp throwOnError to include method and url in error detail", body=function( currentSpec ) {
				try {
					http url="#variables.updateProvider#/rest/update/echoPost?statuscode=404" throwonerror=true method="post";
				}
				catch(any e) {
					var details = e.detail;
				}
				expect(details).toBe("POST #variables.updateProvider#/rest/update/echoPost?statuscode=404");
			});
		});
	}

}
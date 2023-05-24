component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4011", function() {
			it( title="check cfhttp throwOnError to include method and url in error detail", body=function( currentSpec ) {
				try {
					http url="https://update.lucee.org/rest/update/404" throwonerror=true method="post";
				}
				catch(any e) {
					var details = e.detail;
				}
				expect(details).toBe("POST https://update.lucee.org/rest/update/404");
			});
		});
	}

}
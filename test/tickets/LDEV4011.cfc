component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	variables.updateProvider = server.getTestService("updateProvider").url;

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4011", function() {
			it( title="check cfhttp throwOnError to include method and url in error detail", body=function( currentSpec ) {
				var details = "";
				try {
					http url="#variables.updateProvider#/rest/update/provider/echoGet?statuscode=404" throwonerror=true method="get";
				}
				catch(any e) {
					details = e.detail;
				}
				expect( details ).toBe( "GET #variables.updateProvider#/rest/update/provider/echoGet?statuscode=404" );
			});
		});
	}

}
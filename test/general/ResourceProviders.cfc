component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults , testBox ) {
		describe( "Test suite for the getResourceProviders", function() {

			it( title='test if getResourceProviders works', body=function( currentSpec ) {
				admin 
					action="getResourceProviders" 
					type="server" 
					returnVariable="local.resourceProviders" 
					password="#server.SERVERADMINPASSWORD#";
				expect( local.resourceProviders ).toBeQuery();
				// systemOutput(local.providers, true);
			});
			
		});
	}

}

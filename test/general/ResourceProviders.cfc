component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for cfadmin ResourceProviders", function() {

			it( title='test if cfadmin getResourceProviders works', body=function( currentSpec ) {
				admin 
					action="getResourceProviders"
					type="server" 
					returnVariable="local.resourceProviders"
					password="#server.SERVERADMINPASSWORD#";

				expect( local.resourceProviders ).toBeQuery();
				expect( local.resourceProviders.recordcount ).toBeGTE( 1 );

			});
			
		});
	}

}

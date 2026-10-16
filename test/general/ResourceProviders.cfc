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

			// LDEV-6295: a resource provider declared with a scheme but no class (the pattern used by
			// extension-provided providers such as "s3") must not abort resource/config loading. The entry
			// should be skipped and the built-in default used instead, rather than throwing "no class defined".
			it( title='a resource provider without a class does not break loading', body=function( currentSpec ) {
				var scheme = "ldev6295test";
				try {
					admin
						action="updateResourceProvider"
						type="server"
						scheme=scheme
						arguments="lock-timeout:1000;"
						password="#server.SERVERADMINPASSWORD#";

					admin
						action="getResourceProviders"
						type="server"
						returnVariable="local.providers"
						password="#server.SERVERADMINPASSWORD#";

					expect( local.providers ).toBeQuery();
					expect( local.providers.recordcount ).toBeGTE( 1 );
				}
				finally {
					admin
						action="removeResourceProvider"
						type="server"
						scheme=scheme
						password="#server.SERVERADMINPASSWORD#";
				}
			});

		});
	}

}

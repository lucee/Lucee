component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4717", function() {

			it( title='Test if cfadmin getApplicationListener works', body=function( currentSpec ) {
				admin 
					action="getApplicationListener"
					type="server" 
					returnVariable="local.resourceProviders"
					password="#request.SERVERADMINPASSWORD#";

				expect( isStruct(local.resourceProviders) ).toBeTrue();
				expect( local.resourceProviders ).toHaveKey( "mode" );
				expect( local.resourceProviders ).toHaveKey( "type" );
				expect( local.resourceProviders ).toHaveKey( "applicationPathTimeout" );
				expect( local.resourceProviders.count() ).toBe( 3 );
			});
		});
	}
}
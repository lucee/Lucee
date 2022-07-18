component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2558", function() {
			it(title = "cfadmin getRHExtensions should return at least 10 extensions", body = function( currentSpec ) {
				admin action="getRHExtensions"
					type="server"
					password="#request.SERVERADMINPASSWORD#"
					returnVariable="local.extensions";

				expect(len(extensions)).toBeGT(10);
			});

			it(title = "getPageContext().getConfig().getServerRHExtensions() should return at least 10 extensions", body = function( currentSpec ) {
				local.extensions=getPageContext().getConfig().getServerRHExtensions();
				expect(len(extensions)).toBeGT(10);
			});

		});
	}
}
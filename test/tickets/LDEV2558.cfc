component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2558", function() {
			it(title = "cfadmin", body = function( currentSpec ) {
				admin action="getRHExtensions"
					type="server"
					password="#request.SERVERADMINPASSWORD#"
					returnVariable="local.extensions";

				expect(len(extensions)>10).toBe(true);
			});

			it(title = "getPageContext().getConfig().getServerRHExtensions()", body = function( currentSpec ) {
				local.extensions=getPageContext().getConfig().getServerRHExtensions();
				expect(len(extensions)>10).toBe(true);
			});

		});
	}
}
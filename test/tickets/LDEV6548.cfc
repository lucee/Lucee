component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		if ( isNull( request.serverAdminPassword ) ) request.serverAdminPassword = "webweb";
		admin action="getProxy" type="server" password="#request.serverAdminPassword#" returnVariable="variables.orgProxy";
	}

	function afterAll() {
		if ( len( variables.orgProxy.server ) ) {
			admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
				proxyenabled=true
				proxyserver="#variables.orgProxy.server#"
				proxyport="#len( variables.orgProxy.port ) ? variables.orgProxy.port : -1#"
				proxyusername="#variables.orgProxy.username#"
				proxypassword="#variables.orgProxy.password#";
		}
		else {
			admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
				proxyenabled=false proxyserver="" proxyport=-1 proxyusername="" proxypassword="";
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6548 server proxy from .CFConfig.json is ignored", function() {

			it( "getProxyData() returns the server proxy set via updateProxy", function() {
				admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
					proxyenabled=true proxyserver="ldev6548-proxy.example" proxyport=3128 proxyusername="user" proxypassword="secret";

				var pd = getPageContext().getConfig().getProxyData();
				expect( isNull( pd ) ).toBeFalse( "server proxy should not be null" );
				expect( pd.getServer() ).toBe( "ldev6548-proxy.example" );
				expect( pd.getPort() ).toBe( 3128 );
				expect( pd.getUsername() ).toBe( "user" );
				expect( pd.getPassword() ).toBe( "secret" );

				admin action="getProxy" type="server" password="#request.serverAdminPassword#" returnVariable="local.proxy";
				expect( proxy.server ).toBe( "ldev6548-proxy.example" );
				expect( proxy.port ).toBe( "3128" );
			});

			it( "getProxyData() returns null when the server proxy is disabled", function() {
				admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
					proxyenabled=false proxyserver="" proxyport=-1 proxyusername="" proxypassword="";

				expect( isNull( getPageContext().getConfig().getProxyData() ) ).toBeTrue();
			});

		});
	}
}

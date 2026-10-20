component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		if ( isNull( request.serverAdminPassword ) ) request.serverAdminPassword = "webweb";
		admin action="getProxy" type="server" password="#request.serverAdminPassword#" returnVariable="variables.orgProxy";
		variables.downloader = createObject( "java", "lucee.commons.net.http.HTTPEngine" );
		// .invalid never resolves (RFC 2606), so without the proxy the request fails with UnknownHostException
		variables.target = createObject( "java", "java.net.URL" ).init( "http://ldev6579.invalid/maven2/test.pom" );
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
		describe( "LDEV-6579 Maven downloads use the common downloader (server proxy)", function() {

			it( "getResponse() goes through the configured server proxy", function() {
				// nothing listens on port 1, so the request must fail connecting to the proxy
				admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
					proxyenabled=true proxyserver="127.0.0.1" proxyport=1 proxyusername="" proxypassword="";
				try {
					var msg = "";
					try {
						variables.downloader.getResponse( variables.target, nullValue(), nullValue(), 2000, 2000, nullValue() );
						msg = "no exception";
					}
					catch ( any e ) {
						// HTTPEngine wraps the connect error, the proxy host is in the cause (stacktrace)
						msg = e.type & " " & e.message & " " & e.stacktrace;
					}
					expect( msg ).notToInclude( "UnknownHost", "the proxy was bypassed: " & msg );
					expect( msg ).toInclude( "127.0.0.1", msg );
				}
				finally {
					admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
						proxyenabled=false proxyserver="" proxyport=-1 proxyusername="" proxypassword="";
				}
			});

			it( "getResponse() without a proxy connects directly", function() {
				admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
					proxyenabled=false proxyserver="" proxyport=-1 proxyusername="" proxypassword="";
				var msg = "";
				try {
					variables.downloader.getResponse( variables.target, nullValue(), nullValue(), 2000, 2000, nullValue() );
					msg = "no exception";
				}
				catch ( any e ) {
					msg = e.type & " " & e.message;
				}
				expect( msg ).toInclude( "ldev6579.invalid", msg );
			});

		});
	}
}

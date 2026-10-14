component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// nothing listens on the proxy port, and the target host never resolves: the error says which one was tried
	variables.proxyHost = "127.0.0.1";
	variables.proxyPort = 1;
	variables.target = "http://ldev6581.invalid/";

	function beforeAll() {
		if ( isNull( request.serverAdminPassword ) ) request.serverAdminPassword = "webweb";
		admin action="getProxy" type="server" password="#request.serverAdminPassword#" returnVariable="variables.orgProxy";
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6581 internal downloads use the server proxy", function() {

			// the overloads the internal downloaders use pass a null proxy, which falls back to the server proxy
			it( "HTTPEngine.get( url ) falls back to the server proxy", function() {
				var err = _errorWithDeadProxy( function( engine, targetUrl ) {
					engine.get( targetUrl );
				} );
				expect( err ).toInclude( "#variables.proxyHost#:#variables.proxyPort#", "should have tried the proxy, got [#err#]" );
			});

			it( "HTTPEngine.head( url ) falls back to the server proxy", function() {
				var err = _errorWithDeadProxy( function( engine, targetUrl ) {
					engine.head( targetUrl );
				} );
				expect( err ).toInclude( "#variables.proxyHost#:#variables.proxyPort#", "should have tried the proxy, got [#err#]" );
			});

			it( "HTTPEngine.getAsString() uses the server proxy", function() {
				var err = _errorWithDeadProxy( function( engine, targetUrl ) {
					engine.getAsString( targetUrl, "UTF-8" );
				} );
				expect( err ).toInclude( "#variables.proxyHost#:#variables.proxyPort#", "should have tried the proxy, got [#err#]" );
			});

			it( "http:// resources stay off the server proxy", function() {
				var err = _errorWithDeadProxy( function( engine, targetUrl ) {
					fileRead( variables.target );
				} );
				expect( err ).toInclude( "ldev6581.invalid", "should fail on the unresolvable host, got [#err#]" );
				expect( err ).notToInclude( "#variables.proxyHost#:#variables.proxyPort#", "should have gone direct, got [#err#]" );
			});

		});
	}

	// runs fn with the dead server proxy set, restores the original proxy whatever happens,
	// returns the stacktrace of the expected failure (the message only names the URL, the connect failure is in the cause)
	private string function _errorWithDeadProxy( required function fn ) {
		var engine = createObject( "java", "lucee.commons.net.http.HTTPEngine" );
		var targetUrl = createObject( "java", "java.net.URL" ).init( variables.target );
		var err = "";
		admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
			proxyenabled=true proxyserver="#variables.proxyHost#" proxyport=#variables.proxyPort# proxyusername="" proxypassword="";
		try {
			arguments.fn( engine, targetUrl );
		}
		catch ( any e ) {
			err = e.stacktrace;
		}
		finally {
			_restoreProxy();
		}
		expect( err ).notToBe( "", "should have failed, the proxy is dead and the host doesn't resolve" );
		return err;
	}

	private function _restoreProxy() {
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
}

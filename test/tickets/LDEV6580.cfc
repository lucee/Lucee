component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai,proxy" {

	function beforeAll() {
		if ( isNull( request.serverAdminPassword ) ) request.serverAdminPassword = "webweb";
		admin action="getProxy" type="server" password="#request.serverAdminPassword#" returnVariable="variables.orgProxy";
	}

	function afterAll() {
		restoreProxy();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6580 AI engines use the common HTTP engine (server proxy)", function() {

			var engines = [ "ldev6580-openai", "ldev6580-claude", "ldev6580-gemini" ];

			for ( var engine in engines ) {
				it( title="#engine# inquiry goes through the configured server proxy", data={ engine: engine }, body=function( data ) {
					// nothing listens on port 1, so the request must fail connecting to the proxy
					setProxy( "127.0.0.1", 1 );
					try {
						var msg = callEngine( data.engine, "inquiry" );
					}
					finally {
						restoreProxy();
					}
					expect( msg ).notToInclude( "UnknownHost", "the proxy was bypassed: " & msg );
					expect( msg ).notToInclude( ".invalid", "the proxy was bypassed: " & msg );
					expect( msg ).toInclude( "127.0.0.1", msg );
				});

				it( title="#engine# inquiry without a proxy connects directly", data={ engine: engine }, body=function( data ) {
					setProxy( "", -1 );
					try {
						var msg = callEngine( data.engine, "inquiry" );
					}
					finally {
						restoreProxy();
					}
					expect( msg ).toInclude( "#data.engine#.invalid", msg );
				});
			}

			for ( var engine in engines ) {
				it( title="#engine# model listing goes through the configured server proxy", data={ engine: engine }, body=function( data ) {
					setProxy( "127.0.0.1", 1 );
					try {
						var msg = callEngine( data.engine, "models" );
					}
					finally {
						restoreProxy();
					}
					expect( msg ).notToInclude( "UnknownHost", "the proxy was bypassed: " & msg );
					expect( msg ).toInclude( "127.0.0.1", msg );
				});
			}

		});
	}

	private string function callEngine( required string engine, required string action ) {
		var res = _InternalRequest(
			template: createURI( "LDEV6580/test.cfm" ),
			url: { engine: arguments.engine, action: arguments.action }
		);
		return trim( res.fileContent );
	}

	private function setProxy( required string server, required numeric port ) {
		if ( len( arguments.server ) ) {
			admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
				proxyenabled=true proxyserver="#arguments.server#" proxyport=arguments.port proxyusername="" proxypassword="";
		}
		else {
			admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
				proxyenabled=false proxyserver="" proxyport=-1 proxyusername="" proxypassword="";
		}
	}

	private function restoreProxy() {
		if ( len( variables.orgProxy.server ) ) {
			admin action="updateProxy" type="server" password="#request.serverAdminPassword#"
				proxyenabled=true
				proxyserver="#variables.orgProxy.server#"
				proxyport="#len( variables.orgProxy.port ) ? variables.orgProxy.port : -1#"
				proxyusername="#variables.orgProxy.username#"
				proxypassword="#variables.orgProxy.password#";
		}
		else {
			setProxy( "", -1 );
		}
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}

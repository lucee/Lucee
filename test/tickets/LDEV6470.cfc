component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// LDEV-6470 cfhttp attribute httpVersion: "1.1" (default), "2" and "auto". The built-in HTTP engine speaks only HTTP/1.1,
	// the HTTP/2 itself is done by the HTTP extension, which has the tests for it.

	variables.httpbin = server.getTestService( "httpbin" );
	variables.noServer = "http://127.0.0.1:1/ldev6470";

	private boolean function notBuiltInEngine() {
		return server.isHttpExtensionEngine();
	}

	private boolean function noHttpbin() {
		return structCount( variables.httpbin ) == 0;
	}

	private string function httpbinURL( required string path ) {
		return "http://#variables.httpbin.server#:#variables.httpbin.port#/#arguments.path#";
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6470 cfhttp attribute httpVersion", function() {

			it( title="an invalid value is an error of the tag, nothing is sent", body=function() {
				var msg = "";
				try {
					cfhttp( url=noServer, result="local.res", timeout=5, httpVersion="3" );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "invalid value for attribute httpVersion [3]" );
				expect( msg ).toInclude( "1.1, 2, auto" );
			});

			it( title="an invalid value is an error also with throwOnError=false", body=function() {
				var msg = "";
				try {
					cfhttp( url=noServer, result="local.res", timeout=5, throwOnError=false, httpVersion="http/3" );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "invalid value for attribute httpVersion" );
			});

			it( title="httpVersion=""2"" fails with a clear message with an engine without HTTP/2", skip=notBuiltInEngine(), body=function() {
				for ( var v in [ "2", "http/2", "H2" ] ) {
					var msg = "";
					try {
						cfhttp( url=noServer, result="local.res", timeout=5, httpVersion=v );
					}
					catch ( any e ) {
						msg = e.message;
					}
					expect( msg ).toInclude( "does not support HTTP/2" );
					expect( msg ).toInclude( "install or update the HTTP extension" );
				}
			});

			it( title="httpVersion=""2"" is an error also with throwOnError=false, it is not a connection failure", skip=notBuiltInEngine(), body=function() {
				var msg = "";
				try {
					cfhttp( url=noServer, result="local.res", timeout=5, throwOnError=false, httpVersion="2" );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "does not support HTTP/2" );
			});

			it( title="the default is HTTP/1.1", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "get" ), result="local.res", timeout=10 );
				expect( local.res.status_code ).toBe( 200 );
				expect( left( local.res.http_version, 7 ) ).toBe( "HTTP/1.", "an HTTP/1.x server answers with HTTP/1.x, never with HTTP/2" );
			});

			it( title="httpVersion=""1.1"" is HTTP/1.1", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "get" ), result="local.res", timeout=10, httpVersion="1.1" );
				expect( local.res.status_code ).toBe( 200 );
				expect( left( local.res.http_version, 7 ) ).toBe( "HTTP/1.", "an HTTP/1.x server answers with HTTP/1.x, never with HTTP/2" );
			});

			it( title="httpVersion=""auto"" uses HTTP/1.1 with an engine without HTTP/2 and with a server without HTTP/2", skip=( notBuiltInEngine() || noHttpbin() ), body=function() {
				cfhttp( url=httpbinURL( "get" ), result="local.res", timeout=10, httpVersion="auto" );
				expect( local.res.status_code ).toBe( 200 );
				expect( left( local.res.http_version, 7 ) ).toBe( "HTTP/1.", "an HTTP/1.x server answers with HTTP/1.x, never with HTTP/2" );
			});

		});
	}

}

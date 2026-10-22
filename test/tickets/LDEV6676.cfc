component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// LDEV-6676 the built-in HTTP engine fails with a clear message for every feature only the HTTP extension provides,
	// nothing is silently ignored. Nothing is sent for these requests, the engine checks them before it connects.

	variables.httpbin = server.getTestService( "httpbin" );
	variables.noServer = "http://127.0.0.1:1/ldev6676";

	// the messages are only thrown by the built-in engine, the HTTP extension engine supports these features
	private boolean function notBuiltInEngine() {
		return server.isHttpExtensionEngine();
	}

	private boolean function noHttpbin() {
		return notBuiltInEngine() || structCount( variables.httpbin ) == 0;
	}

	private boolean function extensionNoHttpbin( string feature="" ) {
		return structCount( variables.httpbin ) == 0 || server.needsHttpExtensionEngine( arguments.feature );
	}

	private string function httpbinURL( required string path ) {
		return "http://#variables.httpbin.server#:#variables.httpbin.port#/#arguments.path#";
	}

	private void function expectUnsupported( required function call, required string feature ) {
		try {
			arguments.call();
		}
		catch ( any e ) {
			expect( e.message ).toInclude( "the built-in HTTP engine does not support" );
			expect( e.message ).toInclude( arguments.feature );
			expect( e.message ).toInclude( "install the HTTP extension" );
			return;
		}
		fail( "the request with [#arguments.feature#] did not fail" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6676 built-in HTTP engine, features only the HTTP extension provides", function() {

			for ( var name in [ "Host", "Connection", "Expect", "Upgrade" ] ) {
				it( title="header [#name#] fails with a clear message", data={ name: name }, skip=notBuiltInEngine(), body=function( data ) {
					expectUnsupported( function() {
						cfhttp( url=noServer, result="local.res", timeout=5 ) {
							cfhttpparam( type="header", name=data.name, value="1" );
						}
					}, "setting the header [#data.name#]" );
				});
			}

			it( title="a restricted header is checked case insensitive", skip=notBuiltInEngine(), body=function() {
				expectUnsupported( function() {
					cfhttp( url=noServer, result="local.res", timeout=5 ) {
						cfhttpparam( type="header", name="hOsT", value="example.com" );
					}
				}, "setting the header [hOsT]" );
			});

			it( title="the message is thrown also with throwOnError=false, it is not a connection failure", skip=notBuiltInEngine(), body=function() {
				expectUnsupported( function() {
					cfhttp( url=noServer, result="local.res", timeout=5, throwOnError=false ) {
						cfhttpparam( type="header", name="Host", value="example.com" );
					}
				}, "setting the header [Host]" );
			});

			it( title="the message is thrown also when the request runs in its own thread (timeout set)", skip=notBuiltInEngine(), body=function() {
				expectUnsupported( function() {
					cfhttp( url=noServer, result="local.res", timeout=5, socketTimeout=5, throwOnError=false ) {
						cfhttpparam( type="header", name="Upgrade", value="h2c" );
					}
				}, "setting the header [Upgrade]" );
			});

			// cfhttp never passed a Content-Length header param on, with no engine, the length comes from the body
			it( title="a Content-Length header param is ignored as before, the length comes from the body", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "post" ), method="post", result="local.res", timeout=10 ) {
					cfhttpparam( type="header", name="Content-Length", value="1" );
					cfhttpparam( type="body", value="abcdef" );
				}
				expect( res.status_code ).toBe( 200 );
				expect( deserializeJSON( res.fileContent ).headers[ "Content-Length" ] ).toBe( "6" );
			});

			it( title="NTLM authentication fails with a clear message", skip=notBuiltInEngine(), body=function() {
				expectUnsupported( function() {
					cfhttp( url=noServer, result="local.res", timeout=5, username="u", password="p", authType="NTLM", domain="d", workstation="w" );
				}, "NTLM authentication" );
			});

			it( title="Basic authentication at a proxy for HTTPS fails with a clear message", skip=notBuiltInEngine(), body=function() {
				expectUnsupported( function() {
					// a host that is not local, local hosts never go through the proxy, nothing is resolved or sent
					cfhttp( url="https://ldev6676.invalid/", result="local.res", timeout=5, proxyServer="127.0.0.1", proxyPort=1, proxyUser="u", proxyPassword="p" );
				}, "Basic authentication at a proxy for a HTTPS request" );
			});

			it( title="Digest authentication fails with a clear message", skip=noHttpbin(), body=function() {
				expectUnsupported( function() {
					cfhttp( url=httpbinURL( "digest-auth/auth/user/passwd" ), result="local.res", timeout=10, username="user", password="passwd" );
				}, "the authentication scheme [Digest]" );
			});

			it( title="Basic authentication works, after the challenge of the server", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "basic-auth/user/passwd" ), result="local.res", timeout=10, username="user", password="passwd", preauth=false );
				expect( res.status_code ).toBe( 200 );
			});

			it( title="Basic authentication works, preemptive", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "basic-auth/user/passwd" ), result="local.res", timeout=10, username="user", password="passwd", preauth=true );
				expect( res.status_code ).toBe( 200 );
			});

			it( title="wrong Basic credentials end in a 401, not in an error", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "basic-auth/user/passwd" ), result="local.res", timeout=10, username="user", password="wrong" );
				expect( res.status_code ).toBe( 401 );
			});

			it( title="Basic authentication works with every engine", skip=structCount( variables.httpbin ) == 0, body=function() {
				cfhttp( url=httpbinURL( "basic-auth/user/passwd" ), result="local.res", timeout=10, username="user", password="passwd" );
				expect( res.status_code ).toBe( 200 );
			});
		});

		describe( "LDEV-6676 features only the HTTP extension engine provides", function() {

			it( title="the Host header is sent (needs the HTTP extension engine)", skip=extensionNoHttpbin( "Host header" ), body=function() {
				cfhttp( url=httpbinURL( "headers" ), result="local.res", timeout=10 ) {
					cfhttpparam( type="header", name="Host", value="ldev6676.example.com" );
				}
				expect( res.status_code ).toBe( 200 );
				expect( deserializeJSON( res.fileContent ).headers.Host ).toBe( "ldev6676.example.com" );
			});

			it( title="Digest authentication works (needs the HTTP extension engine)", skip=extensionNoHttpbin( "Digest authentication" ), body=function() {
				cfhttp( url=httpbinURL( "digest-auth/auth/user/passwd" ), result="local.res", timeout=10, username="user", password="passwd" );
				expect( res.status_code ).toBe( 200 );
			});
		});
	}
}

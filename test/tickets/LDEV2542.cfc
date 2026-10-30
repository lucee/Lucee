component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// LDEV-2542 the result of cfhttp has a key "request" with the request that was sent, sensitive values are masked

	variables.httpbin = server.getTestService( "httpbin" );

	// nothing listens there, the request key is set before the request is sent
	variables.noServer = "http://127.0.0.1:1/ldev2542";

	private boolean function noHttpbin() {
		return structCount( variables.httpbin ) == 0;
	}

	private string function httpbinURL( required string path ) {
		return "http://#variables.httpbin.server#:#variables.httpbin.port#/#arguments.path#";
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2542 cfhttp result key request", function() {

			it( title="has the method, url, finalUrl, headers and params", body=function() {
				cfhttp( url=variables.noServer, result="local.res", timeout=5, throwOnError=false );
				expect( res ).toHaveKey( "request" );
				expect( res.request ).toBeStruct();
				expect( res.request.method ).toBe( "GET" );
				expect( res.request.url ).toBe( variables.noServer );
				expect( res.request.finalUrl ).toBe( variables.noServer );
				expect( res.request.headers ).toBeStruct();
				expect( res.request.params ).toBeStruct();
				expect( res.request.params.url ).toBeStruct();
				expect( res.request.params.form ).toBeStruct();
				expect( res.request.params.file ).toBeStruct();
				expect( res.request.params.body ).toBeStruct();
				expect( res.request.headers ).toHaveKey( "User-Agent" );
			});

			it( title="has the method that is used", body=function() {
				cfhttp( url=variables.noServer, method="PUT", result="local.res", timeout=5, throwOnError=false );
				expect( res.request.method ).toBe( "PUT" );
			});

			it( title="has the url params in the url and in params.url", body=function() {
				cfhttp( url="#variables.noServer#?a=1", result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="url", name="b", value="x y" );
				}
				expect( res.request.url ).toBe( variables.noServer & "?a=1&b=x%20y" );
				expect( res.request.params.url.a ).toBe( "1" );
				expect( res.request.params.url.b ).toBe( "x y" );
			});

			it( title="has the header params, the values of sensitive headers are masked", body=function() {
				cfhttp( url=variables.noServer, result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="header", name="X-Ldev2542", value="visible" );
					cfhttpparam( type="header", name="Authorization", value="Bearer abc123" );
					cfhttpparam( type="header", name="Proxy-Authorization", value="Basic abc123" );
					cfhttpparam( type="header", name="Cookie", value="sid=abc123" );
					cfhttpparam( type="header", name="X-Api-Token", value="abc123" );
					cfhttpparam( type="header", name="X-Client-Secret", value="abc123" );
					cfhttpparam( type="header", name="X-Password", value="abc123" );
					cfhttpparam( type="header", name="X-Api-Key", value="abc123" );
				}
				var headers = res.request.headers;
				expect( headers[ "X-Ldev2542" ] ).toBe( "visible" );
				for ( var name in [ "Authorization", "Proxy-Authorization", "Cookie", "X-Api-Token", "X-Client-Secret", "X-Password", "X-Api-Key" ] ) {
					expect( headers[ name ] ).toBe( "***" );
				}
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
			});

			it( title="has the cookie params as masked Cookie header", body=function() {
				cfhttp( url=variables.noServer, result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="cookie", name="sid", value="abc123" );
				}
				expect( res.request.headers.cookie ).toBe( "***" );
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
			});

			it( title="has the Authorization header when credentials are sent with the request, the credentials are masked", body=function() {
				cfhttp( url=variables.noServer, username="ldev2542user", password="abc123", result="local.res", timeout=5, throwOnError=false );
				expect( res.request.headers.authorization ).toBe( "***" );
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
				expect( serializeJSON( res.request ) ).notToInclude( "YWJjMTIz" );
			});

			it( title="masks the user info and the sensitive params in the url", body=function() {
				cfhttp( url="http://ldev2542user:abc123@127.0.0.1:1/ldev2542?a=1&access_token=abc123&Password=abc123&secret=abc123&c=3", result="local.res", timeout=5, throwOnError=false );
				expect( res.request.url ).toBe( "http://***@127.0.0.1:1/ldev2542?a=1&access_token=***&Password=***&secret=***&c=3" );
				expect( res.request.finalUrl ).toBe( res.request.url );
				expect( res.request.params.url.a ).toBe( "1" );
				expect( res.request.params.url.c ).toBe( "3" );
				expect( res.request.params.url.access_token ).toBe( "***" );
				expect( res.request.params.url.password ).toBe( "***" );
				expect( res.request.params.url.secret ).toBe( "***" );
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
				expect( serializeJSON( res.request ) ).notToInclude( "ldev2542user" );
			});

			it( title="has the form fields, the values of sensitive fields are masked", body=function() {
				cfhttp( url=variables.noServer, method="POST", result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="formfield", name="user", value="susi" );
					cfhttpparam( type="formfield", name="password", value="abc123" );
					cfhttpparam( type="formfield", name="api_token", value="abc123" );
				}
				expect( res.request.method ).toBe( "POST" );
				expect( res.request.params.form.user ).toBe( "susi" );
				expect( res.request.params.form.password ).toBe( "***" );
				expect( res.request.params.form.api_token ).toBe( "***" );
				expect( res.request.headers[ "Content-Type" ] ).toInclude( "application/x-www-form-urlencoded" );
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
			});

			it( title="has a name used more than once as array", body=function() {
				cfhttp( url=variables.noServer, method="POST", result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="formfield", name="a", value="1" );
					cfhttpparam( type="formfield", name="a", value="2" );
					cfhttpparam( type="url", name="b", value="3" );
					cfhttpparam( type="url", name="b", value="4" );
				}
				expect( res.request.params.form.a ).toBe( [ "1", "2" ] );
				expect( res.request.params.url.b ).toBe( [ "3", "4" ] );
			});

			it( title="has the files and the form fields of a multipart request", body=function() {
				var file = getTempFile( getTempDirectory(), "ldev2542", "txt" );
				try {
					fileWrite( file, "abc123" );
					cfhttp( url=variables.noServer, method="POST", result="local.res", timeout=5, throwOnError=false ) {
						cfhttpparam( type="formfield", name="user", value="susi" );
						cfhttpparam( type="formfield", name="secret", value="abc123" );
						cfhttpparam( type="file", name="upload", file=file, mimetype="text/plain" );
					}
				}
				finally {
					fileDelete( file );
				}
				expect( res.request.headers[ "Content-Type" ] ).toBe( "multipart/form-data" );
				expect( res.request.params.form.user ).toBe( "susi" );
				expect( res.request.params.form.secret ).toBe( "***" );
				expect( res.request.params.file.upload ).toBe( listLast( file, "/\" ) );
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
			});

			it( title="has a summary of the body, not the content", body=function() {
				cfhttp( url=variables.noServer, method="POST", result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="body", value="abc123", mimetype="application/json" );
				}
				expect( res.request.params.body.type ).toBe( "text" );
				expect( res.request.params.body.length ).toBe( 6 );
				expect( res.request.params.body.mimetype ).toBe( "application/json" );
				expect( serializeJSON( res.request ) ).notToInclude( "abc123" );
			});

			it( title="has the length of a binary body", body=function() {
				cfhttp( url=variables.noServer, method="POST", result="local.res", timeout=5, throwOnError=false ) {
					cfhttpparam( type="body", value=toBinary( toBase64( "abcd" ) ) );
				}
				expect( res.request.params.body.type ).toBe( "binary" );
				expect( res.request.params.body.length ).toBe( 4 );
			});

			it( title="has the request headers that are sent, the masked ones are sent with their real value", skip=noHttpbin(), body=function() {
				cfhttp( url=httpbinURL( "anything" ), method="POST", result="local.res", timeout=10 ) {
					cfhttpparam( type="header", name="X-Ldev2542", value="visible" );
					cfhttpparam( type="header", name="X-Api-Token", value="abc123" );
					cfhttpparam( type="formfield", name="a", value="1" );
				}
				expect( res.status_code ).toBe( 200 );
				var sent = deserializeJSON( res.fileContent ).headers;
				expect( sent[ "X-Ldev2542" ] ).toBe( "visible" );
				expect( sent[ "X-Api-Token" ] ).toBe( "abc123" );
				expect( res.request.headers[ "X-Ldev2542" ] ).toBe( sent[ "X-Ldev2542" ] );
				expect( res.request.headers[ "X-Api-Token" ] ).toBe( "***" );
				expect( res.request.url ).toBe( httpbinURL( "anything" ) );
			});

			it( title="has the final url after a redirect that was followed in finalUrl, url is the requested url", skip=noHttpbin(), body=function() {
				var target = httpbinURL( "get" );
				cfhttp( url=httpbinURL( "redirect-to?url=#urlEncodedFormat( target )#&status_code=302" ), redirect=true, result="local.res", timeout=10 );
				expect( res.status_code ).toBe( 200 );
				expect( res.request.finalUrl ).toBe( target );
				expect( res.request.url ).notToBe( target );
				expect( res.request.url ).toInclude( "redirect-to" );
			});

			it( title="has the requested url as finalUrl when the redirect is not followed", skip=noHttpbin(), body=function() {
				var target = httpbinURL( "get" );
				cfhttp( url=httpbinURL( "redirect-to?url=#urlEncodedFormat( target )#&status_code=302" ), redirect=false, result="local.res", timeout=10 );
				expect( res.status_code ).toBe( 302 );
				expect( res.request.finalUrl ).toBe( res.request.url );
			});

		});
	}

}

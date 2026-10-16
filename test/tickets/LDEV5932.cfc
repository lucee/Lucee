component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.httpbin = server.getTestService( "httpbin" );
		variables.updateProvider = server.getTestService( "updateProvider" );
	}

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-5932 - CFHTTP GET compression", function() {

			it( title="Standard GET request should receive gzip compression from httpbin", body=function( currentSpec ) {
				if ( structCount( variables.httpbin ) == 0 ) {
					return;
				}

				cfhttp(
					url="http://#variables.httpbin.server#:#variables.httpbin.port#/gzip",
					method="GET",
					result="local.result",
					compression=true
				);

				expect( local.result.statusCode ).toBe( "200 OK" );

				// httpbin /gzip endpoint returns JSON with "gzipped": true when compression was used
				// Note: Content-Encoding header is removed by Apache HttpClient after automatic decompression
				var data = deserializeJSON( local.result.fileContent );
				expect( data ).toHaveKey( "gzipped" );
				expect( data.gzipped ).toBeTrue();
			});

			it( title="GET request with body parameter should still work (HttpGetWithBody)", body=function( currentSpec ) {
				if ( structCount( variables.httpbin ) == 0 ) {
					return;
				}

				cfhttp(
					url="http://#variables.httpbin.server#:#variables.httpbin.port#/anything",
					method="GET",
					result="local.result"
				) {
					cfhttpparam( type="body", value='{"test":"data"}' );
				}

				expect( local.result.statusCode ).toBe( "200 OK" );

				var data = deserializeJSON( local.result.fileContent );
				expect( data ).toHaveKey( "method" );
				expect( data.method ).toBe( "GET" );
			});

			it( title="HEAD request should receive gzip compression (existing behavior)", body=function( currentSpec ) {
				if ( structCount( variables.httpbin ) == 0 ) {
					return;
				}

				cfhttp(
					url="http://#variables.httpbin.server#:#variables.httpbin.port#/gzip",
					method="HEAD",
					result="local.result",
					compression=true
				);

				expect( local.result.statusCode ).toBe( "200 OK" );
				// HEAD requests don't have body, but should have Content-Encoding header
				expect( local.result.responseHeader ).toHaveKey( "Content-Encoding" );
			});

			it( title="DELETE request without body should use standard HttpDelete", body=function( currentSpec ) {
				if ( structCount( variables.httpbin ) == 0 ) {
					return;
				}

				cfhttp(
					url="http://#variables.httpbin.server#:#variables.httpbin.port#/delete",
					method="DELETE",
					result="local.result"
				);

				expect( local.result.statusCode ).toBe( "200 OK" );

				var data = deserializeJSON( local.result.fileContent );
				// Just verify we got a valid httpbin response (has headers key)
				expect( data ).toHaveKey( "headers" );
				expect( data ).toHaveKey( "url" );
			});

			it( title="DELETE request with body parameter should use HttpDeleteWithBody", body=function( currentSpec ) {
				if ( structCount( variables.httpbin ) == 0 ) {
					return;
				}

				cfhttp(
					url="http://#variables.httpbin.server#:#variables.httpbin.port#/delete",
					method="DELETE",
					result="local.result"
				) {
					cfhttpparam( type="body", value='{"test":"data"}' );
				}

				expect( local.result.statusCode ).toBe( "200 OK" );

				var data = deserializeJSON( local.result.fileContent );
				// Verify we got a valid httpbin response
				expect( data ).toHaveKey( "headers" );
				expect( data ).toHaveKey( "url" );
				// Verify body was sent - httpbin puts it in data key
				expect( data ).toHaveKey( "data" );
				expect( data.data ).toInclude( "test" );
			});

			it( title="Standard GET request should send proper Accept-Encoding header (echoGET test)", body=function( currentSpec ) {
				if ( structCount( variables.updateProvider ) == 0 ) {
					return;
				}

				// updateProvider (https://update.lucee.org / Cloudflare) is occasionally
				// unreachable from CI ("Connection Failure. Status code unavailable.").
				// Retry briefly, then skip rather than fail the suite on a network flake.
				// When a real 200 is returned, Accept-Encoding assertions stay strict.
				var result = {};
				var maxAttempts = 3;
				for ( var attempt = 1; attempt <= maxAttempts; attempt++ ) {
					cfhttp(
						url="#variables.updateProvider.url#/rest/update/provider/echoGet",
						method="GET",
						result="local.httpResult",
						compression=true
					);
					result = local.httpResult;
					if ( !findNoCase( "Connection Failure", result.statusCode ?: "" )
							&& !findNoCase( "Status code unavailable", result.statusCode ?: "" ) ) {
						break;
					}
					if ( attempt < maxAttempts ) {
						sleep( 500 * attempt );
					}
				}

				if ( findNoCase( "Connection Failure", result.statusCode ?: "" )
						|| findNoCase( "Status code unavailable", result.statusCode ?: "" ) ) {
					systemOutput(
						"LDEV-5932: skipping echoGET Accept-Encoding check - updateProvider unreachable: #result.statusCode ?: 'unknown'#",
						true
					);
					return;
				}

				expect( result.statusCode ).toBe( "200 OK" );

				var data = deserializeJSON( result.fileContent );

				// Verify Accept-Encoding header was sent (proves compression is being requested)
				expect( data ).toHaveKey( "httpRequestData" );
				expect( data.httpRequestData ).toHaveKey( "headers" );
				expect( data.httpRequestData.headers ).toHaveKey( "accept-encoding" );
				expect( data.httpRequestData.headers["accept-encoding"] ).toInclude( "gzip" );
			});

			xit( title="GET request with compression=false should NOT request gzip (DISABLED - Cloudflare modifies headers)", body=function( currentSpec ) {
				// Note: This test is disabled because Cloudflare adds 'br' to Accept-Encoding even when we don't send gzip
				// The updateProvider endpoint is behind Cloudflare which modifies the headers
				if ( structCount( variables.updateProvider ) == 0 ) {
					return;
				}

				cfhttp(
					url="#variables.updateProvider.url#/rest/update/provider/echoGet",
					method="GET",
					result="local.result",
					compression=false
				);

				expect( local.result.statusCode ).toBe( "200 OK" );
			});

			it( title="GET request with compression=false should NOT request gzip (httpbin test)", body=function( currentSpec ) {
				if ( structCount( variables.httpbin ) == 0 ) {
					return;
				}

				cfhttp(
					url="http://#variables.httpbin.server#:#variables.httpbin.port#/headers",
					method="GET",
					result="local.result",
					compression=false
				);

				expect( local.result.statusCode ).toBe( "200 OK" );

				var data = deserializeJSON( local.result.fileContent );

				// When compression=false, Accept-Encoding should be "deflate;q=0" (not gzip)
				expect( data ).toHaveKey( "headers" );
				expect( data.headers ).toHaveKey( "Accept-Encoding" );
				expect( data.headers["Accept-Encoding"] ).notToInclude( "gzip" );
				expect( data.headers["Accept-Encoding"] ).toInclude( "deflate" );
			});

		});
	}

}

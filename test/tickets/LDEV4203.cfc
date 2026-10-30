component extends="org.lucee.cfml.test.LuceeTestCase" labels="request" {

	function beforeAll() {
		variables.template = createURI( "LDEV4203/dump.cfm" );
		// arabic letters, they exist in ISO-8859-6 but not in ISO-8859-1
		variables.arabic = chr( 1605 ) & chr( 1585 ) & chr( 1581 ) & chr( 1576 ) & chr( 1575 );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-4203 _InternalRequest() passes the charset on to the request", function() {

			it( title="request character encoding is the charset", body=function( currentSpec ) {
				var r = doRequest( "ISO-8859-6", "GET" );
				expect( r.requestEncoding ).toBe( "ISO-8859-6" );
			});

			it( title="url values are decoded with the charset", body=function( currentSpec ) {
				var r = doRequest( "ISO-8859-6", "GET" );
				expect( r.urlValue ).toBe( variables.arabic );
			});

			it( title="form values are passed on with the charset", body=function( currentSpec ) {
				var r = doRequest( "ISO-8859-6", "POST" );
				expect( r.formValue ).toBe( variables.arabic );
			});

			it( title="a string body is encoded with the charset and read back with it", body=function( currentSpec ) {
				var r = doRequest( "ISO-8859-6", "POST", variables.arabic );
				expect( r.body ).toBe( variables.arabic );
			});

			it( title="the charset of the Content-Type header wins for the body", body=function( currentSpec ) {
				var r = doRequest( "ISO-8859-6", "POST", variables.arabic, { "Content-Type": "text/plain; charset=utf-8" } );
				expect( r.requestEncoding ).toBe( "utf-8" );
				expect( r.body ).toBe( variables.arabic );
			});

			it( title="without a charset the web charset is used", body=function( currentSpec ) {
				var r = doRequest( "", "POST", variables.arabic );
				expect( r.requestEncoding ).toBe( "UTF-8" );
				expect( r.urlValue ).toBe( variables.arabic );
				expect( r.formValue ).toBe( variables.arabic );
				expect( r.body ).toBe( variables.arabic );
			});

		});
	}

	private struct function doRequest( required string charset, required string method, string body="", struct headers={} ) {
		var res = _internalRequest(
			template=variables.template,
			method=arguments.method,
			urls={ u: variables.arabic },
			forms={ f: variables.arabic },
			headers=arguments.headers,
			body=arguments.body,
			charset=arguments.charset
		);
		expect( res.status ).toBe( 200, res.fileContent );
		return deserializeJSON( res.fileContent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}

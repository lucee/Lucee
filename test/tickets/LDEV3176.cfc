component extends="org.lucee.cfml.test.LuceeTestCase" labels="request" {

	function beforeAll() {
		variables.template = createURI( "LDEV3176/body.cfm" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3176 getHTTPRequestData() reads the request body only once", function() {

			it( title="text body is returned as the same String instance for every call", body=function( currentSpec ) {
				var r = post( "text/plain; charset=utf-8", "hello body" );
				expect( r.isBinary ).toBeFalse();
				expect( r.content ).toBe( "hello body" );
				expect( r.sameValue ).toBeTrue();
				expect( r.sameInstance ).toBeTrue();
			});

			it( title="json body is returned as String and keeps its charset", body=function( currentSpec ) {
				var body = '{"name":"Zürich äöü"}';
				var r = post( "application/json; charset=utf-8", body );
				expect( r.isBinary ).toBeFalse();
				expect( r.content ).toBe( body );
				expect( r.sameInstance ).toBeTrue();
			});

			it( title="surrounding whitespace of a text body is still trimmed", body=function( currentSpec ) {
				var r = post( "text/plain; charset=utf-8", "  trim me " & chr( 10 ) );
				expect( r.content ).toBe( "trim me" );
				expect( r.sameInstance ).toBeTrue();
			});

			it( title="binary body is returned as the same binary for every call", body=function( currentSpec ) {
				var body = repeatString( "binary-data-", 100 );
				var r = post( "application/octet-stream", body );
				expect( r.isBinary ).toBeTrue();
				expect( r.length ).toBe( len( body ) );
				expect( r.hash ).toBe( hash( body ) );
				expect( r.sameValue ).toBeTrue();
				expect( r.sameInstance ).toBeTrue();
			});

			it( title="large binary body (bigger than the in memory limit) is returned as the same binary for every call", body=function( currentSpec ) {
				var body = repeatString( "0123456789abcdef", 160000 ); // 2.5 MB
				var r = post( "application/octet-stream", body );
				expect( r.isBinary ).toBeTrue();
				expect( r.length ).toBe( len( body ) );
				expect( r.hash ).toBe( hash( body ) );
				expect( r.sameValue ).toBeTrue();
				expect( r.sameInstance ).toBeTrue();
			});

			it( title="large text body is returned as the same String for every call", body=function( currentSpec ) {
				var body = repeatString( "0123456789abcdef", 160000 );
				var r = post( "text/plain; charset=utf-8", body );
				expect( r.isBinary ).toBeFalse();
				expect( r.length ).toBe( len( body ) );
				expect( r.sameInstance ).toBeTrue();
			});

			it( title="empty body returns an empty string", body=function( currentSpec ) {
				var r = post( "text/plain; charset=utf-8", "" );
				expect( r.isBinary ).toBeFalse();
				expect( r.content ).toBe( "" );
				expect( r.sameValue ).toBeTrue();
			});

			it( title="includeBody=false never returns the body", body=function( currentSpec ) {
				var r = post( "text/plain; charset=utf-8", "hello body" );
				expect( r.hasContentWithoutBody ).toBeFalse();
				expect( r.content ).toBe( "hello body" );
			});

			it( title="form post: the form scope and the body stay the same for every call", body=function( currentSpec ) {
				var r = _internalRequest( template=variables.template, method="POST", forms={ a: "form-value" } );
				var data = deserializeJSON( r.fileContent.trim() );
				expect( data.formA ).toBe( "form-value" );
				expect( data.isBinary ).toBeFalse();
				expect( data.sameValue ).toBeTrue();
				expect( data.sameInstance ).toBeTrue();
				expect( data.hasContentWithoutBody ).toBeFalse();
			});

			it( title="form post: reading the form scope first does not change the body for every call", body=function( currentSpec ) {
				var r = _internalRequest( template=variables.template, method="POST", urls={ formFirst: true }, forms={ a: "form-value" } );
				var data = deserializeJSON( r.fileContent.trim() );
				expect( data.formA ).toBe( "form-value" );
				expect( data.sameValue ).toBeTrue();
				expect( data.sameInstance ).toBeTrue();
			});

			it( title="request without a body", body=function( currentSpec ) {
				var r = _internalRequest( template=variables.template, method="GET" );
				var data = deserializeJSON( r.fileContent.trim() );
				expect( data.content ).toBe( "" );
				expect( data.sameValue ).toBeTrue();
			});

		});
	}

	private struct function post( required string contentType, required string body ) {
		var r = _internalRequest(
			template=variables.template,
			method="POST",
			headers={ "content-type": arguments.contentType },
			body=arguments.body
		);
		expect( r.status ).toBe( 200, r.fileContent );
		return deserializeJSON( r.fileContent.trim() );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}
}

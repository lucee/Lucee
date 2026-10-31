component extends="org.lucee.cfml.test.LuceeTestCase" labels="cookie" {

	function run( testResults, testBox ) {
		describe( "LDEV-5287 only the last Set-Cookie header per cookie is sent", function() {

			it( title="setting the same cookie several times sends one header", body=function( currentSpec ) {
				var result = _request( "sameCookie.cfm" );
				var headers = _setCookies( result );
				expect( arrayLen( headers ) ).toBe( 1, serializeJson( headers ) );
				expect( _nameValue( headers[ 1 ] ) ).toBe( "LDEV5287=3" );
				expect( result.cookies.ldev5287 ).toBe( 3 );
			});

			it( title="distinct cookies are all sent in the order they were first set", body=function( currentSpec ) {
				var result = _request( "order.cfm" );
				var headers = _setCookies( result );
				expect( arrayLen( headers ) ).toBe( 3, serializeJson( headers ) );
				expect( _nameValue( headers[ 1 ] ) ).toBe( "A=a2" );
				expect( _nameValue( headers[ 2 ] ) ).toBe( "B=b2" );
				expect( _nameValue( headers[ 3 ] ) ).toBe( "C=c1" );
			});

			it( title="a cookie is identified by name, path and domain", body=function( currentSpec ) {
				var result = _request( "pathDomain.cfm" );
				var headers = _setCookies( result );
				// p on / and on /foo, d on example.com, on other.example and without domain
				expect( arrayLen( headers ) ).toBe( 5, serializeJson( headers ) );

				var p = _filter( headers, "P=" );
				expect( arrayLen( p ) ).toBe( 2, serializeJson( p ) );
				expect( p[ 1 ] ).toInclude( "P=slash2" );
				expect( p[ 1 ] ).toInclude( "Path=/" );
				expect( p[ 1 ] ).notToInclude( "Path=/foo" );
				expect( p[ 2 ] ).toInclude( "P=foo2" );
				expect( p[ 2 ] ).toInclude( "Path=/foo" );

				var d = _filter( headers, "D=" );
				expect( arrayLen( d ) ).toBe( 3, serializeJson( d ) );
				// ".Example.com" replaces "example.com", the leading dot and the case do not matter
				expect( d[ 1 ] ).toInclude( "D=host2" );
				expect( d[ 2 ] ).toInclude( "D=other" );
				expect( d[ 3 ] ).toInclude( "D=nodomain" );
				expect( d[ 3 ] ).notToInclude( "Domain=" );
			});

			it( title="cfcookie and the cookie scope set the same cookie", body=function( currentSpec ) {
				var result = _request( "tag.cfm" );
				var headers = _setCookies( result );
				expect( arrayLen( headers ) ).toBe( 1, serializeJson( headers ) );
				expect( _nameValue( headers[ 1 ] ) ).toBe( "TAGGED=3" );
				// the last value was set without httponly
				expect( headers[ 1 ] ).notToInclude( "HttpOnly" );
			});

			it( title="cookies set before a cfflush are sent, once", body=function( currentSpec ) {
				var result = _request( "flush.cfm" );
				var headers = _setCookies( result );
				expect( trim( result.fileContent ) ).toBe( "firstsecond" );
				expect( arrayLen( headers ) ).toBe( 2, serializeJson( headers ) );
				expect( _nameValue( headers[ 1 ] ) ).toBe( "BEFORE=2" );
				expect( _nameValue( headers[ 2 ] ) ).toBe( "OTHER=x" );
			});

			it( title="cookies set with cfheader are kept, except a cookie set again with the cookie scope", body=function( currentSpec ) {
				var result = _request( "cfheader.cfm" );
				var headers = _setCookies( result );
				expect( arrayLen( headers ) ).toBe( 3, serializeJson( headers ) );
				expect( _nameValue( headers[ 1 ] ) ).toBe( "raw=1" );
				expect( _nameValue( headers[ 2 ] ) ).toBe( "N=2" );
				expect( _nameValue( headers[ 3 ] ) ).toBe( "M=3" );
			});

		});
	}

	private struct function _request( required string template ) {
		return _InternalRequest( template: "#createURI( "LDEV5287" )#/#arguments.template#" );
	}

	private array function _setCookies( required struct result ) {
		var headers = arguments.result.headers[ "Set-Cookie" ] ?: [];
		return isSimpleValue( headers ) ? [ headers ] : headers;
	}

	private string function _nameValue( required string header ) {
		return listFirst( arguments.header, ";" );
	}

	private array function _filter( required array headers, required string prefix ) {
		var matches = [];
		for ( var header in arguments.headers ) {
			if ( left( header, len( arguments.prefix ) ) == arguments.prefix ) arrayAppend( matches, header );
		}
		return matches;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}

component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function run( testResults, testBox ) {
		describe( "LDEV-2250 cfhttpparam type=formfield encoded attribute", function() {

			it( title="encoded=false sends the form field value unchanged", body=function( currentSpec ) {
				var body = postFormField( "https://docs.lucee.org", false );
				expect( body ).toBe( "lucee=https://docs.lucee.org" );
			});

			it( title="encoded=true encodes the form field value", body=function( currentSpec ) {
				var body = postFormField( "https://docs.lucee.org", true );
				expect( body ).toBe( "lucee=https%3A%2F%2Fdocs.lucee.org" );
			});

			it( title="default (no encoded attribute) encodes the form field value", body=function( currentSpec ) {
				var body = postFormField( "https://docs.lucee.org" );
				expect( body ).toBe( "lucee=https%3A%2F%2Fdocs.lucee.org" );
			});

			it( title="encoded=false only affects the field it is set on", body=function( currentSpec ) {
				var body = postFormFields( [
					{ name: "a", value: "x/y z", encoded: false },
					{ name: "b", value: "x/y z", encoded: true }
				] );
				expect( body ).toBe( "a=x/y z&b=x%2Fy+z" );
			});

		});
	}

	private string function postFormField( required string value, boolean encoded ) {
		var field = { name: "lucee", value: arguments.value };
		if ( !isNull( arguments.encoded ) ) field.encoded = arguments.encoded;
		return postFormFields( [ field ] );
	}

	/**
	* starts a minimal http server on a free local port, posts the given form fields to it
	* and returns the raw body of the request the server received
	*/
	private string function postFormFields( required array fields ) {
		var ss = createObject( "java", "java.net.ServerSocket" ).init( 0 );
		ss.setSoTimeout( 20000 );
		var port = ss.getLocalPort();
		var result = { body: "" };
		try {
			thread name="ldev2250_#createUniqueId()#" ss=ss result=result {
				var sock = attributes.ss.accept();
				try {
					sock.setSoTimeout( 20000 );
					var is = sock.getInputStream();
					var head = "";
					while ( !head.endsWith( chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) ) ) {
						var b = is.read();
						if ( b == -1 ) break;
						head &= chr( b );
					}
					var len = 0;
					loop list=head delimiters=chr( 10 ) item="local.line" {
						if ( reFindNoCase( "^content-length:", trim( line ) ) ) len = val( listLast( line, ":" ) );
					}
					var bytes = [];
					loop times=len {
						arrayAppend( bytes, is.read() );
					}
					var sb = "";
					loop array=bytes item="local.c" {
						sb &= chr( c );
					}
					attributes.result.body = sb;
					var os = sock.getOutputStream();
					os.write( javacast( "byte[]", toBinary( toBase64( "HTTP/1.1 200 OK" & chr( 13 ) & chr( 10 ) & "Content-Length: 0" & chr( 13 ) & chr( 10 ) & "Connection: close" & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) ) ) ) );
					os.flush();
				}
				finally {
					sock.close();
				}
			}
			cfhttp( method="POST", url="http://127.0.0.1:#port#/", timeout=20 ) {
				loop array=arguments.fields item="local.f" {
					if ( structKeyExists( f, "encoded" ) ) cfhttpparam( name=f.name, type="formfield", value=f.value, encoded=f.encoded );
					else cfhttpparam( name=f.name, type="formfield", value=f.value );
				}
			}
			threadJoin( "", 30000 );
		}
		finally {
			ss.close();
		}
		return result.body;
	}

}

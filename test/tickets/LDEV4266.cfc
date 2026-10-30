component extends="org.lucee.cfml.test.LuceeTestCase" labels="hash" {

	function run( testResults, testBox ) {
		describe( title="LDEV-4266 hash() outputEncoding", body=function() {

			var str = "This is a string to be encoded.";
			var hex = "4703F1F78E75A3C0497DA868AB75854D5FDF881CBFF943FC28749A244729456A";
			var b64 = "RwPx9451o8BJfahoq3WFTV/fiBy/+UP8KHSaJEcpRWo=";
			var b64url = "RwPx9451o8BJfahoq3WFTV_fiBy_-UP8KHSaJEcpRWo";

			it( title="default output is still upper case hex", body=function() {
				expect( hash( "foo" ) ).toBe( "ACBD18DB4CC2F85CEDEF654FCCC4A4D8" );
				expect( hash( input="foo", outputEncoding="hex" ) ).toBe( "ACBD18DB4CC2F85CEDEF654FCCC4A4D8" );
				expect( hash( str, "SHA-256", "UTF-8" ) ).toBe( hex );
			});

			it( title="outputEncoding hex", body=function() {
				expect( hash( string=str, algorithm="SHA-256", encoding="UTF-8", outputEncoding="hex" ) ).toBe( hex );
			});

			it( title="outputEncoding base64 (named arguments)", body=function() {
				expect( hash( input="foo", outputEncoding="base64" ) ).toBe( "rL0Y20zC+Fzt72VPzMSk2A==" );
				expect( hash( string=str, algorithm="SHA-256", encoding="UTF-8", outputEncoding="base64" ) ).toBe( b64 );
			});

			it( title="outputEncoding base64url (named arguments)", body=function() {
				expect( hash( string=str, algorithm="SHA-256", encoding="UTF-8", outputEncoding="base64url" ) ).toBe( b64url );
			});

			it( title="outputEncoding is case insensitive", body=function() {
				expect( hash( input=str, algorithm="SHA-256", encoding="UTF-8", outputEncoding="BASE64" ) ).toBe( b64 );
				expect( hash( input=str, algorithm="SHA-256", encoding="UTF-8", outputEncoding="Base64Url" ) ).toBe( b64url );
			});

			it( title="outputEncoding as 4th positional argument like Adobe ColdFusion", body=function() {
				expect( hash( str, "SHA-256", "UTF-8", "base64" ) ).toBe( b64 );
				expect( hash( str, "SHA-256", "UTF-8", "base64url" ) ).toBe( b64url );
				expect( hash( str, "SHA-256", "UTF-8", "hex" ) ).toBe( hex );
			});

			it( title="iterations as 5th positional argument like Adobe ColdFusion", body=function() {
				var expected = hash( str, "SHA-256", "UTF-8", 3, "base64" );
				expect( hash( str, "SHA-256", "UTF-8", "base64", 3 ) ).toBe( expected );
				expect( hash( input=str, algorithm="SHA-256", encoding="UTF-8", outputEncoding="base64", iterations=3 ) ).toBe( expected );
				expect( expected ).notToBe( b64 );
			});

			it( title="numIterations as 4th positional argument still works", body=function() {
				var h = hash( str, "SHA-256", "UTF-8", 3 );
				expect( h ).toBe( hash( str, "SHA-256", "UTF-8", "3" ) );
				expect( h ).toBe( hash( input=str, algorithm="SHA-256", encoding="UTF-8", numIterations=3 ) );
				expect( h ).notToBe( hex );
				var once = hash( binaryDecode( hex, "hex" ), "SHA-256" );
				expect( hash( str, "SHA-256", "UTF-8", 2 ) ).toBe( once );
			});

			it( title="base64 output decodes to the same bytes as the hex output", body=function() {
				expect( binaryEncode( binaryDecode( hash( str, "SHA-512", "UTF-8", "base64" ), "base64" ), "hex" ) ).toBe( hash( str, "SHA-512", "UTF-8" ) );
				expect( binaryEncode( binaryDecode( hash( "foo", "MD5", "UTF-8", "base64" ), "base64" ), "hex" ) ).toBe( hash( "foo" ) );
			});

			it( title="byte array input with outputEncoding", body=function() {
				expect( hash( charsetDecode( str, "UTF-8" ), "SHA-256", "UTF-8", "base64" ) ).toBe( b64 );
			});

			it( title="member function with outputEncoding", body=function() {
				expect( str.hash( "SHA-256", "UTF-8", "base64" ) ).toBe( b64 );
				expect( str.hash( algorithm="SHA-256", outputEncoding="base64url" ) ).toBe( b64url );
			});

			it( title="invalid outputEncoding throws", body=function() {
				expect( function() {
					hash( input=str, outputEncoding="hexx" );
				}).toThrow( type="expression", regex="outputEncoding" );
			});

			it( title="algorithm quick keeps working and only supports hex", body=function() {
				expect( hash( str, "quick" ) ).toBe( hash( str, "quick", "UTF-8", "hex" ) );
				expect( hash( str, "quick" ) ).toBe( hash( input=str, algorithm="quick", outputEncoding="hex" ) );
				expect( function() {
					hash( input=str, algorithm="quick", outputEncoding="base64" );
				}).toThrow( type="expression", regex="quick" );
			});

		});
	}

}

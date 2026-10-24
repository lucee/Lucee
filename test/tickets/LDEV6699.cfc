component extends="org.lucee.cfml.test.LuceeTestCase" labels="guard" {
	function run( testResults , testBox ) {
		describe( title='Testcase for LDEV-6699: decodeFromURL() with a % that is not a valid escape' , body=function() {

			it( title='a trailing % is kept as is', body=function() {
				expect( decodeFromURL( "100%" ) ).toBe( "100%" );
				expect( guardDecode( "url", "100%" ) ).toBe( "100%" );
			});

			it( title='a % followed by non hex characters is kept as is', body=function() {
				expect( decodeFromURL( "50% off" ) ).toBe( "50% off" );
				expect( decodeFromURL( "(80 - 100%).pdf" ) ).toBe( "(80 - 100%).pdf" );
				expect( decodeFromURL( "a%zzb" ) ).toBe( "a%zzb" );
				expect( decodeFromURL( "a%4" ) ).toBe( "a%4" );
			});

			it( title='valid escapes next to a bare % are still decoded', body=function() {
				expect( decodeFromURL( "%3Cb%3E 100%" ) ).toBe( "<b> 100%" );
				expect( decodeFromURL( "%E2%82%AC 5%" ) ).toBe( "#chr( 8364 )# 5%" );
				expect( decodeFromURL( "100%25" ) ).toBe( "100%" );
			});

			it( title='+ is decoded to a space, an encoded + stays a +', body=function() {
				expect( decodeFromURL( "a+b" ) ).toBe( "a b" );
				expect( decodeFromURL( "1%2B1" ) ).toBe( "1+1" );
				expect( decodeFromURL( "50%+off" ) ).toBe( "50% off" );
			});

			it( title='decodeFromURL() is the inverse of encodeForURL()', body=function() {
				var str = "a b+c&d=e/100%";
				expect( decodeFromURL( encodeForURL( str ) ) ).toBe( str );
			});
		});
	}
}

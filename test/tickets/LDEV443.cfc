component extends="org.lucee.cfml.test.LuceeTestCase" labels="component" {

	function run( testResults, testBox ) {
		describe( "LDEV-443 javadoc style comment, tag value on multiple lines", function() {

			it( title="the function hint only contains the description", body=function( currentSpec ) {
				var md = getMetadata( new LDEV443.Multiline().shutdown );
				expect( trim( md.hint ) ).toBe( "Shutdown the client" );
			});

			it( title="a following line is part of the argument hint", body=function( currentSpec ) {
				var md = getMetadata( new LDEV443.Multiline().shutdown );
				expect( md.parameters[ 1 ].hint ).toBe( "The timeout in seconds, we default to 10 seconds" & chr( 10 ) & "so that we can do stuff" );
			});

			it( title="a following line is part of the return value", body=function( currentSpec ) {
				var md = getMetadata( new LDEV443.Multiline().shutdown );
				expect( md.return ).toBe( 'A reference to "this" CFC' & chr( 10 ) & "so that it can work." );
			});

			it( title="single line tags are not changed", body=function( currentSpec ) {
				var md = getMetadata( new LDEV443.Multiline().single );
				expect( trim( md.hint ) ).toBe( "Single line hint" );
				expect( md.parameters[ 1 ].hint ).toBe( "The timeout" );
				expect( md.return ).toBe( "The result" );
			});

			it( title="description over multiple lines stays the function hint", body=function( currentSpec ) {
				var md = getMetadata( new LDEV443.Multiline().description );
				expect( md.hint ).toInclude( "First line" );
				expect( md.hint ).toInclude( "second line of the function hint" );
				expect( md.return ).toBe( "Something" );
			});

		});
	}

}

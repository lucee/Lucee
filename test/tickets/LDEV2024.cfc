component extends="org.lucee.cfml.test.LuceeTestCase" labels="list" {
	function run( testResults, testBox ) {
		describe( "LDEV-2024 listAppend() with a multi-character delimiter", function() {

			it( title="multiCharacterDelimiter=true uses the whole delimiter", body=function( currentSpec ) {
				expect( listAppend( "1, 2", 3, ", ", true, true ) ).toBe( "1, 2, 3" );
				expect( listAppend( "1 | 2", 3, " | ", true, true ) ).toBe( "1 | 2 | 3" );
				expect( listAppend( "1delim2", 3, "delim", true, true ) ).toBe( "1delim2delim3" );
				expect( listAppend( "", 3, ", ", true, true ) ).toBe( "3" );
			});

			it( title="named arguments and member function", body=function( currentSpec ) {
				expect( listAppend( list="a::b", value="c", delimiter="::", multiCharacterDelimiter=true ) ).toBe( "a::b::c" );
				expect( "a::b".listAppend( "c", "::", true, true ) ).toBe( "a::b::c" );
			});

			it( title="multiCharacterDelimiter=true with includeEmptyFields=false removes empty fields", body=function( currentSpec ) {
				expect( listAppend( "a::::b::", "c", "::", false, true ) ).toBe( "a::b::c" );
				expect( listAppend( "::", "::c::", "::", false, true ) ).toBe( "c" );
			});

			it( title="default (false) keeps using only the first character", body=function( currentSpec ) {
				expect( listAppend( "1,2", 3, ":;" ) ).toBe( "1,2:3" );
				expect( listAppend( "1,2", 3, ":;", true, false ) ).toBe( "1,2:3" );
				expect( listAppend( "1, 2", 3, ", " ) ).toBe( "1, 2,3" );
			});
		});
	}
}

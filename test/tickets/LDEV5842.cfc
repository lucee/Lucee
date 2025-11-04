component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function run( testResults, testBox ) {
		describe( "Test case for LDEV-5842: Handle malformed HTTP date headers with single-digit hours", function() {

			it( title="parseDateTime should handle RFC 1123 dates with proper two-digit hours", body=function( currentSpec ) {
				var dateStr = "Sat, 13 Sep 2025 09:29:31 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd", "UTC" ) ).toBe( "2025-09-13" );
				expect( hour( result, "UTC") ).toBe( 9 );
			});

			it( title="parseDateTime should handle malformed HTTP headers with single-digit hours", body=function( currentSpec ) {
				// Some HTTP servers return malformed Last-Modified headers with single-digit hours
				// Example: "Sat, 13 Sep 2025  9:29:31 GMT" (note the extra space before 9)
				var dateStr = "Sat, 13 Sep 2025  9:29:31 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd", "UTC" ) ).toBe( "2025-09-13" );
				expect( hour( result, "UTC" ) ).toBe( 9 );
				expect( minute( result, "UTC" ) ).toBe( 29 );
				expect( second( result, "UTC" ) ).toBe( 31 );
			});

			it( title="parseDateTime should handle single-digit hours without extra space", body=function( currentSpec ) {
				var dateStr = "Fri, 05 Jan 2024 8:15:00 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd", "UTC" ) ).toBe( "2024-01-05" );
				expect( hour( result, "UTC" ) ).toBe( 8 );
			});

			it( title="parseDateTime should handle standard RFC 1123 dates", body=function( currentSpec ) {
				var dateStr = "Thu, 20 Jun 2024 20:35:36 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd", "UTC" ) ).toBe( "2024-06-20" );
				expect( hour( result, "UTC" ) ).toBe( 20 );
			});

			it( title="parseDateTime should handle single-digit hours from Maven metadata", body=function( currentSpec ) {
				// Maven metadata files sometimes generate dates without leading zeros
				var dateStr = "Mon, 03 Mar 2025 7:45:12 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd", "UTC" ) ).toBe( "2025-03-03" );
				expect( hour( result, "UTC" ) ).toBe( 7 );
				expect( minute( result, "UTC" ) ).toBe( 45 );
				expect( second( result, "UTC" ) ).toBe( 12 );
			});

			it( title="parseDateTime should handle dates with Unicode non-breaking space (U+00A0)", body=function( currentSpec ) {
				// Some systems use non-breaking spaces instead of regular spaces
				// The character between "2025" and "6:30:00" is U+00A0 (non-breaking space)
				var dateStr = "Tue, 15 Apr 2025" & chr(160) & "6:30:00 GMT";
				var result = parseDateTime( dateStr );

				expect( isDate( result ) ).toBeTrue();
				expect( dateFormat( result, "yyyy-mm-dd", "UTC" ) ).toBe( "2025-04-15" );
				expect( hour( result, "UTC" ) ).toBe( 6 );
				expect( minute( result, "UTC" ) ).toBe( 30 );
				expect( second( result, "UTC" ) ).toBe( 0 );
			});

		});
	}
}
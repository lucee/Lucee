component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function beforeAll() {
		variables.testDate = now();
		variables.originalLocale = getLocale();
		setLocale( "en_US" );
	}

	function afterAll() {
		setLocale( variables.originalLocale );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5890: dateTimeFormat with 'long' mask", function() {

			it( title="should parse formatted date with original whitespace (U+202F)", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "long" );
				expect( formatted ).toBeDate();
			} );

			it( title="should parse formatted date with U+202F replaced by regular space", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "long" );
				var withSpace = replace( formatted, chr( 8239 ), " ", "all" );
				expect( withSpace ).toBeDate();
			} );

		} );

		describe( "LDEV-5890: dateTimeFormat with 'medium' mask", function() {

			it( title="should parse formatted date with original whitespace (U+202F)", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "medium" );
				expect( formatted ).toBeDate();
			} );

			it( title="should parse formatted date with U+202F replaced by regular space", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "medium" );
				var withSpace = replace( formatted, chr( 8239 ), " ", "all" );
				expect( withSpace ).toBeDate();
			} );

		} );

		describe( "LDEV-5890: dateTimeFormat with 'short' mask", function() {

			it( title="should parse formatted date with original whitespace (U+202F)", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "short" );
				expect( formatted ).toBeDate();
			} );

			it( title="should parse formatted date with U+202F replaced by regular space", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "short" );
				var withSpace = replace( formatted, chr( 8239 ), " ", "all" );
				expect( withSpace ).toBeDate();
			} );

		} );

		describe( "LDEV-5890: dateTimeFormat with 'full' mask", function() {

			it( title="should parse formatted date with original whitespace (U+202F)", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "full" );
				expect( formatted ).toBeDate();
			} );

			it( title="should parse formatted date with U+202F replaced by regular space", body=function( currentSpec ) {
				var formatted = dateTimeFormat( variables.testDate, "full" );
				var withSpace = replace( formatted, chr( 8239 ), " ", "all" );
				expect( withSpace ).toBeDate();
			} );

		} );

			// dateDiff() cases based on #2695 (MitrahSoft); a single captured date avoids flakiness when now() crosses a minute
		describe( "LDEV-5890: dateDiff with dateTimeFormat / lsDateTimeFormat masks", function() {

			it( title="dateDiff() works with dateTimeFormat() long, medium and short masks", body=function( currentSpec ) {
				loop list="long,medium,short" item="local.mask" {
					var formatted = dateTimeFormat( variables.testDate, mask );
					expect( dateDiff( "s", formatted, formatted ) ).toBe( 0, mask );
				}
			} );

			it( title="dateDiff() works with lsDateTimeFormat() long, medium and short masks", body=function( currentSpec ) {
				loop list="long,medium,short" item="local.mask" {
					var formatted = lsDateTimeFormat( variables.testDate, mask );
					expect( dateDiff( "s", formatted, formatted ) ).toBe( 0, mask );
				}
			} );

			it( title="dateDiff() works with equivalent custom masks", body=function( currentSpec ) {
				loop list="mmmm d, yyyy h:nn:ss tt zzz|mmm d, yyyy h:nn:ss tt|m/d/y h:nn tt" delimiters="|" item="local.mask" {
					var formatted = dateTimeFormat( variables.testDate, mask );
					expect( dateDiff( "s", formatted, formatted ) ).toBe( 0, mask );
					formatted = lsDateTimeFormat( variables.testDate, mask );
					expect( dateDiff( "s", formatted, formatted ) ).toBe( 0, mask );
				}
			} );

		} );

	}

}

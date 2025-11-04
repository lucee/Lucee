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

	}

}

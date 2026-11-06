component extends="org.lucee.cfml.test.LuceeTestCase" labels="date" {

	function run( testResults, testBox ) {
		describe( "LDEV-1653: lsParseDateTime() accepts nn (CFML) for the minutes of a mask", function() {

			it( title="mask with nn for the minutes", body=function( currentSpec ) {
				var result = lsParseDateTime( "2018-01-04-23.40.56", "en_US", "yyyy-MM-dd-HH.nn.ss" );
				expect( toString( result ) ).toBe( "{ts '2018-01-04 23:40:56'}" );
			});

			it( title="mask with mm for the minutes is unchanged", body=function( currentSpec ) {
				var result = lsParseDateTime( "2018-01-04-23.40.56", "en_US", "yyyy-MM-dd-HH.mm.ss" );
				expect( toString( result ) ).toBe( "{ts '2018-01-04 23:40:56'}" );
			});

			it( title="nn and mm give the same result", body=function( currentSpec ) {
				var withNN = lsParseDateTime( "2018-01-04-23.40.56", "en_US", "yyyy-MM-dd-HH.nn.ss" );
				var withMM = lsParseDateTime( "2018-01-04-23.40.56", "en_US", "yyyy-MM-dd-HH.mm.ss" );
				expect( dateCompare( withNN, withMM ) ).toBe( 0 );
			});

			it( title="mask with nn and a timezone", body=function( currentSpec ) {
				var result = lsParseDateTime( "2018-01-04-23.40.56", "en_US", "UTC", "yyyy-MM-dd-HH.nn.ss" );
				var expected = lsParseDateTime( "2018-01-04-23.40.56", "en_US", "UTC", "yyyy-MM-dd-HH.mm.ss" );
				expect( dateCompare( result, expected ) ).toBe( 0 );
				expect( minute( result, "UTC" ) ).toBe( 40 );
				expect( hour( result, "UTC" ) ).toBe( 23 );
			});

			it( title="mask with a single n for the minutes", body=function( currentSpec ) {
				var result = lsParseDateTime( "2018-01-04-23.5.56", "en_US", "yyyy-MM-dd-HH.n.ss" );
				expect( toString( result ) ).toBe( "{ts '2018-01-04 23:05:56'}" );
			});

			it( title="a quoted nn in the mask is a literal", body=function( currentSpec ) {
				var result = lsParseDateTime( "2018-01-04 nn 23:40:56", "en_US", "yyyy-MM-dd 'nn' HH:mm:ss" );
				expect( toString( result ) ).toBe( "{ts '2018-01-04 23:40:56'}" );
			});

			it( title="a quoted nn next to a minutes nn", body=function( currentSpec ) {
				var result = lsParseDateTime( "2018-01-04 nn 23:40:56", "en_US", "yyyy-MM-dd 'nn' HH:nn:ss" );
				expect( toString( result ) ).toBe( "{ts '2018-01-04 23:40:56'}" );
			});

			it( title="parseDateTime() with a mask with nn", body=function( currentSpec ) {
				var result = parseDateTime( "2018-01-04-23.40.56", "yyyy-MM-dd-HH.nn.ss" );
				expect( toString( result ) ).toBe( "{ts '2018-01-04 23:40:56'}" );
			});

			it( title="a wrong date still throws", body=function( currentSpec ) {
				expect( function() {
					lsParseDateTime( "not a date", "en_US", "yyyy-MM-dd-HH.nn.ss" );
				}).toThrow();
			});

		});
	}

}

component extends="org.lucee.cfml.test.LuceeTestCase" labels="timespan" {

	function run( testResults, testBox ) {
		describe( "LDEV-3840 createTimeSpan() is numeric", function() {

			it( title="isNumeric( createTimeSpan() )", body=function( currentSpec ) {
				var ts = createTimeSpan( 1, 2, 3, 4 );
				expect( isSimpleValue( ts ) ).toBeTrue();
				expect( isNumeric( ts ) ).toBeTrue();
				expect( isNumeric( createTimeSpan( 0, 0, 0, 0 ) ) ).toBeTrue();
				expect( isNumeric( createTimeSpan( 0, 0, 30, 0 ) ) ).toBeTrue();
			});

			it( title="the timespan value is the number of days", body=function( currentSpec ) {
				var ts = createTimeSpan( 1, 12, 0, 0 );
				expect( ts + 0 ).toBe( 1.5 );
				expect( isValid( "numeric", ts ) ).toBeTrue();
			});

			it( title="dates and other values are unchanged", body=function( currentSpec ) {
				expect( isNumeric( now() ) ).toBeFalse();
				expect( isNumeric( "abc" ) ).toBeFalse();
				expect( isNumeric( "1.5" ) ).toBeTrue();
				expect( isNumeric( {} ) ).toBeFalse();
			});
		});
	}
}

component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function beforeAll() {
		variables.helper = createObject( "java", "lucee.commons.date.TwoDigitYear" );
		variables.pivot = variables.helper.getPivot();
		variables.currentYear = year( now() );
		// the window java.text.SimpleDateFormat uses for two-digit years
		var sdfStart = createObject( "java", "java.text.SimpleDateFormat" ).init().get2DigitYearStart();
		variables.firstYear = year( sdfStart ) + 1;
	}

	private numeric function expectedYear( required numeric yy ) {
		if ( variables.pivot >= 0 ) return arguments.yy < variables.pivot ? 2000 + arguments.yy : 1900 + arguments.yy;
		var result = int( variables.firstYear / 100 ) * 100 + arguments.yy;
		if ( result < variables.firstYear ) result += 100;
		return result;
	}

	private array function samples() {
		var cy = variables.currentYear;
		return [ ( cy + 20 ) mod 100, ( cy - 79 ) mod 100, ( cy + 5 ) mod 100, ( cy - 50 ) mod 100, 0, 99 ];
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6180 - two-digit years", function() {

			it( title="the default follows the sliding window of java.text.SimpleDateFormat", body=function( currentSpec ) {
				if ( variables.pivot >= 0 ) return;
				var cy = variables.currentYear;
				expect( variables.helper.toYear( ( cy + 20 ) mod 100 ) ).toBe( cy + 20 );
				expect( variables.helper.toYear( ( cy + 21 ) mod 100 ) ).toBe( cy - 79 );
				expect( variables.helper.toYear( ( cy - 79 ) mod 100 ) ).toBe( cy - 79 );
				expect( variables.helper.toYear( cy mod 100 ) ).toBe( cy );
				expect( variables.helper.getYearsBefore() ).toBe( 80 );
			});

			it( title="all parse functions return the same year", body=function( currentSpec ) {
				loop array=samples() item="local.yy" {
					var y2 = numberFormat( yy, "00" );
					var exp = expectedYear( yy );
					var str = "6/15/" & y2;
					expect( year( parseDateTime( str ) ) ).toBe( exp, "parseDateTime [#str#]" );
					expect( year( lsParseDateTime( str, "en_US" ) ) ).toBe( exp, "lsParseDateTime [#str#]" );
					expect( year( parseDateTime( str, "M/d/yy" ) ) ).toBe( exp, "parseDateTime [#str#] with mask" );
					expect( year( lsParseDateTime( str, "en_US", "M/d/yy" ) ) ).toBe( exp, "lsParseDateTime [#str#] with mask" );
					expect( year( lsParseDateTime( "15.06." & y2, "de_CH" ) ) ).toBe( exp, "lsParseDateTime [15.06.#y2#] de_CH" );
					expect( year( str ) ).toBe( exp, "implicit cast [#str#]" );
					expect( year( createDate( yy, 6, 15 ) ) ).toBe( exp, "createDate(#yy#)" );
				}
			});

			it( title="four-digit years are not changed", body=function( currentSpec ) {
				expect( year( parseDateTime( "6/15/2099" ) ) ).toBe( 2099 );
				expect( year( parseDateTime( "6/15/1930", "M/d/yyyy" ) ) ).toBe( 1930 );
				expect( year( lsParseDateTime( "6/15/2030", "en_US" ) ) ).toBe( 2030 );
				expect( variables.helper.toYear( 2030 ) ).toBe( 2030 );
				expect( variables.helper.toYear( 130 ) ).toBe( 130 );
			});

			it( title="a fixed pivot maps years below it to 20xx and all others to 19xx", body=function( currentSpec ) {
				expect( variables.helper.toYear( 0, 30 ) ).toBe( 2000 );
				expect( variables.helper.toYear( 29, 30 ) ).toBe( 2029 );
				expect( variables.helper.toYear( 30, 30 ) ).toBe( 1930 );
				expect( variables.helper.toYear( 99, 30 ) ).toBe( 1999 );
				expect( variables.helper.toYear( 0, 0 ) ).toBe( 1900 );
				expect( variables.helper.toYear( 99, 100 ) ).toBe( 2099 );
				expect( variables.helper.toYear( 2030, 30 ) ).toBe( 2030 );
			});
		});
	}
}

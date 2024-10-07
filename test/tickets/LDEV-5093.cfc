component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){
		describe( "Test LDEV-5093", function(){
			it( "test EEE MMM dd yyyy HH:mm:ss 'GMT'ZZ (zzzz)", function(){
				var dateTime = "Mon Sep 23 2024 16:36:28 GMT+0530 (India Standard Time)";
				var result = isDate(ParseDateTime(dateTime));

				expect( result ).toBeTrue();
				expect( dateTimeFormat(dateTime,"EEE") ).toBe( "Mon" );
			});
		} );
	}
}

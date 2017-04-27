component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1185", function() {
			it( title='Checking isDate()', body=function( currentSpec ) {
				var result = isDate("32825-0002")
				expect(result).toBe(false);
			});

			it( title='Checking parseDateTime()', body=function( currentSpec ) {
				var result = "";

				try{
					result = parseDateTime("32825-0002");
				}catch(any e){
					result = "Not a valid date/time format";
				}

				expect(result).toBe("Not a valid date/time format");
			});
		});
	}
}
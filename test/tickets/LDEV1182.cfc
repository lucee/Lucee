component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1182", function() {
			it( title='Checking date difference for day', body=function( currentSpec ) {
				var date1 = CreateDate(2017, 1, 31);
				var date2 = date1.add("m", 3);
				var result = datediff("m", date1, date2);
				expect(result).toBe(3);
			});

			it( title='Checking date difference for month', body=function( currentSpec ) {
				var date1 = CreateDate(2017, 1, 31);
				var date2 = date1.add("d", 3);
				var result = datediff("d", date1, date2);
				expect(result).toBe(3);
			});

			it( title='Checking date difference for year', body=function( currentSpec ) {
				var date1 = CreateDate(2017, 1, 31);
				var date2 = date1.add("yyyy", 3);
				var result = datediff("yyyy", date1, date2);
				expect(result).toBe(3);
			});
		});
	}
}
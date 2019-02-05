component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2148", function() {
			it(title = "parseDateTime with various argument format", body = function( currentSpec ) {
				//These are passes as expected
				expect(parseDateTime( "2018-12-31","yyyy-mm-dd" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "2018-31-12","yyyy-dd-mm" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "12-2018-31","mm-yyyy-dd" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "12-31-2018","mm-dd-yyyy" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "31-2018-12","dd-yyyy-mm" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "31-12-2018","dd-mm-YYYY" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "10:00 31-12-2018","hh:mm dd-mm-YYYY" )).toBe("{ts '2018-12-31 10:00:00'}");

				//These belows are not passes & get fails

				expect(parseDateTime( "2018/12/31","yyyy/mm/DD" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "2018-12-31","yyyy-mm-DD" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "2018-31-12","yyyy-DD-mm" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "12-2018-31","mm-yyyy-DD" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "12-31-2018","mm-DD-yyyy" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "31-2018-12","DD-yyyy-mm" )).toBe("{ts '2018-12-31 00:00:00'}");
				expect(parseDateTime( "31-12-2018","DD-mm-yyyy" )).toBe("{ts '2018-12-31 00:00:00'}");

				expect(parseDateTime( "2018-12-31 10:00","yyyy-mm-dd hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "2018-12-31 10:00","yyyy-mm-DD hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "2018-31-12 10:00","yyyy-dd-mm hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "2018-31-12 10:00","yyyy-DD-mm hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "12-2018-31 10:00","mm-yyyy-dd hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "12-2018-31 10:00","mm-yyyy-DD hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "12-31-2018 10:00","mm-dd-yyyy hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "12-31-2018 10:00","mm-DD-yyyy hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "31-2018-12 10:00","DD-yyyy-mm hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "31-2018-12 10:00","dd-yyyy-mm hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "31-12-2018 10:00","DD-mm-yyyy hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				expect(parseDateTime( "31-12-2018 10:00","dd-mm-YYYY hh:mm" )).toBe("{ts '2018-12-31 10:00:00'}");
				
				expect(parseDateTime( "10:00 12-31-2018","hh:mm mm-DD-yyyy" )).toBe("{ts '2018-12-31 10:00:00'}");

			});

		});
	}
}
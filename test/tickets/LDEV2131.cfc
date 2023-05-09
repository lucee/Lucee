component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.tz = getTimeZone();
		setTimeZone("CET");
	}

	function afterAll() {
		setTimeZone(variables.tz);
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-2131", function() {
			it( title="dateTimeFormat with mask 'isoms' and aliases", body=function( currentSpec ) {
				expect(dateTimeFormat("2022/01/02 11:22:33.444", "isoMs")).toBe("2022-01-02T11:22:33.444+01:00");
				expect(dateTimeFormat("2022/01/02 11:22:33.444", "isoMillis")).toBe("2022-01-02T11:22:33.444+01:00");
				expect(dateTimeFormat("2022/01/02 11:22:33.444", "javascript")).toBe("2022-01-02T11:22:33.444+01:00");
			});

			it( title="parseDateTime with format 'isoms' and aliases", body=function( currentSpec ) {
				expect(toString(parseDateTime("2022-01-02T11:22:33.444+01:00", "isoMs"))).toBe("{ts '2022-01-02 11:22:33'}");
				expect(toString(parseDateTime("2022-01-02T11:22:33.444+01:00", "isoMillis"))).toBe("{ts '2022-01-02 11:22:33'}");
				expect(toString(parseDateTime("2022-01-02T11:22:33.444+01:00", "javascript"))).toBe("{ts '2022-01-02 11:22:33'}");
			});
		});
	}

}
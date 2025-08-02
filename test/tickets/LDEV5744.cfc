component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="LDEV-5744 dateparsing regressions", body=function() {

			var okDateFormats = [
				{"format": "ISO 8601","example":                                "2022-09-20T12:34:00-07:00"},
				{"format": "ISO 8601 with Z","example":                         "2022-09-20T12:34:00Z"},
				{"format": "ISO 8601 with milliseconds","example":              "2022-09-20T12:34:00.000-07:00"},
				{"format": "RFC 2822","example":                                "Tue, 20 Sep 2022 12:34:00 -0700"},
				{"format": "yyyy-mm-dd hh:mm:ss","example":                     "2022-09-20 12:34:00"},
				{"format": "yyyy-mm-dd hh:mm:ss.000","example":                 "2022-09-20 12:34:00.000"},
				{"format": "yyyy/mm/dd hh:mm:ss","example":                     "2022/09/20 12:34:00"},
				{"format": "yyyy/mm/dd  hh:mm:ss","example":                    "2022/09/20  12:34:00"},
				{"format": "mm-dd-yyyy hh:mm:ss","example":                     "09-20-2022 12:34 PM"},
				{"format": "mm/dd/yyyy hh:mm:ss","example":                     "09/20/2022 12:34 PM"},
				{"format": "short string","example":                            "9/20/22 12:34 PM"},
				{"format": "contains narrow no-break space","example":          "9/20/22 12:34#chr(8239)#PM"},
				{"format": "bash ""$(date)"" two-digit day","example":          "Tue Sep 20 12:34:00 PDT 2022"},
				{"format": "bash ""$(date)"" add leading zero","example":       "Mon Mar 03 03:09:07 PDT 2025"},
				{"format": "JavaScript: new Date()","example":                  "Tue Sep 20 2022 12:34:00 GMT-0700 (Pacific Daylight Time)"},
				{"format": "Long month name","example":                         "September 20, 2022 12:34 PM"},
				{"format": "JDBC/SQL Timestamp","example":                      "{ts '2022-09-20 12:34:00'}"},
				{"format": "bash ""$(date)"" add leading zero","example":       "Mon Mar  03 03:09:07 PDT 2025"},
				{"format": "bash ""$(date)"" two spaces before day","example":  "Mon Mar  3 03:09:07 PDT 2025"},
				{"format": "bash ""$(date)"" one digit day","example":          "Mon Mar 3 03:09:07 PDT 2025"}
			];

			var badDateFormats = [
				{"format": "contains comma","example":                          "9/20/22, 12:34 PM"},
				{"format": "Oracle","example":                                  "20-SEP-22 12.34.00.000000 PM"},
				{"format": "Syslog","example":                                  "Sep 20 12:34:00"},
				{"format": "Apache log","example":                              "[20/Sep/2022:12:34:00 -0700]"}
			];

			loop array="#okDateFormats#" value="local.test" {
				describe( title="using date #test.format#", body=function() {
					it(title="test date parsing (#test.example#)",
							data={ test=test },
							body=function( data ) {
						var date = parseDateTime( data.test.example );
						expect( isDate(date) ).toBeTrue();
					});
				});
			}

			loop array="#badDateFormats#" value="local.test" {
				describe( title="date #test.format#", body=function() {
					xit(title="test date parsing (#test.example#)",
							data={ test=test },
							body=function( data ) {
						var date = parseDateTime( data.test.example );
						expect( isDate( date ) ).toBeTrue();
					});
				});
			}

		});

	}
}
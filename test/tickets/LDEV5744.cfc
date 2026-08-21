component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.goodDateFormats = [
			{ format: "yyyy-mm-dd hh:mm:ss", example: "2022-09-20 12:34:00" },
			{ format: "yyyy-mm-dd hh:mm:ss.000", example: "2022-09-20 12:34:00.000" },
			{ format: "yyyy/mm/dd hh:mm:ss", example: "2022/09/20 12:34:00" },
			{ format: "mm-dd-yyyy hh:mm:ss", example: "09-20-2022 12:34 PM" },
			{ format: "mm/dd/yyyy hh:mm:ss", example: "09/20/2022 12:34 PM" },
			{ format: "short string", example: "9/20/22 12:34 PM" },
			{ format: "bash $(date) with leading zero", example: "Mon Mar 03 03:09:07 PDT 2025" },
			{ format: "Long month name", example: "September 20, 2022 12:34 PM" },
			{ format: "JDBC/SQL Timestamp", example: "{ts '2022-09-20 12:34:00'}" }
		];
	}

	function testBashDateSingleDigitDay() {
		var dateStr = "Mon Mar 3 03:09:07 PDT 2025";
		try {
			var result = parseDateTime(dateStr);
			assertTrue(isDate(result), "Should parse bash date format with single digit day: #dateStr#");
			assertTrue(month(result) == 3, "Month should be 3 (March)");
			assertTrue(day(result) == 3, "Day should be 3");
			assertTrue(year(result) == 2025, "Year should be 2025");
		} catch (any e) {
			fail("Failed to parse bash date format: #dateStr# - Error: #e.message#");
		}
	}

	function testBashDateDoubleDigitDay() {
		var dateStr = "Mon Mar 03 03:09:07 PDT 2025";
		try {
			var result = parseDateTime(dateStr);
			assertTrue(isDate(result), "Should parse bash date format with double digit day");
			assertTrue(month(result) == 3, "Month should be 3 (March)");
			assertTrue(day(result) == 3, "Day should be 3");
			assertTrue(year(result) == 2025, "Year should be 2025");
		} catch (any e) {
			fail("Failed to parse bash date format with double digit day - Error: #e.message#");
		}
	}

	function testLongMonthName() {
		var dateStr = "September 20, 2022 12:34 PM";
		try {
			var result = parseDateTime(dateStr);
			assertTrue(isDate(result), "Should parse long month name format");
			assertTrue(month(result) == 9, "Month should be 9 (September)");
			assertTrue(day(result) == 20, "Day should be 20");
			assertTrue(year(result) == 2022, "Year should be 2022");
		} catch (any e) {
			fail("Failed to parse long month name format - Error: #e.message#");
		}
	}

	function testJDBCTimestamp() {
		var dateStr = "{ts '2022-09-20 12:34:00'}";
		try {
			var result = parseDateTime(dateStr);
			assertTrue(isDate(result), "Should parse JDBC/SQL timestamp");
			assertTrue(month(result) == 9, "Month should be 9 (September)");
			assertTrue(day(result) == 20, "Day should be 20");
			assertTrue(year(result) == 2022, "Year should be 2022");
		} catch (any e) {
			fail("Failed to parse JDBC/SQL timestamp - Error: #e.message#");
		}
	}

	function testStandardDateFormats() {
		var testCases = [
			"2022-09-20 12:34:00",
			"09/20/2022 12:34 PM",
			"9/20/22 12:34 PM",
			"09-20-2022 12:34 PM"
		];

		for (var dateStr in testCases) {
			try {
				var result = parseDateTime(dateStr);
				assertTrue(isDate(result), "Should parse standard date format: #dateStr#");
				assertTrue(month(result) == 9, "Month should be 9 (September) for #dateStr#");
				assertTrue(day(result) == 20, "Day should be 20 for #dateStr#");
				assertTrue(year(result) == 2022, "Year should be 2022 for #dateStr#");
			} catch (any e) {
				fail("Failed to parse standard date format #dateStr# - Error: #e.message#");
			}
		}
	}

	function testDateTimeObject() {
		var result = parseDateTime("2022-09-20 12:34:00");
		assertTrue(isDate(result), "Result should be a date object");
		
		assertEquals(2022, year(result), "Year should be 2022");
		assertEquals(9, month(result), "Month should be 9");
		assertEquals(20, day(result), "Day should be 20");
		assertEquals(12, hour(result), "Hour should be 12");
		assertEquals(34, minute(result), "Minute should be 34");
		assertEquals(0, second(result), "Second should be 0");
	}
}

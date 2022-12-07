component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true" {
	function run( testResults , testBox ) {
		describe( title="Testcase for lsIsDate()", body=function() {
			it(title='Checking lsIsDate() function with locale "english (uk)"', body=function( currentSpec ) {
				assertEquals("true", lsIsDate("22/01/01", "english (uk)"));
				assertEquals("true", lsIsDate("31/12/1999", "english (uk)"));
				assertEquals("true", lsIsDate("31 December 1999", "english (uk)"));
				assertEquals("true", lsIsDate("25-march-2000", "english (uk)"));
				assertEquals("false", lsIsDate("12-12-2022", "english (uk)"));
				assertEquals("true", lsIsDate("12.12.2022", "english (uk)"));
				assertEquals("true", lsIsDate("2022-12-31", "english (uk)"));
				assertEquals("true", lsIsDate("12-12-22", "english (uk)"));
			});
		});
	}
}

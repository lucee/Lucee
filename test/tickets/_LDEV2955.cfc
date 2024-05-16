component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2955", function() {
			it(title = "Checked with isDate() function", body = function( currentSpec ) {
				expect(isdate("8.8")).toBe(false);
			    expect(isdate("10.8")).toBe(false);
			    expect(isdate("20-3")).toBe(false);
			    expect(isdate("8-8")).toBe(false);
			    expect(isdate("98-8")).toBe(true);
			    expect(isdate("1900.8")).toBe(true);
			});
		});
	}
}
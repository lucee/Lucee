component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Test case for LDEV-3056", function() {
			it( title = "Checked abs() with string value", body = function( currentSpec ){
			    teststrValue = "0.08263888888888889";
			    absValue = abs(teststrValue);
			    res = teststrValue eq abs(teststrValue);
			    expect(res).toBe(true);
			});
			it( title = "Checked abs() with numeric value", body = function( currentSpec ){
			    testnumValue = 0.08263888888888889;
			    absValue = abs(testnumValue);
			    res = testnumValue eq abs(testnumValue);
			    expect(res).toBe(true);
			});
		});
	}
}
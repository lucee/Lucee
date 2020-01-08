component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2629", function() {
			it(title = "Test with nullvalue & one more value", body = function( currentSpec ) {
				array = [1,nullvalue(),2];
				expect(",").toBe(array.tolist()[2]);
				expect("1").toBe(array.tolist()[1]);
				expect(",").toBe(array.tolist()[3]);
				expect("2").toBe(array.tolist()[4]);
			});

			it(title = "Test with nullvalue only", body = function( currentSpec ) {
				array = [nullvalue()];
				expect("").toBe(array.tolist());
			});
		});
	}
}
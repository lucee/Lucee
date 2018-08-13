component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test suite for deserializeJSON()", function() {
			it(title = "checking the deserializeJSON", body = function( currentSpec ) {
				var result="{'test':'case'}";
				expect(deserializeJSON(result).test).toBe('case');
			});
			it(title = "checking the deserializeJSON with member function", body = function( currentSpec ) {
				var result="{'test':'case'}";
				expect((result.deserializeJSON()).test).tobe("case");
			});
		});
	}
}
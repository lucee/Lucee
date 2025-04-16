component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for array() function", body = function() {
			it(title = "checking Array() function", body = function( currentSpec ) {
				var arr = array(1, 2, 3, "Earth");
				expect(arr[1]).toBe(1);
				expect(arr[4]).toBe("Earth");
			});
		});
	}
}

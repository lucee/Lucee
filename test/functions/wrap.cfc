 component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for wrap() function", body=function() {
			it(title="Checking the wrap() function", body=function( currentSpec ) {
				var string = "Lucee Documentation";
				var wrapped_string = wrap(string, 4);

				expect(wrapped_string.len()).toBe(28)
				expect(wrapped_string).toBe("luce
e
docu
ment
atio
n");

			});
		});
	}
} 


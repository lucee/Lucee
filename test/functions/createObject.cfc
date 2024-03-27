component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title = "Testcase for createObject() function", body = function() {
			it( title = "Checking the createObject() function", body = function( currentSpec ) {
				object = createObject('java',"java.lang.StringBuffer")
				expect(isObject(object)).toBeTrue();
				expect(object.length()).toBe(0);
			});
		});
	}
}
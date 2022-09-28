component extends = "org.lucee.cfml.test.LuceeTestCase"  {

	function run( testresults , testbox ) {
		describe( "Testcase for LDEV-3184", function () {
			it( title="Checking Tag islands in cfc function below the static function", body = function ( currentSpec ) {
				try {
					var result = new LDEV3184.LDEV3184().foo();
				}
				catch(any e) {
					var result = e.message;
				}
				expect(trim(result) ).toBe("I'm a static method");
			});
		});
	}

}
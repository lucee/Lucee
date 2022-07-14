component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3846", function() {
			it(title="serialize a catch block", body=function( currentSpec ) {
				try {
					throw "shit happen!";
				}
				catch(e) {
					dump(objectsave(e));
				}
			});
		});
	}
}
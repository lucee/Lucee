component extends="org.lucee.cfml.test.LuceeTestCase" labels="extensions"{
	function run( testResults , testBox ) {
		describe( "test case for extensionExists()", function() {

			it(title = "Checking extensionExists()", body = function( currentSpec ) {
				var exts = ExtensionList();
				loop query=exts {
					expect( extensionExists( exts.id ) ).toBeTrue();
				}
			});

		});
	}
}
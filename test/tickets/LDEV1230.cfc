component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1230", function() {
			it( title='Checking isValid() for image object', body=function( currentSpec ) {
				var newImage = ImageNew("", 1, 1);
				var result = IsValid("string", newImage);

				assertEquals(result,true);
			});

			it( title='Checking isValid() for saved image object', body=function( currentSpec ) {
				var uri = createURI("LDEV1230/mitrah.jpg");
				var savedImage = ImageNew(uri);
				var result = IsValid("string", savedImage);
				assertEquals(result,true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV4726"); 	
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4726", body=function() {
			it( title="Checking isNull() with undefined structure", body=function( currentSpec ) {
				var hasError = false;
				try {
					result = _internalRequest(
						template : "#variables.uri#/LDEV4726.cfm",
						forms : { scene = 1 }
					);
				} catch ( any e ) {
					var hasError = true;
				}
				expect(hasError).toBeFalse();
				if (!hasError) {
					expect(result.filecontent).toBeTrue();
				}
			});

			it( title="Checking isNull() with defined structure", body=function( currentSpec ) {
				var hasError = false;
				try {
					result = _internalRequest(
						template : "#variables.uri#/LDEV4726.cfm",
						forms : { scene = 2 }
					);
				} catch (e) {
					var hasError = true;
				}
				expect(hasError).toBeFalse();
				if (!hasError) {
					expect(result.filecontent).toBeFalse();
				}
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
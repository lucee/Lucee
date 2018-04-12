component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1797", function() {
			it( title='checking threads tags in member function', body=function( currentSpec ) {
				var uri = createURI("LDEV1797");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('false');
			});

			it( title='checking threads tags without member function', body=function( currentSpec ) {
				var arr = [];
				var hasError = false;
				try {
					ARRAYEACH([1, 2, 3], function(i){
						arr.append("testThread#i#");
						thread name="testThread#i#"{
							sleep(300);
						}
					});
				} catch ( any e ){
					hasError = true;
				}
				expect(hasError).toBe('false');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
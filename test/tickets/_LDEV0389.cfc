component extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf"{
	function beforeAll(){
		uri = createURI("LDEV0389");
		if(not directoryExists(uri)){
			Directorycreate(uri);
		}

		if(not fileExists('#uri#/test.pdf')){
			cfdocument(format="PDF" filename='#uri#/test.pdf'){
			}
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-389", function() {
			it("Checking directoryList, call back function with no arguments", function( currentSpec ) {
				try {
					uri = createURI("LDEV0389");
					result = directoryList(uri, true, "array", function(){
						return true;
					});
				} catch ( any e ){
					result[1] = e.message;
				}
				expect(result[1]).toBe(ExpandPath("#uri#/test.pdf"));
			});

			it("Checking directoryList, call back function with single argument", function( currentSpec ) {
				try {
					uri = createURI("LDEV0389");
					result = directoryList(uri, true, "array", function(a){
						return true;
					});
				} catch ( any e ){
					result[1] = e.message;
				}
				expect(result[1]).toBe(ExpandPath("#uri#/test.pdf"));
			});

			it("Checking directoryList, call back function with two arguments", function( currentSpec ) {
				try {
					uri = createURI("LDEV0389");
					result = directoryList(uri, true, "array", function(a,b){
						return true;
					});
				} catch ( any e ){
					result[1] = e.message;
				}
				expect(result[1]).toBe(ExpandPath("#uri#/test.pdf"));
			});
		});
	}
	// private function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
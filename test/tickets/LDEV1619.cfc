component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
		variables.uri = createURI("LDEV1619");
	}
	function run( testResults , testBox ) {
		describe( "Test cases for LDEV-1619", function() {
			it(title = "Checking URL with first parameter as empty", body = function( currentSpec ) {
				expect(testUrl("a=&a=1")).toBe(",1");
			});
			it(title = "Checking URL with second parameter as empty", body = function( currentSpec ) {
				expect(testUrl("a=1&a=")).toBe("1,");
			});
			it(title = "Checking URL with middle parameter as empty", body = function( currentSpec ) {
				expect(testUrl("a=1&a=&a=3")).toBe("1,,3"); 
			});
			it(title = "Checking URL with first & last parameter as empty", body = function( currentSpec ) {
				expect(testUrl("a=&a=2&a=")).toBe(",2,");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
	private string function testUrl( required string url ) {
		var res = _InternalRequest(
			template:"#uri#/test.cfm",
			urls:arguments.url
		);
		return trim( res.fileContent );
	} 
} 
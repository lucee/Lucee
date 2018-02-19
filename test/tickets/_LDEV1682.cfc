component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1682", body=function() {
			it(title="Checking REFind() attribute SCOPE ", body = function( currentSpec ) {
				var uri = createURI("LDEV1682");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe("true||2");
			});

			it(title="Checking REFind() returns struct with matching value", body = function( currentSpec ) {
				var testString = "I'm testing RE expression {%1%} test Letter";
				var result = refind("{%\d%}", testString, 1 ,  true);
				expect(isArray(result)).toBe(true);
				if(isArray(result)){
					expect(structkeyList(result[1])).toBe("MATCH,LEN,POS");
				}
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}

component extends="org.lucee.cfml.test.LuceeTestCase" labels="static" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV4212";
		variables.uri = createURI("LDEV4212");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4212", function() {
			it(title="checking static scope of child and parent cfc files", body=function( currentSpec ){
				writeComponentFile(fileName="parent.cfc", staticKey="fromParent", staticValue="from parent", isChild=false);
				writeComponentFile(fileName="child.cfc", staticKey="fromChild", staticValue="from Child", isChild=true);
				var res = _internalRequest(
					template = "#variables.uri#/LDEV4212.cfm"
				);

				var result = listToArray(res.fileContent.trim());

				expect(result[1]).toBe("from Child");
				expect(result[2]).toBe("from Parent");
			});
			it(title="checking static scope after both child and parent cfc file sourceCode changed", body=function( currentSpec ){
				// changes the both child and parent cfc files source code
				writeComponentFile(fileName="parent.cfc", staticKey="fromParent", staticValue="parent source code changed", isChild=false);
				writeComponentFile(fileName="child.cfc", staticKey="fromChild", staticValue="child source code changed", isChild=true);

				var res = _internalRequest(
					template = "#variables.uri#/LDEV4212.cfm"
				);

				var result = listToArray(res.fileContent.trim());
				
				expect(result[1]).toBe("child source code changed");
				expect(result[2]).toBe("parent source code changed");
			});
			it(title="checking static scope after only the parent cfc file sourceCode changed", body=function( currentSpec ){
				// changes the parent cfc file source code only
				writeComponentFile(fileName="parent.cfc", staticKey="fromParent", staticValue="parent source code only changed again", isChild=false);

				var res = _internalRequest(
					template = "#variables.uri#/LDEV4212.cfm"
				);

				var result = listToArray(res.fileContent.trim());

				expect(result[1]).toBe("child source code changed"); // its still remains the same as the second iteration result
				expect(result[2]).toBe("parent source code only changed again");
			});
		});
	}

	function afterAll() {
		fileDelete("#variables.dir#/child.cfc");
		fileDelete("#variables.dir#/parent.cfc");
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}


	// this function is helps to write the cfc files/change the source code of the cfc files
	private function writeComponentFile(required String fileName, required String staticKey, required String staticValue, boolean isChild = isChild=false) {

		extends = "";
		if (isChild) extends = 'extends="parent"';

		var cfcSourceCode = 'component ' & extends &' {
	static {
		#arguments.staticKey# = "#arguments.staticValue#";
	}
}';

		fileWrite("#variables.dir#/#arguments.fileName#", cfcSourceCode); // write and rewrite the cfc files
	}
}
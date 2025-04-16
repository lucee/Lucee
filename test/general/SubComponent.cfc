component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll() {
		variables.cfcFile = getDirectoryFromPath(getCurrentTemplatePath()) & "subComponent/testComp.cfc";
		variables.uri = createURI("subComponent")

		writeComponentFileWithsubComponent();
	}

	function afterAll() {
		if (fileExists(variables.cfcFile)) fileDelete(variables.cfcFile);
	}

	function run( testResults , testBox ) {
		describe( "test sub component", function() {
			it(title="tag based main component", body=function() {
				var cfc=new subComponent.TestSubTag();
				expect(cfc.testtag()).toBe("tag:closure-insidetag:argclosuretag");
				expect(cfc.testscript()).toBe("script:closure-insidescript:argclosurescript");
			});
			it(title="tag based sub component", body=function() {
				var cfc=new subComponent.TestSubTag$sub();
				expect(cfc.subtest()).toBe("subito");
				expect(cfc.bb()).toBe("bb");
			});
			it(title="script based main component", body=function() {
				var cfc=new subComponent.TestSubScript();
				expect(cfc.test()).toBe("main:closure-insidemain:argclosuremain");
				expect(cfc.a()).toBe("a");
				expect(cfc.b()).toBe("b");
			});
			it(title="script based sub component", body=function() {
				var cfc=new subComponent.TestSubScript$sub();
				expect(cfc.test()).toBe("sub:closure-insidesub:argclosuresub");
				expect(cfc.c()).toBe("c");
				expect(cfc.d()).toBe("d");
			});
			it(title="checking sub component this scope", body=function( currentSpec ){

				var result = _internalRequest(
					template = "#variables.uri#/index.cfm",
					forms = {scene:1}
				).fileContent.trim();

				var res = listToArray(result);

				expect(res[1]).toBe("from sub component");
				expect(res[2]).toBe("from sub function");
			});
			it(title="checking sub component static scope", body=function( currentSpec ){
			   var res = _internalRequest(
					template = "#variables.uri#/index.cfm",
					forms = {scene:2}
				).fileContent.trim();

				expect(res).toBe("from sub static");
			});
			it(title="checking sub component after the code changed", body=function( currentSpec ){
				writeComponentFileWithsubComponent(additionalFunction='function addiFunc() { return "from additional function";}');

				var res = _internalRequest(
					template = "#variables.uri#/index.cfm",
					forms = {scene:3}
				).fileContent.trim();

				expect(res).toBe("from additional function");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	// this function is helps to write the cfc files/change the source code of the cfc files
	private function writeComponentFileWithsubComponent(String additionalFunction="") {

		var cfcSourceCode = '
component {

	this.main = "from main"

	static {
		mainStatic = "from main static";
	}

	function mainFunc() {
		return "from main function";
	}
}

component name="testSub" {

	 this.sub = "from sub component"

	static {
		subStatic = "from sub static";
	}

	function subFunc() {
		return "from sub function";
	}

	' & additionalFunction & '
}'

		fileWrite(variables.cfcFile, cfcSourceCode); // write and rewrite the cfc files
	}
}

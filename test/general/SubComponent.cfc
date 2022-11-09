component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll() {
		variables.cfcFile = getDirectoryFromPath(getCurrentTemplatePath()) & "cfc/testComp.cfc";
		variables.uri = createURI("cfc")

		writeComponentFileWithSubComponent();
	}

	function afterAll() {
		if (fileExists(variables.cfcFile)) fileDelete(variables.cfcFile);
	}

	function run( testResults , testBox ) {
		describe( "test sub component", function() {
			it(title="tag based main component", body=function() {
				var cfc=new cfc.TestSubTag();
				expect(cfc.testtag()).toBe("tag:closure-insidetag:argclosuretag");
				expect(cfc.testscript()).toBe("script:closure-insidescript:argclosurescript");
			});
			it(title="tag based sub component", body=function() {
				var cfc=new cfc.TestSubTag$sub();
				expect(cfc.subtest()).toBe("subito");
			});
			it(title="script based main component", body=function() {
				var cfc=new cfc.TestSubScript();
				expect(cfc.test()).toBe("main:closure-insidemain:argclosuremain");
			});
			it(title="script based sub component", body=function() {
				var cfc=new cfc.TestSubScript$sub();
				expect(cfc.test()).toBe("sub:closure-insidesub:argclosuresub");
			});
			it(title="checking sub component this scope", body=function( currentSpec ){

				var result = _internalRequest(
					template = "#variables.uri#/index.cfm",
					forms = {scene:1}
				).fileContent.trim();

				res = listToArray(result);

				expect(res[1]).toBe("from sub component");
				expect(res[2]).toBe("from sub function");
			});
			it(title="checking sub component static scope", skip=true, body=function( currentSpec ){
			   var res = _internalRequest(
					template = "#variables.uri#/index.cfm",
					forms = {scene:2}
				).fileContent.trim();

				expect(res).toBe("from sub static");
			});
			it(title="checking sub component after the code changed", skip=true, body=function( currentSpec ){
				writeComponentFileWithSubComponent(additionalFunction='function addiFunc() { return "from additional function";}');

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
	private function writeComponentFileWithSubComponent(String additionalFunction="") {

		cfcSourceCode = '
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

component extends="org.lucee.cfml.test.LuceeTestCase" labels="subComponent" {

    function beforeAll() {
        variables.cfcFile = getDirectoryFromPath(getCurrentTemplatePath()) & "subComponent/testComp.cfc";
        variables.uri = createURI("subComponent")

        writeComponentFileWithSubComponent();
    }

    function run( testResults, testBox ) {
        describe("Testcase for LDEV-4212", function() {
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
                writeComponentFileWithSubComponent(additionalFunction='function addiFunc() { return "from additional function";}');

                var res = _internalRequest(
                    template = "#variables.uri#/index.cfm",
                    forms = {scene:3}
                ).fileContent.trim();

                expect(res).toBe("from additional function");
            });
        });
    }

    function afterAll() {
        fileDelete(variables.cfcFile);
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
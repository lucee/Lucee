component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" skip=true {
    function beforeAll(){
        variables.uri = createURI("LDEV3393");
    }
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3393", function() {
            arr = [1,2,3,4];
            it( title="cfloop inside try{}", body=function( currentSpec ){
                try{
                    res = [];
                    cfloop(array=arr, item="item"){
                         res.append(item);
                    }
                }
                finally{}
                expect(res).tobe(arr);
            });
            it( title="For-loop inside try{}", body=function( currentSpec ){
                try{
                    res = [];
                    for (item in arr){
                        res.append(item);
                    }
                }
                finally{}
                expect(res).tobe(arr);
            });
            it( title="cfloop inside finally{}", body=function( currentSpec ){
                try{
                    res = [];
                }
                finally{
                    cfloop(array=arr, item="item"){
                         res.append(item);
                    }
                }
                expect(res).tobe(arr);
            });
            it( title="For-loop inside finally{}", body=function( currentSpec ){
                try{
                    local.result = _InternalRequest(
                        template : "#uri#\test.cfm"
                    );
                }
                catch(any e){
                    result.fileContent = e.message;
                }
                expect(trim(result.fileContent)).tobe(serializeJSON(arr));
            });

            it( title="Compiler NPE crash with CFFinally block LDEV-2456", body=function( currentSpec ){
                try {
                    local.result = _InternalRequest(
                        template : "#uri#\testFinallyBlock.cfm"
                    );
                }
                catch(any e){
                    result.fileContent = e.message;
                }
                expect(trim(result.fileContent)).notToInclude("java.lang.NullPointerException");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}
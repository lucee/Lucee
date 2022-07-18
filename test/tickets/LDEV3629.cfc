component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run ( testResults , testBox ) {
        describe("Testcase for LDEV-3629", function(){
            it(title="Call Webservice with returnType XML", body=function( currentSpec ){
                try {
                    path = "http://"&cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME);
                    ws = createObject("webservice", "#path#LDEV3629/test.cfc?wsdl");
                    res = ws.getCustomer();
                }
                catch(any e) {
                    res = e.message;
                }
                expect(toString(res)).toBe('<?xml version="1.0" encoding="UTF-8" standalone="no"?><customers><customer>test</customer></customers>');
            });
        });
    }
}
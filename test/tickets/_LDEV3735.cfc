component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3735", function(){
            it(title="Checking QoQ with divided by zero", body=function( currentSpec ){
                try {
                    hasError = false;
                    qry = QueryNew('foo','integer',[[40]]);
                    result = queryExecute("SELECT 5/0 As inf From qry", {}, {dbType:"query"});
                }
                catch(any e){
                    hasError = true;
                }
                expect(hasError).toBe(true);
            });
        });
    }
}
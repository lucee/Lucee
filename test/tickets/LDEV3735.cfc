component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" skip="true" {
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3735", function(){
            it(title="Checking QoQ throws on divide by zero", body=function( currentSpec ){
                expect( function(){
                    qry = QueryNew('foo','integer',[[40]]);
                    result = queryExecute("SELECT 5/0 As inf From qry", {}, {dbType:"query"});
                }).toThrow();
            });
        });
    }
}
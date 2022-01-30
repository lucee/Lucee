component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true labels="qoq"{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3734", function(){
            it(title="Arithmetic operation with NULL in QoQ", body=function( currentSpec ){
                application enableNullSupport=true;
                qry = QueryNew('foo','integer',[[40]]);
                res = queryExecute("SELECT NULL-5 AS inf FROM qry", {}, {dbtype="query"}).inf;
                expect(res).toBeNull();
            });
        });
    }
}
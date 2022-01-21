component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3735", function(){

            it(title="Checking QoQ throws on divide by zero", body=function( currentSpec ){
                var qry = QueryNew('foo','integer',[[40]]);
                expect( ()=>queryExecute("SELECT 5/0 As inf From qry", {}, {dbType:"query"}) ).toThrow();
            });

            it(title="Checking QoQ throws on divide by zero with modulus", body=function( currentSpec ){
                var qry = QueryNew('foo','integer',[[40]]);
                expect( ()=>queryExecute("SELECT 5%0 As inf From qry", {}, {dbType:"query"}) ).toThrow();
            });

            it(title="Checking QoQ throws on divide by zero has useful message", body=function( currentSpec ){
                var qry = QueryNew('foo,bar','integer,integer',[[40,50],[50,100],[10,0]]);
                expect( ()=>queryExecute("SELECT foo/isNull(bar,2) As inf From qry", {}, {dbType:"query"}) ).toThrow( regex="Encountered while evaluating \[foo / isnull\(bar,2\)] in row 3" );
                expect( ()=>queryExecute("SELECT max(foo)/min(bar) As inf From qry", {}, {dbType:"query"}) ).toThrow( regex="Encountered while evaluating \[max\(foo\) / min\(bar\)] in row 1" );
            });

        });
    }
}
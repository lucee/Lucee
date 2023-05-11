component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3774", function() {
            it( title="queryNew() using struct as data and struct key has space", body=function( currentSpec ){
                sct = {"foo":"foo", " bar ":"bar"}
                qry = queryNew("foo,bar","varchar,varchar",sct);
                expect(qry.foo).toBe("foo");
                expect(qry.bar).toBe("bar");
            });
            it( title="queryNew() using array of struct as data and struct key has space", body=function( currentSpec ){
                arr = [{"foo":"foo", " bar ":"bar"}]
                qry = queryNew("foo,bar","varchar,varchar",arr);
                expect(qry.foo).toBe("foo");
                expect(qry.bar).toBe("bar");
            });
        });
    }
}
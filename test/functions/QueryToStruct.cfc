component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,struct" {
    function beforeAll() {
        variables.qry = query(
            id:[1,2,3,4],
            name:["mssql","mysql","image","pdf"],
            version:["7.2.2.jre8","8.0.30","1.0.0.42","1.1.0.7"]
        );
    }

    function run( testResults, testBox ){
        describe("Testcase for QueryToStruct() function", function( currentSpec ) {
            it(title="QueryToStruct()", body=function( currentSpec )  {
                var res = queryToStruct(variables.qry, "name");
                expect(res).toBeTypeOf("struct"); 
                expect(structKeyList(res)).toBe("mssql,mysql,image,pdf");
                expect(res.image.version).toBe("1.0.0.42");
            });

            it(title="QueryToStruct() valueRowNumber argument", body=function( currentSpec )  {
                var res = queryToStruct(variables.qry, "name", "ordered", true);
                expect(res.mysql).toBeTypeOf("string");
                expect(res.pdf).toBe(4);
            });

            it(title="Query.toStruct() member function", body=function( currentSpec )  {
                var res = variables.qry.ToStruct("id");
                expect(res).toBeTypeOf("struct"); 
                expect(structKeyList(res)).toBe("1,2,3,4");
                expect(res.4.version).toBe("1.1.0.7");
            });
        });
    }
}
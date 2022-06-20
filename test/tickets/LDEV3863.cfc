component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults,testBox ){
        describe("Testcase for LDEV-3873", function(){
            variables.qry = queryNew("id,name,num", "integer,varchar,integer", [[1,"foo",1],[2,"bar",2]]);
            it( title="queryExecute with \' in sql and positional params", body=function( currentSpec ){
                var posParam = queryExecute(sql="SELECT 'D:\test\' AS path, name FROM qry WHERE id = ? AND num = ?",
                    params=[1,1],
                    options={dbtype="query"}
                );
                expect(posparam.path).toBe("D:\test\");
                expect(posparam.name).toBe("foo");
                expect(posParam.getSql().toString()).toBe("SELECT 'D:\test\' AS path, name FROM qry WHERE id = '1' AND num = '1'");
            });
            it( title="queryExecute with \' in sql and named params", body=function( currentSpec ){
                var namedParam = queryExecute(sql="SELECT 'D:\test\' AS path, name FROM qry WHERE id = :id AND num = :num",
                    params={id:1,num:1},
                    options={dbtype="query"}
                );
                expect(namedParam.path).toBe("D:\test\");
                expect(namedParam.name).toBe("foo");
                expect(namedParam.getSql().toString()).toBe("SELECT 'D:\test\' AS path, name FROM qry WHERE id = '1' AND num = '1'");
            });

            it( title="queryExecute with empty params struct", body=function( currentSpec ){
                var emptyParams = queryExecute(sql="SELECT name FROM qry",
                    params={},
                    options={dbtype="query"}
                );
                expect(emptyParams.recordcount).toBe(2);
            });

            it( title="queryExecute with empty params struct, but with a :id param", body=function( currentSpec ){
                var emptyParams = queryExecute(sql="SELECT name FROM qry where id = :id",
                    options={dbtype="query"}
                );
                expect(emptyParams.recordcount).toBe(2); // :id becomes null, 0 rows returned
            });

            it( title="queryExecute with empty params struct, but with a ? param", body=function( currentSpec ){
                var emptyParams = queryExecute(sql="SELECT name FROM qry where id = ?",
                    options={dbtype="query"}
                ); // fails with native QoQ, falls back to hsqldb, Assert failed: S0000 Direct execute with param count > 0java.lang.Exception 
                expect(emptyParams.recordcount).toBe(2); 
            });
        });
    }
}

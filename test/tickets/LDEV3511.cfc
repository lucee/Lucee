component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe("Testcase for LDEV3511", function(){
            var qry = queryNew( 'col1,col2,col3', 'integer,integer,integer', [[1,2,3],[4,5,6]] )
            it( title="Check query with returnType = query", body=function( currentSpec ){
                resQuery = queryExecute('SELECT * FROM qry',[],{dbType : 'query'});
                expect(serializeJSON(resQuery)).toBe('{"COLUMNS":["col1","col2","col3"],"DATA":[[1,2,3],[4,5,6]]}');
            });
            it( title="Check query returnType = array is uses ordered struct", body=function( currentSpec ){
                resArray = queryExecute('SELECT * FROM qry',[],{dbType : 'query', returnType="array"});
                expect(serializeJSON(resArray)).toBe('[{"col1":1,"col2":2,"col3":3},{"col1":4,"col2":5,"col3":6}]');
                expect(GetMetadata(resArray[1]).ordered).tobe("ordered");
            });
            it( title="Check query returnType = struct is uses ordered struct", body=function( currentSpec ){
                resStruct = queryExecute('SELECT * FROM qry',[],{dbType : 'query', returnType="struct", columnKey="col1"});
                expect(serializeJSON(resStruct)).toBe('{"1":{"col1":1,"col2":2,"col3":3},"4":{"col1":4,"col2":5,"col3":6}}');
                expect(GetMetadata(resStruct[1]).ordered).tobe("ordered");
            });
        });
    }
}
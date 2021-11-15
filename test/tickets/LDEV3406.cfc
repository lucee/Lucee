component extends="org.lucee.cfml.test.LuceeTestCase"  skip=true {
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3406", function() {
            it( title="serializeJSON() with struct", body=function( currentSpec ){
                sct = {boolean:true,int:100,string:"string",intString:"1000",booleanString:"true"}
                res = serializeJSON(sct);
                writeDump(res);
                expect(res).toBe('{"INT":100,"BOOLEANSTRING":"true","BOOLEAN":true,"INTSTRING":"1000","STRING":"string"}')
            });
            it( title="serializeJSON() with query column type bit", body=function( currentSpec ){
                qry = queryNew("bitCol","bit",[{"bitCol":0},{"bitCol":1}]);
                expect(serializeJSON(qry)).toBe('{"COLUMNS":["bitCol"],"DATA":[[false],[true]]}');
            });
            it( title="serializeJSON() with query column type integer", body=function( currentSpec ){
                qry = queryNew("intCol","integer",[{"intCol":1000},{"intCol":"1000"}]);
                expect(serializeJSON(qry)).toBe('{"COLUMNS":["intCol"],"DATA":[[1000],[1000]]}');
            });
            it( title="serializeJSON() with query column type varchar", body=function( currentSpec ){
                qry = queryNew("varcharCol","varchar",[{"varcharCol":1000},{"varcharCol":"1000"}]);
                expect(serializeJSON(qry)).toBe('{"COLUMNS":["varcharCol"],"DATA":[["1000"],["1000"]]}');
            });
        });
    }
}
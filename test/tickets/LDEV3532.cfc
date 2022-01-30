component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe( "Test case for LDEV-3532", function() {
            it( title="reFind() no matches with regex type java", body=function( currentSpec ){
                cfapplication(regex = {type="java"});
                expect(reFind("(f)(oo)", "bar", 1, false)).toBe(0);
                expect(reFind("(f)(oo)", "bar", 1, false, "ALL")).toBe(0);
            });
            it( title="reFind() no matches with regex type java and returnsubexpressions = true", body=function( currentSpec ){
                expect(serializeJSON(reFind("(f)(oo)", "bar", 1, true))).toBe('{"match":[""],"len":[0],"pos":[0]}');
                expect(serializeJSON(reFind("(f)(oo)", "bar", 1, true,"all"))).toBe('[{"match":[""],"len":[0],"pos":[0]}]');
            });
            it( title="reFind() no matches with regex type perl", body=function( currentSpec ){
                try{
                    cfapplication(regex = {type="perl"});
                    res = [];
                    res[1] = reFind("(f)(oo)", "bar", 1, false);
                    res[2] = reFind("(f)(oo)", "bar", 1, false,"all");
                }
                catch(any e){
                    res = e.message;
                }
                expect(serializeJSON(res)).toBe("[0,0]");
            });
            it( title="reFind() no matches with regex type perl and returnsubexpressions = true", body=function( currentSpec ){
                    expect(serializeJSON(reFind("(f)(oo)", "bar", 1, true))).toBe('{"match":[""],"len":[0],"pos":[0]}');
                    expect(serializeJSON(reFind("(f)(oo)", "bar", 1, true,"all"))).toBe('[{"match":[""],"len":[0],"pos":[0]}]');
            });
        });
    }
}
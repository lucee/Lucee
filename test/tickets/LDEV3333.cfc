component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3333", function() {
            it( title="Circular references with struct", body=function(){
                    sctOne = {one="one",two="two"};
                    sctTwo = {three="three",four="four"};
                    sctOne.append(sctTwo);
                    sctTwo.append(sctOne);
                    res = serializeJSON(sctTwo);
                expect(res).toBe('{"TWO":"two","THREE":"three","ONE":"one","FOUR":"four"}');
            });
            it( title="Circular references with array", body=function(){
                try{
                    arrOne = [1,2,3];
                    arrTwo = ["one","two","three"];
                    arrOne.append(arrTwo);
                    arrTwo.append(arrOne)
                    res = serializeJSON(arrTwo);
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe('["one","two","three",[1,2,3,["one","two","three"]]]');
            });
        });
    }
}
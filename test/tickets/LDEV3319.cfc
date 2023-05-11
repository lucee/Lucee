component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults, testBox ){
        describe( "Testcase for LDEV-3319", function(){
            it( title="Check undefined array element with array index a[1]", body=function( currentSpec ){
                try{
                    arr = [];
                    arr[15] = 1;
                    res = arr[1];
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("Element at position [1] does not exist in list");
            });
            it( title="Check undefined array element with array index a[1] and null support true", body=function( currentSpec ){
                try{
                    application enableNullSupport=true;
                    arr = [];
                    arr[15] = 1;
                    res = arr[1];
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBeNull();
            });
            it( title="Check undefined array element with arrayfirst and null support true", body=function( currentSpec ){
                try{
                    application enableNullSupport=true;
                    arr = [];
                    arr[15] = 1;
                    res = arrayFirst(arr);
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBeNull();
            });
            it( title="Check undefined array element with arrayfirst and null support false", body=function( currentSpec ){
                try{
                    application enableNullSupport=false;
                    arr = [];
                    arr[15] = 1;
                    res = arrayFirst(arr);
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("undefined");
            });
        });
    }
}
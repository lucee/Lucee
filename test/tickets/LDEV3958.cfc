component extends="org.lucee.cfml.test.LuceeTestCase" labels="array"{
    function run( testResults, testBox ) {
        describe("Testcase for LDEV3958", function() {
            it( title="Checking performance of arraySlice() with array has 2.1M elements", body=function( currentSpec ) {
                var arr = []
                for( i = 1; i <= 2100000; i++ ) {
                    arr.append(i);
                }
                var start = getTickCount();
                var sliced = arr.slice( 1000000, 100 );
                var time =  getTickCount()-start;
                expect(sliced.len()).toBe(100);
                expect(time).toBeLT(20);
            });
            it( title="Checking arraySlice() is return new array", body=function( currentSpec ) {
                var arr = [1,2,3,4,5];
                var sliced = arraySlice(arr, 1, 3);
                sliced[1] = "mutated";
                sliced.append("inserted");
                expect(arr[1]).toBe(1);
                expect(arr.len()).toBe(5);
                expect(sliced.len()).toBe(4);
            });
            it( title="Checking arraySlice() with three dimensional array", body=function( currentSpec ) {
                var Arr3dim=Arraynew(3);
                Arr3dim[1][1][1]="1";
                Arr3dim[1][1][2]="2";
                Arr3dim[1][1][3]="3";
                Arr3dim[2][1][1]="4";
                Arr3dim[2][1][2]="5";
                Arr3dim[2][1][3]="6";

                var subArr = arrayslice(Arr3dim, 2, 1);

                expect(subArr.getDimension()).toBe(3);
                expect(subArr.len()).toBe(1);
            });
        });
    }     
}
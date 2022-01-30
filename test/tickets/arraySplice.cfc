component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testbox ){
        describe( "Testcase for function arraySplice", function(){
            arr = ["one","two","three","four","five","six"];
            it( title="Check arraySplice function", body=function( currentSpec ){
                expect(arrayToList(arraySplice(arr,3,2))).toBe("three,four");
                expect(arrayToList(arr)).toBe("one,two,five,six");
            });
            it( title="Check arraySplice function with arguments replacements", body=function( currentSpec ){
                arr = ["one","two","three","four","five","six"];
                item = [1,2,3];
                expect(arrayToList(arraySplice(arr,3,2,item))).toBe("three,four");
                expect(arrayToList(arr)).toBe("one,two,1,2,3,five,six");
                expect(arrayToList(arraySplice(arr,3,3,[]))).toBe("1,2,3");
                expect(arrayToList(arr)).toBe("one,two,five,six");
            });
            it( title="arraySplice with index greater than length", body=function( currentSpec ){
                arr = ["one","two","three","four","five","six"];
                expect(arrayToList(arraySplice(arr,8,1))).toBe("");
                expect(arrayToList(arr)).toBe("one,two,three,four,five,six");
            });
            it( title="arraySplice with negative index", body=function( currentSpec ){
                arr = ["one","two","three","four","five","six"];
                expect(arrayToList(arraySplice(arr,-2,2,[1,2]))).toBe("five,six");
                expect(arrayToList(arr)).toBe("one,two,three,four,1,2");
            });
        });
    }
}
component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run() {
        describe("Test suite for LDEV-1298", function(){
            it("checking Built in function with call back", function(){
                var testString = "AbCd";
                var applyTo = function(object, operation){
                    return operation(object);
                };
                var result = applyTo(testString, ucase);
                expect(result).toBeWithCase("ABCD");
            });

            describe("checking arraysort() with BIF as string comparators", function(){
                it("checking with function expression calling compareNoCase as a string comparator when sorting", function(){
                    var arrayToSort = ["d","C","b","A"];
                    arrayToSort.sort(function(e1,e2){
                        return compareNoCase(e1, e2);
                    });
                    expect(arrayToSort).toBe(["A","b","C","d"]);
                });

                it("checking with compareNoCase() as a string comparator when sorting", function(){
                    var arrayToSort = ["d","C","b","A"];
                    arrayToSort.sort(compareNoCase);
                    expect(arrayToSort).toBe(["A","b","C","d"]);
                });
            });

            describe("checking listsort(), with BIF as string comparators", function(){
                it("checking with function expression calling compareNoCase as a string comparator when sorting", function(){
                    var listToSort = "d,C,b,A";
                    var sortedList = listToSort.listSort(function(e1,e2){
                        return compareNoCase(e1, e2);
                    });
                    expect(sortedList).toBe("A,b,C,d");
                    expect(ListToSort).toBe("d,C,b,A");
                });

                it("checking with compareNoCase() as a string comparator when sorting", function(){
                    var listToSort = "d,C,b,A";
                    var sortedList = listToSort.listSort(compareNoCase);
                    expect(sortedList).toBe("A,b,C,d");
                    expect(listToSort).toBe("d,C,b,A");
                });
            });
        });
    }
}

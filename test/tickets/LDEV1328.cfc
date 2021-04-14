component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run(testResults, testBox) {
        
        describe("Test case for LDEV-1328", function() {
            
            it(title="checks that offset 1 returns the same as listRest", body=function(currentSpec) {

                var st1 = callStackGet("text");
                var st2 = callStackGet("text", 1);
                assertEquals(
                    trim(listRest(st1, ";")), st2
                );
            })
            
            it(title="checks that offset and maxLength return the expected subsets", body=function(currentSpec) {

                var st1 = callStackGet();
                
                var st2 = callStackGet("array", 0, 4);
                assertEquals(
                    4, arrayLen(st2)
                );

                // 1st element of st2 should be same as 1st element of st1 because offset is 0
                assertEquals(
                    st1[1].function, st2[1].function
                );

                var st3 = callStackGet("array", 2, 2);
                assertEquals(
                    2, arrayLen(st3)
                );

                // 1st element of st3 should be 3rd element of st2 because offset is 2
                assertEquals(
                    st2[3].function, st3[1].function
                );
            })
        });
    }
}
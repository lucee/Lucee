component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

    function run(testResults, testBox) {
        describe(title = "Test case for LDEV-2160", body = function() {
            var date = createDate(2025, 6, 1); // 1st June 2025 is a Sunday
            it(title = "should return 7 for ISO calendar (Sunday)", body = function( currentSpec ) {
                try{
                    var result = dayOfWeek(dateTime, "iso");
                }
                catch (any error) {
                    result = 0;
                }
                expect(result).toBe(7);
            });

            it("should return 1 for Gregorian calendar (Sunday)", function( currentSpec ) {
                try{
                    var result = dayOfWeek(dateTime, "gregorian");
                }
                catch (any error) {
                    result = 0;
                }
                expect(result).toBe(1);
            });

        });
    }

}
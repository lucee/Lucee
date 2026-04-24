component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfstopwatch" {
    function run( testResults, testBox ) {
        describe("Testcase for cfstopwatch tag", function() {
            it( title="Checking stopwatch", body=function( currentSpec ) {
                stopwatch variable="local.stopwatchVar" {
                    sleep( 2 );
                }

                expect(stopwatchVar).toBeNumeric();
                expect(stopwatchVar).toBeTrue();
            });
        });
    }
}
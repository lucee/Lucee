component extends = "org.lucee.cfml.test.LuceeTestCase" labels="thread" {

    function run( testResults, textbox ) {
        describe("testcase for LDEV-3719", function(){
            it(title="Checking threads ELAPSEDTIME with different processing time", body=function( currentSpec ){
                var threadName = "threadName_#createUUID()#";
                thread name = "#threadName#_one" {
                    sleep( 50 );
                }
                thread name = "#threadName#_two" {
                    sleep( 100 );
                }
                thread name = "#threadName#_three" {
                    sleep( 200 );
                }
                
                sleep(20);

                // checking running threads ELAPSEDTIME should be greater than 0
                expect(cfthread["#threadName#_one"].ELAPSEDTIME).toBeGT(0);
                expect(cfthread["#threadName#_two"].ELAPSEDTIME).toBeGT(0);
                expect(cfthread["#threadName#_three"].ELAPSEDTIME).toBeGT(0);

                thread action = "join" name="#threadName#_one,#threadName#_two,#threadName#_three";
                expect(cfthread["#threadName#_one"].ELAPSEDTIME).toBeBetween(50, 100);
                expect(cfthread["#threadName#_two"].ELAPSEDTIME).toBeBetween(100, 200);
                expect(cfthread["#threadName#_three"].ELAPSEDTIME).toBeBetween(200, 300);

                sleep(100); // after page sleeps thread ELAPSEDTIME should remain the same

                expect(cfthread["#threadName#_one"].ELAPSEDTIME).toBeBetween(50, 100);
                expect(cfthread["#threadName#_two"].ELAPSEDTIME).toBeBetween(100, 200);
                expect(cfthread["#threadName#_three"].ELAPSEDTIME).toBeBetween(200, 300);
            });

            it(title="Checking threads ELAPSEDTIME with the terminated thread", body=function( currentSpec ){
                var threadName = "threadName_#createUUID()#";
                thread name = "#threadName#_four" {
                    sleep(50);
                    throw "error";
                }
                thread action = "join" name="#threadName#_four";
                expect(cfthread["#threadName#_four"].ELAPSEDTIME).toBe(0);

                thread name = "#threadName#_five" {
                    sleep(500);
                }
                thread action="terminate" name="#threadName#_five";
                thread action="join" name="#threadName#_five";
                expect(cfthread["#threadName#_five"].ELAPSEDTIME).toBe(0);
            });
        });
    }
}
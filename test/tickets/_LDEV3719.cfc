component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{

    function run( testResults, textbox ) {
        describe("testcase for LDEV-3719", function(){
            it(title="Checking threads ELAPSEDTIME with different processing time", body=function( currentSpec ){
                thread name = "one" {
                    sleep( 50 );
                }
                thread name = "two" {
                    sleep( 100 );
                }
                thread name = "three" {
                    sleep( 200 );
                }

                thread action = "join" name="one,two,three";
                expect(cfthread.one.ELAPSEDTIME).toBeBetween(50, 100);
                expect(cfthread.two.ELAPSEDTIME).toBeBetween(100, 200);
                expect(cfthread.three.ELAPSEDTIME).toBeBetween(200, 300);
            });
        });
    }
}
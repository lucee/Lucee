component extends="org.lucee.cfml.test.LuceeTestCase" labels="logs" {

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-174",function() {
            it( title="checking writeLog() with empty text", body=function( currentSpec ) {
                application name="LDEV-174_Empty_text_log"; // 

                var logFile = "LDEV174_#createUUID()#";
                writelog(text="", file="#logFile#");

                expect(fileRead("#expandPath("{lucee-config}")#/logs/#logFile#.log")).toInclude('"LDEV-174_Empty_text_log",""'); 
            });
        });
    }

}
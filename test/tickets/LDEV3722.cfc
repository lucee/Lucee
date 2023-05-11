component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3722", function(){
            application action="update" customtagPaths=["#getDirectoryFromPath(getCurrentTemplatePath())#LDEV3722"];
            it(title="Calling customTag in thread & uses caller.thread to assign value", body=function( currentSpec ){
                thread name="LDEV_3722" {
                    thread.before = "before worked";
                    cf_myTag();
                    thread.after = "after worked";
                }
                thread action="join";
                expect(structKeyExists(cfthread.LDEV_3722, "before")).toBeTrue();
                expect(structKeyExists(cfthread.LDEV_3722, "setFromCustomTag")).toBeTrue();
                expect(structKeyExists(cfthread.LDEV_3722, "after")).toBeTrue();
            });
        });
    }
}
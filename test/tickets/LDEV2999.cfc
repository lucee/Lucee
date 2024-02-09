component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{

    function beforeAll() {
        variables.path = getDirectoryFromPath(getCurrentTemplatePath())&"LDEV2999\";
        variables.urlPath = "http://"&cgi.SERVER_NAME & createURI("LDEV2999/userAgent.cfm");
        variables.fileName = path&"LDEV2999.log";
    }

    function run( testResults, textbox ) {
        describe("testcase for LDEV-2999", function(){
            it(title = "schedule task with userAgent attribute", body = function ( currentSpec ){
                cfschedule(
                    action="update",
                    url="#urlPath#",
                    task="userAgenttest",
                    interval="daily",
                    startdate="#dateformat(now())#",
                    starttime="#timeFormat(now())#",
                    file="LDEV2999.log",
                    path="#path#",
                    publish="true",
                    userAgent="test_LDEV2999_userAgent"
                );

                cfschedule(action="run", task="userAgenttest");
                sleep(50); // to prevent file doesn't exist error
                res = fileRead(fileName);
                expect(trim(res)).toBe("test_LDEV2999_userAgent");
            });
        });
    }

    function afterAll() {
        if(fileExists(fileName)) fileDelete(fileName);
        try {
            cfschedule( action="delete", task="userAgenttest"); // throws error due to LDEV-3449
        }
        catch(any e) {}
    }

    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI & "" & calledName;
    }
} 
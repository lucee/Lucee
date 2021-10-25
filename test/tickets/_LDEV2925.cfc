component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    
    function beforeAll() {
        variables.path = getDirectoryFromPath(getCurrentTemplatePath())&"LDEV2925\";
        variables.urlPath = "http://"&cgi.SERVER_NAME & createURI("LDEV2925/getHeader.cfm");
        variables.fileName = path&"LDEV2925.log";
    }

    function run( testResults, textbox ) {
        describe("testcase for LDEV-2925", function(){
            it(title = "Cheking Scheduled tasks to passing Authorization header", body = function ( currentSpec ){
                cfschedule(
                    action="update",
                    url="#urlPath#",
                    username="User123",
                    password="Password123",
                    task="headertest",
                    interval="daily",
                    startdate="#dateformat(now())#",
                    starttime="#timeFormat(now())#",
                    file="LDEV2925.log",
                    path="#path#",
                    publish="true"
                );

                cfschedule(action="run", task="headertest");
                sleep(50); // to prevent file doesn't exist error
                res = fileRead(fileName);
                expect(trim(res)).toBe(true);
            });
        });
    }

    function afterAll() {
        cfschedule( action="delete", task="headertest");
        if(fileExists(fileName)) fileDelete(fileName);
    }

    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI & "" & calledName;
    }
}
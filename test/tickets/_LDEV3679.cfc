component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3679", body=function( currentSpec ) {
            variables.path = "http://"&cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME);
            it(title="Checking cfhttp with mimetype application/x-www-form-urlencoded", body=function( currentSpec )  {
               http url="#path#LDEV3679/test.cfm" result="local.res";
               expect(res.mimetype).toBe("application/x-www-form-urlencoded;charset=UTF-8");
               expect(isBinary(res.filecontent)).toBe(true);
            });
        });
    }
} 
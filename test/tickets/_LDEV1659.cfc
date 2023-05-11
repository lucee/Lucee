component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1659", body=function() {
			it( title='Initialize ORM Secondary cache',body=function( currentSpec ) {
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1659/index.cfm?AppName=myAppTwo" result="result";
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1659/index.cfm?AppName=myAppOne" result="result2";
			});

			it( title='Checking ORM cache connection',body=function( currentSpec ) {
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1659/index.cfm?AppName=myAppTwo" result="result3";
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1659/index.cfm?AppName=myAppOne" result="result4";
				assertEquals("1234", result4.filecontent.trim());
			});
		});
	}
}
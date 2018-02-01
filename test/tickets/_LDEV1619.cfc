component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1619");
	}
	function run( testResults , testBox ) {
		describe( "Test cases for LDEV-1619", function() {
			it(title = "Checking URL with first parameter as empty", body = function( currentSpec ) {
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1619/test.cfm?a=&a=1" result="local.res";
				expect(local.res.filecontent.trim()).toBe(",1");
			});
			it(title = "Checking URL with second parameter as empty", body = function( currentSpec ) {
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1619/test.cfm?a=1&a=" result="local.res";
				expect(local.res.filecontent.trim()).toBe("1,");
			});
			it(title = "Checking URL with middle parameter as empty", body = function( currentSpec ) {
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1619/test.cfm?a=1&a=&a=3" result="local.res";
				expect(local.res.filecontent.trim()).toBe("1,,3"); 
			});
			it(title = "Checking URL with first & last parameter as empty", body = function( currentSpec ) {
				http url="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#LDEV1619/test.cfm?a=&a=2&a=" result="local.res";
				expect(local.res.filecontent.trim()).toBe(",2,");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
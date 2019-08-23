component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){

	}

	function afterAll(){
		deleteFile("javaSettings/test.cfm");
		deleteFile("javaSettings/Test.cfc");
	}

	function run( testResults , testBox ) {
		describe( "test suite for recompiling CFML templates", function() {

			it(title="rewrite CFML template", body=function() {
				
				writeFile("javaSettings/test.cfm","1");
				savecontent variable="local.c" {
					include "javaSettings/test.cfm";
				}
				expect(c).toBe("1");
				sleep(1111);
				pagePoolClear();

				
				writeFile("javaSettings/test.cfm","2");
				savecontent variable="local.c" {
					include "javaSettings/test.cfm";
				}
				expect(c).toBe("2");
				pagePoolClear();

			});

			it(title="rewrite CFC template", body=function() {
				
				writeFile("javaSettings/Test.cfc","component {function test(){return 1;}}");
				var c=new javaSettings.Test();
				expect(c.test()).toBe(1);
				sleep(1111);
				pagePoolClear();

				writeFile("javaSettings/Test.cfc","component {function test(){return 2;}}");
				var c=new javaSettings.Test();
				expect(c.test()).toBe(2);
				pagePoolClear();


			});


		});
	}

	private function writeFile(string template, string data){
		var curr=getDirectoryFromPath(getCurrenttemplatepath());
		var path=curr&template;
		fileWrite(path,data);
	}

	private function deleteFile(string template){
		var curr=getDirectoryFromPath(getCurrenttemplatepath());
		var path=curr&template;
		if(fileExists(path)) fileDelete(path);
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}

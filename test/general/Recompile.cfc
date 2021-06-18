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
				//sleep(1500); // some os 
				InspectTemplates();
				writeFile("javaSettings/test.cfm","10");
				savecontent variable="local.d" {
					include "javaSettings/test.cfm";
				}
    			expect(d).toBe("10");
			});

			it(title="rewrite CFC template", body=function() {
				
				writeFile("javaSettings/ATest.cfc","component {function test(){return '1';}}");
				var c=new javaSettings.ATest();
				expect(c.test()).toBe(1);
    			//sleep(1500);
				InspectTemplates();

				writeFile("javaSettings/ATest.cfc","component {function test(){return '10';}}");
				var d=new javaSettings.ATest();
				expect(d.test()).toBe(10);
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

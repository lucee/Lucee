component extends="org.lucee.cfml.test.LuceeTestCase"{
 	function invokeTest(test) { 
		var obj = createObject("component","LDEV1679.test"); 
		invoke(obj,"FuncWithArg",arguments);
		return structIsempty(arguments);
 	}
 	
	function run(){
		describe( title="Test suite for LDEV-1679", body=function(){
			it(title="Checking invoke function with arguments scope ", body=function(){
				try {
					var dir="#GetDirectoryFromPath(GetCurrentTemplatePath())#LDEV1679\";
					if(!directoryExists(dir)){
						Directorycreate(dir);
					}
					var testfile = dir&"test.cfc";
					fileWrite(testfile,"
						component {
						function FuncWithArg(test) { 
						}
						}");
					var testStr = {};
					var result = invokeTest(testStr);
					expect(result).toBe('false');
				}
				finally {
					if(!isNull(testfile) && fileExists(testfile)) {
						fileDelete(testfile);
					}
				}
			});
		});
	}
}
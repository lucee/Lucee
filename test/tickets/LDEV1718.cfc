component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1718");
		if(not directoryExists(variables.uri)){
			Directorycreate(variables.uri);
			Directorycreate("#variables.uri#/test/");
		}
	}

	function run(){
		describe( title="Test suite for LDEV-1718", body=function(){
			it(title="Checking expandPath() with mapping '/' ", body=function(){
				var mappings["/"] = "D:\";
				application action="update" mappings=mappings;
				var getMappings = GetApplicationSettings().mappings;
				assertEquals("D:\",ExpandPath("/"));
				var Parentpath = "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1718\test\";
				var pathLast = "#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#";
				// /tickets/LDEV1718/test is original root path
				assertEquals(Parentpath, ExpandPath("/#pathLast#/LDEV1718/test/"));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
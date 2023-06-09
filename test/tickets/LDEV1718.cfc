component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV1718");
		if(not directoryExists(variables.uri)){
			Directorycreate(variables.uri);
			Directorycreate("#variables.uri#/test/");
		}
		variables.oldMappings = GetApplicationSettings().mappings;
	}

	function afterAll(){
		application action="update" mappings=variables.oldMappings;
	}

	function run(){
		describe( title="Test suite for LDEV-1718", body=function(){
			it(title="Checking expandPath() with mapping '/' ", skip=true, body=function(){ // this needs work, disabled
				var isWindows =find("Windows", server.os.name );
				var root = isWindows ? "c:\" : "/";
				var mappings["/"] = root;
				application action="update" mappings=mappings;
				var getMappings = GetApplicationSettings().mappings;
				expect( expandPath( "/" ) ).toBe( root );
				var parentpath = "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1718\test\";
				var pathLast = "#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#";
				// /tickets/LDEV1718/test is original root path
				expect( ExpandPath("/#pathLast#/LDEV1718/test/") ).toBe( Parentpath );
			});

			it(title="Checking expandPath() with conflict top level dir", body=function(){ // this needs work, disabled

				var uri = createURI("LDEV1718");
				var result = _InternalRequest(
					template:"#uri#/index.cfm"
				);
				expect(result.filecontent.trim()).toInclude("ldev1718test");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
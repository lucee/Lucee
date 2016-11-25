component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1068 with preserve case true", function() {
			it("Checking serializeJSON() for serializing query with preserve case true", function( currentSpec ){
				uri = createURI("LDEV1068/test1.cfm");
				result = _InternalRequest(
					template:uri,
					forms: { Scene = 1 }
				);
				expect(find('"NAME":', result.fileContent.trim()) > 0).toBeTrue();
				expect(find('"AGE":', result.fileContent.trim()) > 0).toBeTrue();
				expect(find('"SEX":', result.fileContent.trim()) > 0).toBeTrue();
			});

			it("Checking cfwddx tag for serializing query with preserve case true", function( currentSpec ){
				uri = createURI("LDEV1068/test1.cfm");
				result = _InternalRequest(
					template:uri,
					forms: { Scene = 2 }
				);
				expect(result.fileContent.trim()).toBeTrue();
			});

			it("Checking query.columnlist with preserve case true", function( currentSpec ){
				uri = createURI("LDEV1068/test1.cfm");
				result = _InternalRequest(
					template:uri,
					forms: { Scene = 3 }
				);
				expect(result.fileContent.trim()).toBeTrue();
			});
		});

		describe( "Test suite for LDEV-1068 with preserve case false", function() {
			it("Checking serializeJSON() for serializing query with preserve case false", function( currentSpec ){
				uri = createURI("LDEV1068/test2.cfm");
				result = _InternalRequest(
					template:uri,
					forms: { Scene = 1 }
				);
				expect(find('"NAME":', result.fileContent.trim()) > 0).toBeTrue();
				expect(find('"AGE":', result.fileContent.trim()) > 0).toBeTrue();
				expect(find('"SEX":', result.fileContent.trim()) > 0).toBeTrue();
			});

			it("Checking cfwddx tag for serializing query with preserve case false", function( currentSpec ){
				uri = createURI("LDEV1068/test2.cfm");
				result = _InternalRequest(
					template:uri,
					forms: { Scene = 2 }
				);
				expect(result.fileContent.trim()).toBeTrue();
			});

			it("Checking query.columnlist with preserve case false", function( currentSpec ){
				uri = createURI("LDEV1068/test2.cfm");
				result = _InternalRequest(
					template:uri,
					forms: { Scene = 3 }
				);
				expect(result.fileContent.trim()).toBeTrue();
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
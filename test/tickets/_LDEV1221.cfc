component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1221",  body=function(){
			describe( title="checking component with static data to access via dynamic way",  body=function(){
				it(title="check the dynamic variable with evaluate function", body=function(){
					var uri = createURI("LDEV1221/test1.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.fileContent.trim()).toBe('foo');
				});

				it(title="check the dynamic variable with evaluate function within hash", body=function(){
					var uri = createURI("LDEV1221/test2.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.fileContent.trim()).toBe('foo');
				});

				it(title="check the dynamic variable with array bracket notation", body=function(){
					var uri = createURI("LDEV1221/test3.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.fileContent.trim()).toBe('foo');
				});

				it(title="check with dynamic variable", body=function(){
					var uri = createURI("LDEV1221/test4.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.fileContent.trim()).toBe('foo');
				});

				it(title="check with dynamic variable within hash", body=function(){
					var uri = createURI("LDEV1221/test5.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.fileContent.trim()).toBe('foo');
				});
			});

			describe( title="checking with creating object function on static data",  body=function(){
				it(title="testing with object variable with doubleDotNotation", body=function(){
					var uri = createURI("LDEV1221/test6.cfm");
					var result = _InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					expect(result.fileContent.trim()).toBe('foo');
				});

				it(title="testing with object variable with singleDotNotation", body=function(){
					var uri = createURI("LDEV1221/test6.cfm");
					var result = _InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					expect(result.fileContent.trim()).toBe('foo');
				});
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
	
}
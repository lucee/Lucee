component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1152", body=function(){
			it(title="Checking with CFML content/code as text in script based component", body=function(){
				local.uri = createURI("LDEV1152/invoke.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene:1
					}
				);
				expect(trim(local.result.fileContent)).toBe("false|");
			});

			it(title="Checking with CFML content/code as text in cfscript within tag based component", body=function(){
				local.uri = createURI("LDEV1152/invoke.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{
						Scene:2
					}
				);
				expect(trim(local.result.fileContent)).toBe("false|");
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
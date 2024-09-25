component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="LDEV-5094 onMissingFunction feature", body=function() {
			it( title='should trigger onMissingFunction in Application.cfc when a nonexistent function is called with positional arguments',body=function( currentSpec ) {
				var uri = createURI("LDEV5094");
				local.result = _InternalRequest(
					template:"#uri#/testPositionalArgs.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('Function name: someTestFunction. Arguments: ["arg1",true]');
			});

			it( title='should trigger onMissingFunction in Application.cfc when a nonexistent function is called with named arguments',body=function( currentSpec ) {
				var uri = createURI("LDEV5094");
				local.result = _InternalRequest(
					template:"#uri#/testNamedArgs.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('Function name: anotherTestFunction. Arguments: {"test":true}');
			});

			it( title='should have lucee throw the usual missing function error when Application.cfc does not implement onMissingFunction',body=function( currentSpec ) {
				var uri = createURI("LDEV5094/notimplemented");
				local.result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toContain('No matching function [myMissingFunction] found');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

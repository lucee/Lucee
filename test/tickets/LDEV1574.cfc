component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1574",  body=function() {
			it(title="checking GetSystemMetrics() without enable clientScope", body = function( currentSpec ) {
				var uri=createURI("LDEV1574/App1/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('true');
			});

			it(title="checking GetSystemMetrics() enable clientScope", body = function( currentSpec ) {
				var uri=createURI("LDEV1574/App2/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('true');

			});

			it(title="checking GetSystemMetrics() CPU", body = function( currentSpec ) {
				var system=GetSystemMetrics();
				expect(system.cpuProcess>=0).toBeTrue();
				expect(system.cpuSystem>=0).toBeTrue();

			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

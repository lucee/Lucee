component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-547", function() {
			describe( "checking cache functions working with default cache", function() {
				it(title="cacheGet() and cachePut() functions with default cache , Used In first Application", body = function( currentSpec ) {
					var uri=createURI("LDEV0547/App1/index.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("AppA");
				});

				it(title="cacheGet() and cachePut() functions with default cache , Used In second Application", body = function( currentSpec ) {
					var uri=createURI("LDEV0547/App2/index.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("AppB");
				});

				it(title="cfcache tag working with default cache, Used In first Application", body = function( currentSpec ) {
					var uri=createURI("LDEV0547/App3/index.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("AppC");
				});

				it(title="cfcache tag working with default cache, Used In second Application ", body = function( currentSpec ) {
					var uri=createURI("LDEV0547/App4/index.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("AppD");
				});
			});

		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

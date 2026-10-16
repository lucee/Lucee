component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {
	function beforeAll() {
		variables.uri = createURI("LDEV4285");
	}
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4285", function() {
			it( title="entityLoad() with positional argument(name)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 1 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with positional arguments(name, idOrFilter)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 2 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with positional arguments(name, idOrFilter, uniqueOrOrder)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 3 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with positional arguments(name, idOrFilter, uniqueOrOrder, options)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 4 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with named arguments(name, idOrFilter, uniqueOrOrder, options)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 5 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with named arguments(name, idOrFilter, options)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 6 }
				);
				expect(result.filecontent.trim()).toBe(true);
			}); 
			it( title="entityLoad() with named arguments(name, idOrFilter)", skip="true", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 7 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with named arguments(name, options)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 8 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
				it( title="entityLoad() with named arguments(name, uniqueOrOrder)", skip=noOrm(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 9 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
			it( title="entityLoad() with named arguments(name, idOrFilter, uniqueOrOrder)", skip=true, body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 10 }
				);
				expect(result.filecontent.trim()).toBe(true);
			});
		});
	}
	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function noOrm() {
		return ( structCount( server.getTestService("orm") ) eq 0 );
	}
}

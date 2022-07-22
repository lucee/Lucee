component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1740", body=function() {
			it( title='Checking SQL Comments in QueryExecute()',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1740");
				var result = _InternalRequest(
					template:"#uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(result.filecontent.trim()).toBe('true');
			});

			it( title='Checking SQL Comments in cfquery tag',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1740");
				var result = _InternalRequest(
					template:"#uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(result.filecontent.trim()).toBe('true');
			});
		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
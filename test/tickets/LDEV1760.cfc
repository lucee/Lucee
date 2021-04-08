component extends="org.lucee.cfml.test.LuceeTestCase"{
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql) && mysql.count()>0){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1760", body=function() {
			it( title='checking Dialect ORM with mySQL without setting in ORM settings',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/App1/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});

			it( title='checking Dialect ORM with mySQL with dialect="MySQLwithInnoDB" in ORM settings',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/App2/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
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
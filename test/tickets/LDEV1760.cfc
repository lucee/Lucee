component extends="org.lucee.cfml.test.LuceeTestCase"{
	// skip closure
	function isNotSupported(db) {
		if (db eq "oracle" or db eq "mssql" ) 			return true; // currently broken
		var mySql = getCredentials(db);
		if(!isNull(mysql) && mysql.count()>0){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1760", body=function() {
			it( title='MySql checking ORM auto detect dialect',skip=isNotSupported("mysql"),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/AutoDetectMySql/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});

			it( title='Oracle checking ORM auto detect dialect',skip=isNotSupported("oracle"),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/AutoDetectOracle/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});

			it( title='Postgres checking ORM auto detect dialect',skip=isNotSupported("postgres"),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/AutoDetectPostgres/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});

			it( title='MsSql checking ORM auto detect dialect',skip=isNotSupported("mssql"),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/AutoDetectMsSql/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});

			it( title='checking Dialect ORM with mySQL with dialect="MySQLwithInnoDB" in ORM settings',skip=isNotSupported("mysql"),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/MySQLwithInnoDB/index.cfm"
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

	private struct function getCredentials(db) {
		return server.getDatasource(db);
	}
}
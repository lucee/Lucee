component extends="org.lucee.cfml.test.LuceeTestCase"  labels="orm" {
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1569",  skip=checkMySqlEnvVarsAvailable(), body=function() {
			xit(title="checking SerializeJSON() with ORM key having NULL value",
					skip=checkMySqlEnvVarsAvailable(),
					body = function( currentSpec ) {
				var uri=createURI("LDEV1569/orm.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: false
					}
				);
				expect(result.filecontent.trim()).toBe('{"ID":2,"name":""}'); // nulls are an empty string, unless fullnullsupport
			});

			xit(title="checking SerializeJSON() ORM key with NULL value, nullSupport=true",
					skip=checkMySqlEnvVarsAvailable(),
					body = function( currentSpec ) {
				var uri=createURI("LDEV1569/orm.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: true
					}
				);
				expect(result.filecontent.trim()).toBe('{"ID":2, "Name":null}');

			});

			it(title="checking SerializeJSON() mysql query with NULL value",
					skip=checkMySqlEnvVarsAvailable(),
					body = function( currentSpec ) {
				var uri=createURI("LDEV1569/query.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: false
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":1,"Name":""}]'); // nulls are an empty string, unless fullnullsupport
			});

			it(title="checking SerializeJSON() mysql query with NULL value, nullSupport=true",
					skip=checkMySqlEnvVarsAvailable(),
					body = function( currentSpec ) {
				var uri=createURI("LDEV1569/query.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: true
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":1,"Name":null}]');
			});

			it(title="checking SerializeJSON() for QoQ with NULL value", body = function( currentSpec ) {
				var uri=createURI("LDEV1569/qoq.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: false
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":3,"Name":""}]');  // nulls are an empty string, unless fullnullsupport
			});

			it(title="checking SerializeJSON() for QoQ with NULL value, nullSupport=true", body = function( currentSpec ) {
				var uri=createURI("LDEV1569/qoq.cfm");
				var result = _InternalRequest(
					template:uri,
					url:{
						nullSupport: true
					}
				);
				expect(result.filecontent.trim()).toBe('[{"ID":3,"Name":null}]');
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		// getting the credentials from the environment variables
		var mySQL = server.getDatasource("mysql");
		return structIsEmpty(mySQL);
	}
}

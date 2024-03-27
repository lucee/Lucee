component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql"{
	function isMySqlNotSupported() {
		var mySql = mySqlCredentials();
		return isEmpty(mysql);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-215", body=function() {
			it( title='Checking MYSQL, INDEX for the client/session storage table',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV0215");
				var result = _InternalRequest(
					template:"#uri#/mysql/mysql.cfm"
				);
				// do it twice coz client data gets written out after the request
				result = _InternalRequest(
					template:"#uri#/mysql/mysql.cfm"
				);
				var res = result.fileContent.trim().deserializeJson();
				var hasExpires = (listFirst(server.lucee.version, ".") gte 6);
				expect(res.session[1].toJson()).toBe('{"INDEX_NAME":"ix_cf_session_data"}');
				if (hasExpires)
					expect(res.session[2].toJson()).toBe('{"INDEX_NAME":"ix_cf_session_data_expires"}');

				expect(res.client[1].toJson()).toBe('{"INDEX_NAME":"ix_cf_client_data"}');
				if (hasExpires)
					expect(res.client[2].toJson()).toBe('{"INDEX_NAME":"ix_cf_client_data_expires"}');
				
			});
	});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	private struct function mySqlCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}
}
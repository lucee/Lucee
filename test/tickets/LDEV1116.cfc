component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1116",  skip=doSkip(),body=function(){
			it(title="Checking oracle query to select current Date & time", body=function(){
				var orc = getCredentials();
				var oracletestdb = {
					  class: 'oracle.jdbc.OracleDriver'
					, bundleName: 'ojdbc6'
					, bundleVersion: '11.2.0.4'
					, connectionString: 'jdbc:oracle:thin:@#orc.server#:#orc.port#/#orc.database#'
					, username: orc.username
					, password: orc.password
				};
				var Test = queryExecute("SELECT	systimestamp AS db_time FROM dual", {}, {datasource="#oracletestdb#", result="result"});
				//expect(getMetadata(Test.DB_Time[1]).getName()).toBe("java.lang.String");
				expect(left(Test.DB_Time[1],4)).toBe("{ts ");
			});
		});
	}


	private boolean function doSkip() {
		return structCount(getCredentials())==0;
	}

	private struct function getCredentials() {
		return server.getDatasource("oracle");
	}
}

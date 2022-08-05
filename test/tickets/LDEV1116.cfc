component extends="org.lucee.cfml.test.LuceeTestCase" labels="oracle"{
	function run(){
		describe( title="Test suite for LDEV-1116",  skip=doSkip(),body=function(){
			it(title="Checking oracle query to select current Date & time", body=function(){
				var oracletestdb = server.getDatasource("oracle");

				var Test = queryExecute("SELECT	systimestamp AS db_time FROM dual", {}, {datasource="#oracletestdb#", result="result"});
				//expect(getMetadata(Test.DB_Time[1]).getName()).toBe("java.lang.String");
				expect(left(Test.DB_Time[1],4)).toBe("{ts ");
			});
		});
	}

	private boolean function doSkip() {
		return structCount(server.getDatasource("oracle"))==0;
	}

}

component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	variables.mysql = server.getDatasource("mysql");

	private function isMySqlNotSupported() {
		return isEmpty(variables.mysql);
	}

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-4871", function() {
            it( title="testing array", body=function( currentSpec ) {
				var arr=[1,2,3];
				var ser=objectSave(arr);
				var rel=objectLoad(ser);
				expect( serializeJson(rel) ).toBe( serializeJson(arr) );
			});
            it( title="testing struct", body=function( currentSpec ) {
				var sct={a:1};
				var ser=objectSave(sct);
				var rel=objectLoad(ser);
				expect( serializeJson(rel) ).toBe( serializeJson(sct) );
			});
            it( title="testing query",skip=isMySqlNotSupported(), body=function( currentSpec ) {
				query datasource=variables.mysql name="local.qry" {
					```
					show tables
					```
				}
				var ser=objectSave(qry);
				var rel=objectLoad(ser);
				expect( serializeJson(rel) ).toBe( serializeJson(qry) );
			});
		});
    }
}

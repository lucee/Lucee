component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2104", function() {
			it(title = "allow queryformat=row when requesting data from a CFC", body = function( currentSpec ) {
				cfhttp(url="http://#CGI.SERVER_NAME#/test/testcases/LDEV2104/test.cfc?method=testFun&queryFormat=row" result="res") {
				}
				expect(res.filecontent.trim()).toBe('{"COLUMNS":["FIRSTNAME","LASTNAME"],"DATA":[["Lucee","Server"],["Test ","Case"]]}');
			});

			it(title = "allow queryformat=column when requesting data from a CFC", body = function( currentSpec ) {
				cfhttp(url="http://#CGI.SERVER_NAME#/test/testcases/LDEV2104/test.cfc?method=testFun&queryFormat=column" result="res") {
				}
				expect(res.filecontent.trim()).toBe('{"ROWCOUNT":2,"COLUMNS":["FIRSTNAME","LASTNAME"],"DATA":{"FIRSTNAME":["Lucee","Test "],"LASTNAME":["Server","Case"]}}');
			});

			it(title = "allow queryformat=struct when requesting data from a CFC", body = function( currentSpec ) {
				cfhttp(url="http://#CGI.SERVER_NAME#/test/testcases/LDEV2104/test.cfc?method=testFun&queryFormat=struct" result="res") {
				}
				expect(res.filecontent.trim()).toBe('[{"FIRSTNAME":"Lucee","LASTNAME":"Server"},{"FIRSTNAME":"Test ","LASTNAME":"Case"}]');
			});
		});
	}
}
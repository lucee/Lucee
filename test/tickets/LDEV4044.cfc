component extends="org.lucee.cfml.test.LuceeTestCase" lables="query" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV4044");
		variables.qry = queryNew("id,name,num", "integer,varchar,integer", [[1,"foo",1],[2,"bar",2]]);
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4044", function() {
			it( title="QoQ with named parameter and without params defined", body=function( currentSpec ) {
				expect( ()=> {
					queryExecute(sql="SELECT name FROM qry WHERE id = :id"
						,options={dbtype="query"}
					);
				}).toThrow();
			});
			it( title="QoQ with named parameter and empty params defined", body=function( currentSpec ) {
				expect( ()=> {
					queryExecute(sql="SELECT name FROM qry WHERE id = :id"
						,params={}
						,options={dbtype="query"}
					);
				}).toThrow();
			});
			it( title="QoQ with positional parameter and without params defined", body=function( currentSpec ) {
				expect( ()=> {
					queryExecute(sql="SELECT name FROM qry WHERE id = ?"
						,options={dbtype="query"}
					);
				}).toThrow();
			});
			it( title="QoQ with positional parameter and empty params defined", body=function( currentSpec ) {
				expect( ()=> {
					queryExecute(sql="SELECT name FROM qry WHERE id = ?"
						,params={}
						,options={dbtype="query"}
					);
				}).toThrow();
			});

			it( title="JDBC query with named parameter and without params defined", skip="#notHasMysql()#" , body=function( currentSpec ) {
				var res = _internalRequest(
					template : "#variables.uri#/LDEV4044.cfm",
					forms : {scene:"named",params:"without params"}
					).fileContent.trim();
					expect(res).toBe("JDBC query with named parameter and without Params failed as expected");
			});
			it( title="JDBC query with named parameter and empty params defined", skip="#notHasMysql()#" ,body=function( currentSpec ) {
				var res = _internalRequest(
					template : "#variables.uri#/LDEV4044.cfm",
					forms : {scene:"named",params:"empty params"}
				).fileContent.trim();
				expect(res).toBe("JDBC query with named parameter and empty Params failed as expected");
			});
			it( title="JDBC query with positional parameter and without params defined", skip="#notHasMysql()#" , body=function( currentSpec ) {
				var res = _internalRequest(
					template : "#variables.uri#/LDEV4044.cfm",
					forms : {scene:"positional",params:"without params"}
				).fileContent.trim();
				expect(res).toBe("JDBC query with positional parameter and without Params failed as expected");
			});
			it( title="JDBC query with positional parameter and empty params defined", skip="#notHasMysql()#" , body=function( currentSpec ) {
				var res = _internalRequest(
					template : "#variables.uri#/LDEV4044.cfm",
					forms : {scene:"positional",params:"empty params"}
				).fileContent.trim();
				expect(res).toBe("JDBC query with positional parameter and empty Params failed as expected");
			});
		}); 
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function notHasMysql() {
		return structCount(server.getDatasource("mysql")) == 0;
	}
}
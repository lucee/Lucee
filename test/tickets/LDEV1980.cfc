component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1980");
		variables.adminWeb=new org.lucee.cfml.Administrator("web", request.WebAdminPassword);
	}
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1980", body=function() {
			it(title = "checking cfdbinfo without DB name", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				
				adminWeb.updateDatasource(argumentCollection = deserialiZejson(local.result.filecontent));
				variables.dsn=deserialiZejson(local.result.filecontent);
				tableCreation();
				cfdbinfo(datasource=variables.dsn.newname,name="return_variable",type= "column",table="TestDsnTBL");
				expect(IsQuery(return_variable)).toBe('true');
			});

			it(title = "checking cfdbinfo with DB name", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);

				adminWeb.updateDatasource(argumentCollection = deserialiZejson(local.result.filecontent));
				variables.dsn=deserialiZejson(local.result.filecontent);
				tableCreation();
				cfdbinfo(datasource=variables.dsn.newname,name="return_variable",type= "column",table="TestDsnTBL",dbname=variables.dsn.database);
				expect(IsQuery(return_variable)).toBe('true');
			});
		});
	}

	private function tableCreation(){
		query name="test" datasource=dsn.newname{
			echo("DROP TABLE IF EXISTS `TestDsnTBL`");
		}
		query name="test" datasource=dsn.newname {
			echo( "
				create table `TestDsnTBL`(id varchar(10),Personname varchar(10))"
				);
		}
	}

	function afterAll(){
		query name="test" datasource=dsn.newname {
			echo( "
					DROP DATABASE IF EXISTS `LDEV1980DB` 
				");
		}
		adminWeb.removeDatasource(dsn=dsn.newname,password="password",name=dsn.newname,remoteClients="arrayOfClients");
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
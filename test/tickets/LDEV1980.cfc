component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1980",skip=isNotSupported(), body=function() {
			it(title = "checking cfdbinfo without DB name", body = function( currentSpec ) {
				var ds=getDatasource();
				tableCreation(ds);
				cfdbinfo(datasource=ds,name="local.return_variable",type= "columns",table="TestDsnTBL");
				expect(IsQuery(return_variable)).toBe('true');
				//systemOutput(return_variable,1,1);
			});

			it(title = "checking cfdbinfo with DB name",skip=isNotSupported(), body = function( currentSpec ) {
				var ds=getDatasource();
				tableCreation(ds);
				cfdbinfo(datasource=ds,name="local.pre",type= "columns",table="TestDsnTBL");
				
				cfdbinfo(datasource=ds,name="local.return_variable",type= "columns",table="TestDsnTBL",dbname=pre.TABLE_CAT);
				expect(IsQuery(return_variable)).toBe('true');
			});
		});
	}

	private function tableCreation(ds){
		ds.storage=true;
		query name="test" datasource=ds {
			echo("DROP TABLE IF EXISTS `TestDsnTBL`");
		}
		query name="test" datasource=ds {
			echo( "
				create table `TestDsnTBL`(id varchar(10),Personname varchar(10))"
				);
		}
	}

	function afterAll(){
		if(isNotSupported()) return;
		query name="test" datasource=getDatasource() {
			echo( "
					DROP DATABASE IF EXISTS `LDEV1980DB` 
				");
		}
	}

	function isNotSupported() {
		var mySql = getCredentials();
		if(structCount(mySql)){
			return false;
		} else{
			return true;
		}
	}

	private function getDatasource() {
		var cred=getCredentials();
		if(structCount(cred)>0){
			return cred;
		}
		return {};
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}